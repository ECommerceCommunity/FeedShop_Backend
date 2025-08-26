package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.review.application.dto.request.ReviewReportRequest;
import com.cMall.feedShop.review.application.dto.response.ReportedReviewResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewReportResponse;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewReport;
import com.cMall.feedShop.review.domain.enums.ReviewStatus;
import com.cMall.feedShop.review.domain.exception.DuplicateReportException;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.review.domain.repository.ReviewReportRepository;
import com.cMall.feedShop.user.domain.exception.UserNotFoundException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReviewReportService {

    private final ReviewRepository reviewRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final UserRepository userRepository;

    @Value("${review.report.auto-hide-threshold:5}")
    private int autoHideThreshold;

    public ReviewReportResponse reportReview(Long reviewId, String reporterLoginId, ReviewReportRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("신고할 리뷰를 찾을 수 없습니다."));

        log.info("DEBUG - userRepository.findByLoginId 호출: {}", reporterLoginId);
        User reporter = userRepository.findByLoginId(reporterLoginId)
                .orElseThrow(() -> {
                    log.error("DEBUG - 사용자를 찾을 수 없음: {}", reporterLoginId);
                    return new UserNotFoundException("신고자 정보를 찾을 수 없습니다.");
                });
        log.info("DEBUG - 사용자 조회 성공: userId={}, loginId={}", reporter.getId(), reporter.getLoginId());

        if (reviewReportRepository.existsByReviewAndReporter(review, reporter)) {
            throw new DuplicateReportException("이미 신고한 리뷰입니다.");
        }

        ReviewReport reviewReport = ReviewReport.builder()
                .review(review)
                .reporter(reporter)
                .reason(request.getReason())
                .description(request.getDescription())
                .build();

        ReviewReport savedReport = reviewReportRepository.save(reviewReport);

        updateReviewReportCount(review);
        checkAutoHideThreshold(review);

        log.info("리뷰 신고 완료: reviewId={}, reporterLoginId={}, reason={}", 
                reviewId, reporterLoginId, request.getReason());

        return mapToReportResponse(savedReport);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ReportedReviewResponse> getReportedReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Long> reviewIdPage = reviewReportRepository.findDistinctReviewIdsWithUnprocessedReports(pageable);
        
        if (reviewIdPage.getContent().isEmpty()) {
            return PaginatedResponse.<ReportedReviewResponse>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0L)
                    .totalPages(0)
                    .build();
        }
        
        List<ReviewReport> reports = reviewReportRepository.findUnprocessedReportsByReviewIds(reviewIdPage.getContent());
        
        List<ReportedReviewResponse> responses = reports.stream()
                .collect(Collectors.groupingBy(report -> report.getReview().getReviewId()))
                .values().stream()
                .map(this::mapToReportedReviewResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<ReportedReviewResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalElements(reviewIdPage.getTotalElements())
                .totalPages(reviewIdPage.getTotalPages())
                .build();
    }

    public void hideReview(Long reviewId, String adminLoginId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("숨김 처리할 리뷰를 찾을 수 없습니다."));

        review.markAsHidden();
        review.getImages().forEach(image -> image.delete());
        
        List<ReviewReport> reports = reviewReportRepository.findByReview(review);
        reports.forEach(ReviewReport::markAsProcessed);
        
        reviewRepository.save(review);

        log.info("리뷰 숨김 처리 완료: reviewId={}, adminLoginId={}", reviewId, adminLoginId);
    }

    public void showReview(Long reviewId, String adminLoginId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("숨김 해제할 리뷰를 찾을 수 없습니다."));

        review.markAsActive();
        reviewRepository.save(review);

        log.info("리뷰 숨김 해제 완료: reviewId={}, adminLoginId={}", reviewId, adminLoginId);
    }

    private void updateReviewReportCount(Review review) {
        review.increaseReportCount();
        reviewRepository.save(review);
    }

    private void checkAutoHideThreshold(Review review) {
        long reportCount = reviewReportRepository.countByReview(review);
        if (reportCount >= autoHideThreshold && review.getStatus() == ReviewStatus.ACTIVE) {
            review.markAsHidden();
            reviewRepository.save(review);
            
            log.warn("리뷰 자동 숨김 처리: reviewId={}, reportCount={}", review.getReviewId(), reportCount);
        }
    }

    private ReviewReportResponse mapToReportResponse(ReviewReport report) {
        return ReviewReportResponse.builder()
                .reportId(report.getReportId())
                .reviewId(report.getReview().getReviewId())
                .reporterId(report.getReporter().getId())
                .reason(report.getReason())
                .description(report.getDescription())
                .isProcessed(report.isProcessed())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private ReportedReviewResponse mapToReportedReviewResponse(List<ReviewReport> reports) {
        ReviewReport firstReport = reports.get(0);
        Review review = firstReport.getReview();

        List<ReportedReviewResponse.ReportInfo> reportInfos = reports.stream()
                .map(report -> ReportedReviewResponse.ReportInfo.builder()
                        .reportId(report.getReportId())
                        .reporterId(report.getReporter().getId())
                        .reporterName(report.getReporter().getUserProfile() != null ? 
                                report.getReporter().getUserProfile().getNickname() : "사용자")
                        .reason(report.getReason())
                        .description(report.getDescription())
                        .isProcessed(report.isProcessed())
                        .createdAt(report.getCreatedAt())
                        .build())
                .toList();

        return ReportedReviewResponse.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .content(review.getContent())
                .writerId(review.getUser().getId())
                .writerName(review.getUser().getUserProfile() != null ? 
                        review.getUser().getUserProfile().getNickname() : "사용자")
                .productId(review.getProduct().getProductId())
                .productName(review.getProduct().getName())
                .status(review.getStatus())
                .totalReportCount((long) reports.size())
                .unprocessedReportCount(reports.stream()
                        .filter(report -> !report.isProcessed())
                        .count())
                .reports(reportInfos)
                .reviewCreatedAt(review.getCreatedAt())
                .build();
    }
}