package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.review.application.dto.response.ReviewListResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewImageResponse;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewReadService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewImageService reviewImageService;

    /**
     * 상품별 리뷰 목록 조회
     *
     * @param productId 상품 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 방식 ("points", "latest" 등)
     * @return 리뷰 목록 응답
     */
    public ReviewListResponse getProductReviews(Long productId, int page, int size, String sort) {
        log.info("상품 리뷰 목록 조회 시작: 상품ID={}, 페이지={}, 크기={}, 정렬={}", productId, page, size, sort);
        
        try {

        // 페이지 검증 및 기본값 설정
        page = Math.max(0, page);
        size = (size < 1 || size > 100) ? 20 : size;

        Pageable pageable = PageRequest.of(page, size);

        Page<Review> reviewPage;
        if ("points".equals(sort)) {
            reviewPage = reviewRepository.findActiveReviewsByProductIdOrderByPoints(productId, pageable);
        } else {
            reviewPage = reviewRepository.findActiveReviewsByProductId(productId, pageable);
        }

        List<ReviewResponse> reviewResponses = convertReviewsToResponses(reviewPage.getContent());
        Page<ReviewResponse> reviewResponsePage = new PageImpl<>(
                reviewResponses, pageable, reviewPage.getTotalElements());

        // 통계 정보 조회
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.countActiveReviewsByProductId(productId);

            log.info("리뷰 목록 조회 완료: 총 {}개, 평균 평점 {}", totalReviews, averageRating);

            return ReviewListResponse.of(reviewResponsePage, averageRating, totalReviews);
            
        } catch (Exception e) {
            log.error("상품 리뷰 목록 조회 중 오류 발생: 상품ID={}, 에러={}", productId, e.getMessage(), e);
            throw new RuntimeException("리뷰 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 필터링이 적용된 상품별 리뷰 목록 조회
     */
    public ReviewListResponse getProductReviewsWithFilters(Long productId, int page, int size, String sort, 
                                                          Integer rating, String sizeFit, String cushion, String stability) {
        log.info("필터링된 상품 리뷰 목록 조회 시작: 상품ID={}, 평점={}, 착용감={}, 쿠션감={}, 안정성={}", 
                productId, rating, sizeFit, cushion, stability);
        
        try {
            // 페이지 검증 및 기본값 설정
            page = Math.max(0, page);
            size = (size < 1 || size > 100) ? 20 : size;

            Pageable pageable = PageRequest.of(page, size);

            // 문자열을 enum으로 변환
            SizeFit sizeFitEnum = sizeFit != null ? SizeFit.valueOf(sizeFit.toUpperCase()) : null;
            Cushion cushionEnum = cushion != null ? Cushion.valueOf(cushion.toUpperCase()) : null;
            Stability stabilityEnum = stability != null ? Stability.valueOf(stability.toUpperCase()) : null;

            Page<Review> reviewPage = reviewRepository.findActiveReviewsByProductIdWithFilters(
                    productId, rating, sizeFitEnum, cushionEnum, stabilityEnum, pageable);

            List<ReviewResponse> reviewResponses = convertReviewsToResponses(reviewPage.getContent());
            Page<ReviewResponse> reviewResponsePage = new PageImpl<>(
                    reviewResponses, pageable, reviewPage.getTotalElements());

            // 통계 정보 조회 (전체 리뷰 기준)
            Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
            Long totalReviews = reviewRepository.countActiveReviewsByProductId(productId);

            log.info("필터링된 리뷰 목록 조회 완료: 필터링된 {}개, 전체 {}개", reviewPage.getTotalElements(), totalReviews);

            return ReviewListResponse.of(reviewResponsePage, averageRating, totalReviews);
            
        } catch (IllegalArgumentException e) {
            log.error("잘못된 필터 값: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 필터 값입니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("필터링된 상품 리뷰 목록 조회 중 오류 발생: 상품ID={}, 에러={}", productId, e.getMessage(), e);
            throw new RuntimeException("필터링된 리뷰 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 리뷰 상세 조회
     */
    public ReviewResponse getReview(Long reviewId) {
        log.info("리뷰 상세 조회: ID={}", reviewId);

        Review review = reviewRepository.findByIdWithUserProfile(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다."));

        if (!review.isActive()) {
            throw new ReviewNotFoundException("삭제되었거나 숨김 처리된 리뷰입니다.");
        }

        return createReviewResponseSafely(review);
    }

    /**
     * 사용자의 리뷰 목록 조회 (마이페이지용)
     */
    public List<ReviewResponse> getUserReviews(Long userId, int page, int size) {
        // TODO: SPRINT 3에서 구현 예정
        log.info("사용자 리뷰 목록 조회 요청: userId={}, page={}, size={}", userId, page, size);
        return List.of(); // 임시 반환
    }

    /**
     * 리뷰 수정 가능 여부 확인
     */
    public boolean canUpdateReview(Long reviewId, Long userId) {
        try {
            Review review = reviewRepository.findById(reviewId).orElse(null);
            if (review == null) {
                return false;
            }
            return review.canBeUpdatedBy(userId);
        } catch (Exception e) {
            log.error("리뷰 수정 가능 여부 확인 실패: reviewId={}, userId={}", reviewId, userId, e);
            return false;

        }
    }

    /**
     * 사용자별 삭제된 리뷰 목록 조회
     */
    public List<ReviewResponse> getUserDeletedReviews(Long userId) {
        log.info("사용자 삭제된 리뷰 목록 조회: userId={}", userId);
        
        List<Review> deletedReviews = reviewRepository.findDeletedReviewsByUserId(userId);
        
        List<ReviewResponse> responses = deletedReviews.stream()
                .map(this::createReviewResponseSafely)
                .toList();
        
        log.info("사용자 삭제된 리뷰 조회 완료: userId={}, 개수={}", userId, responses.size());
        
        return responses;
    }

    /**
     * 특정 기간 내 삭제된 리뷰들 조회 (관리자용)
     */
    public List<ReviewResponse> getDeletedReviewsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("기간별 삭제된 리뷰 조회: {} ~ {}", startDate, endDate);
        
        List<Review> deletedReviews = reviewRepository.findDeletedReviewsBetween(startDate, endDate);
        
        List<ReviewResponse> responses = deletedReviews.stream()
                .map(this::createReviewResponseSafely)
                .toList();
        
        log.info("기간별 삭제된 리뷰 조회 완료: 기간={} ~ {}, 개수={}", 
                startDate, endDate, responses.size());
        
        return responses;
    }

    /**
     * 상품별 리뷰 통계 조회 (활성/삭제/전체 개수)
     */
    public ReviewStatsResponse getProductReviewStats(Long productId) {
        log.info("상품 리뷰 통계 조회: productId={}", productId);
        
        Long activeCount = reviewRepository.countActiveReviewsByProductId(productId);
        Long deletedCount = reviewRepository.countDeletedReviewsByProductId(productId);
        Long totalCount = reviewRepository.countAllReviewsByProductId(productId);
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);
        
        if (averageRating == null) {
            averageRating = 0.0;
        }
        
        // 삭제율 계산
        double deletionRate = totalCount > 0 ? (double) deletedCount / totalCount * 100 : 0.0;
        
        ReviewStatsResponse response = ReviewStatsResponse.builder()
                .productId(productId)
                .activeReviewCount(activeCount)
                .deletedReviewCount(deletedCount)
                .totalReviewCount(totalCount)
                .averageRating(averageRating)
                .deletionRate(deletionRate)
                .generatedAt(LocalDateTime.now())
                .build();
        
        log.info("상품 리뷰 통계 조회 완료: productId={}, active={}, deleted={}, total={}, avg={}", 
                productId, activeCount, deletedCount, totalCount, averageRating);
        
        return response;
    }

    /**
     * 사용자별 삭제된 리뷰 개수 조회
     */
    public Long getUserDeletedReviewCount(Long userId) {
        log.info("사용자 삭제된 리뷰 개수 조회: userId={}", userId);
        
        Long deletedCount = reviewRepository.countDeletedReviewsByUserId(userId);
        
        log.info("사용자 삭제된 리뷰 개수 조회 완료: userId={}, 삭제된 개수={}", userId, deletedCount);
        
        return deletedCount;
    }

    /**
     * 최근 30일간 삭제된 리뷰 통계
     */
    public PeriodReviewStatsResponse getRecentDeletedReviewStats() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        
        return getDeletedReviewStatsBetween(startDate, endDate);
    }

    /**
     * 특정 기간 삭제된 리뷰 통계
     */
    public PeriodReviewStatsResponse getDeletedReviewStatsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("기간별 삭제된 리뷰 통계 조회: {} ~ {}", startDate, endDate);
        
        List<Review> deletedReviews = reviewRepository.findDeletedReviewsBetween(startDate, endDate);
        
        long totalDeleted = deletedReviews.size();
        long uniqueUsers = deletedReviews.stream()
                .mapToLong(review -> review.getUser().getId())
                .distinct()
                .count();
        long uniqueProducts = deletedReviews.stream()
                .mapToLong(review -> review.getProduct().getProductId())
                .distinct()
                .count();
        
        double averageRatingOfDeleted = deletedReviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        
        PeriodReviewStatsResponse response = PeriodReviewStatsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalDeletedCount(totalDeleted)
                .uniqueUserCount(uniqueUsers)
                .uniqueProductCount(uniqueProducts)
                .averageRatingOfDeleted(averageRatingOfDeleted)
                .deletedReviews(deletedReviews.stream()
                        .map(review -> PeriodReviewStatsResponse.DeletedReviewSummary.builder()
                                .reviewId(review.getReviewId())
                                .userId(review.getUser().getId())
                                .productId(review.getProduct().getProductId())
                                .rating(review.getRating())
                                .title(review.getTitle())
                                .deletedAt(review.getUpdatedAt())
                                .build())
                        .toList())
                .build();
        
        log.info("기간별 삭제된 리뷰 통계 조회 완료: 기간={} ~ {}, 총 삭제={}, 사용자={}, 상품={}", 
                startDate, endDate, totalDeleted, uniqueUsers, uniqueProducts);
        
        return response;
    }

    /**
     * 리뷰 목록을 응답으로 변환하는 메서드
     */
    private List<ReviewResponse> convertReviewsToResponses(List<Review> reviews) {
        return reviews.stream()
                .map(this::createReviewResponseSafely)
                .toList();
    }

    /**
     * 단일 리뷰를 안전하게 응답으로 변환
     */
    private ReviewResponse createReviewResponseSafely(Review review) {
        List<ReviewImageResponse> images = List.of(); // 기본값

        // 이미지 조회 시도
        try {
            images = reviewImageService.getReviewImages(review.getReviewId());
            log.info("✅ 리뷰 이미지 조회 성공: reviewId={}, 이미지 수={}", review.getReviewId(), images.size());
            
            // 이미지 URL 로깅
            if (!images.isEmpty()) {
                images.forEach(image -> 
                    log.info("🖼️ 최종 이미지 URL: reviewImageId={}, url={}", image.getReviewImageId(), image.getImageUrl())
                );
            }
        } catch (Exception e) {
            log.warn("이미지 조회 실패, 빈 리스트 사용: reviewId={}, 에러={}", review.getReviewId(), e.getMessage());
        }

        // 응답 생성 시도
        try {
            return ReviewResponse.from(review, images);
        } catch (Exception e) {
            log.warn("이미지 포함 응답 생성 실패, 기본 응답 생성: reviewId={}, 에러={}", review.getReviewId(), e.getMessage());
            return ReviewResponse.from(review);
        }
    }

    // ============= 필요한 응답 DTO들 (내부 클래스) =============

    /**
     * 상품별 리뷰 통계 응답 DTO
     */
    @lombok.Getter
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ReviewStatsResponse {
        private Long productId;
        private Long activeReviewCount;
        private Long deletedReviewCount;
        private Long totalReviewCount;
        private Double averageRating;
        private Double deletionRate;
        private LocalDateTime generatedAt;
    }

    /**
     * 기간별 리뷰 통계 응답 DTO
     */
    @lombok.Getter
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PeriodReviewStatsResponse {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Long totalDeletedCount;
        private Long uniqueUserCount;
        private Long uniqueProductCount;
        private Double averageRatingOfDeleted;
        private List<DeletedReviewSummary> deletedReviews;

        @lombok.Getter
        @lombok.Builder
        @lombok.AllArgsConstructor
        @lombok.NoArgsConstructor
        public static class DeletedReviewSummary {
            private Long reviewId;
            private Long userId;
            private Long productId;
            private Integer rating;
            private String title;
            private LocalDateTime deletedAt;
        }
    }
}