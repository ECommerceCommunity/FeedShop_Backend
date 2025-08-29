package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.request.ReviewUpdateRequest;
import com.cMall.feedShop.review.application.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 서비스 Facade
 * 기존 ReviewService의 모든 기능을 CRUD별 서비스로 위임하여 처리
 * 기존 코드와의 호환성을 유지하면서 서비스를 분리한 구조
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewCreateService reviewCreateService;
    private final ReviewReadService reviewReadService;
    private final ReviewUpdateService reviewUpdateService;
    private final ReviewDeleteService reviewDeleteService;

    // =================== CREATE 관련 메서드들 ===================

    /**
     * 리뷰 생성 (DTO 불변성 적용)
     *
     * @param request 불변 리뷰 생성 요청 DTO
     * @param images 업로드할 이미지 파일들 (별도 파라미터)
     * @return 생성된 리뷰 응답
     */
    @Transactional
    public ReviewCreateResponse createReview(ReviewCreateRequest request, List<MultipartFile> images) {
        log.info("Facade: 리뷰 생성 요청 위임 - CreateService로 전달");
        return reviewCreateService.createReview(request, images);
    }

    // =================== READ 관련 메서드들 ===================

    /**
     * 상품별 리뷰 목록 조회
     */
    public ReviewListResponse getProductReviews(Long productId, int page, int size, String sort) {
        log.info("Facade: 상품별 리뷰 목록 조회 요청 위임 - ReadService로 전달");
        return reviewReadService.getProductReviews(productId, page, size, sort);
    }

    /**
     * 필터링이 적용된 상품별 리뷰 목록 조회
     */
    public ReviewListResponse getProductReviewsWithFilters(Long productId, int page, int size, String sort, 
                                                          Integer rating, String sizeFit, String cushion, String stability) {
        log.info("Facade: 필터링된 상품별 리뷰 목록 조회 요청 위임 - ReadService로 전달");
        return reviewReadService.getProductReviewsWithFilters(productId, page, size, sort, rating, sizeFit, cushion, stability);
    }

    /**
     * 리뷰 상세 조회
     */
    public ReviewResponse getReview(Long reviewId) {
        log.info("Facade: 리뷰 상세 조회 요청 위임 - ReadService로 전달");
        return reviewReadService.getReview(reviewId);
    }

    /**
     * 사용자의 리뷰 목록 조회 (마이페이지용)
     */
    public List<ReviewResponse> getUserReviews(Long userId, int page, int size) {
        log.info("Facade: 사용자 리뷰 목록 조회 요청 위임 - ReadService로 전달");
        return reviewReadService.getUserReviews(userId, page, size);
    }

    /**
     * 리뷰 수정 가능 여부 확인
     */
    public boolean canUpdateReview(Long reviewId, Long userId) {
        log.debug("Facade: 리뷰 수정 가능 여부 확인 요청 위임 - ReadService로 전달");
        return reviewReadService.canUpdateReview(reviewId, userId);
    }

    /**
     * 사용자별 삭제된 리뷰 목록 조회
     */
    public List<ReviewResponse> getUserDeletedReviews(Long userId) {
        log.info("Facade: 사용자 삭제된 리뷰 목록 조회 요청 위임 - ReadService로 전달");
        return reviewReadService.getUserDeletedReviews(userId);
    }

    /**
     * 특정 기간 내 삭제된 리뷰들 조회 (관리자용)
     */
    public List<ReviewResponse> getDeletedReviewsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Facade: 기간별 삭제된 리뷰 조회 요청 위임 - ReadService로 전달");
        return reviewReadService.getDeletedReviewsBetween(startDate, endDate);
    }

    /**
     * 상품별 리뷰 통계 조회 (활성/삭제/전체 개수)
     */
    public ReviewReadService.ReviewStatsResponse getProductReviewStats(Long productId) {
        log.info("Facade: 상품 리뷰 통계 조회 요청 위임 - ReadService로 전달");
        return reviewReadService.getProductReviewStats(productId);
    }

    /**
     * 사용자별 삭제된 리뷰 개수 조회
     */
    public Long getUserDeletedReviewCount(Long userId) {
        log.info("Facade: 사용자 삭제된 리뷰 개수 조회 요청 위임 - ReadService로 전달");
        return reviewReadService.getUserDeletedReviewCount(userId);
    }

    /**
     * 최근 30일간 삭제된 리뷰 통계
     */
    public ReviewReadService.PeriodReviewStatsResponse getRecentDeletedReviewStats() {
        log.info("Facade: 최근 삭제된 리뷰 통계 조회 요청 위임 - ReadService로 전달");
        return reviewReadService.getRecentDeletedReviewStats();
    }

    /**
     * 특정 기간 삭제된 리뷰 통계
     */
    public ReviewReadService.PeriodReviewStatsResponse getDeletedReviewStatsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Facade: 기간별 삭제된 리뷰 통계 조회 요청 위임 - ReadService로 전달");
        return reviewReadService.getDeletedReviewStatsBetween(startDate, endDate);
    }

    // =================== UPDATE 관련 메서드들 ===================

    /**
     * 리뷰 수정 (이미지 포함) - DTO 불변성 적용
     *
     * @param reviewId 수정할 리뷰 ID
     * @param request 불변 리뷰 수정 요청 DTO
     * @param newImages 새로 추가할 이미지들 (별도 파라미터)
     * @return 수정 결과 응답
     */
    @Transactional
    public ReviewUpdateResponse updateReview(Long reviewId, ReviewUpdateRequest request,
                                             List<MultipartFile> newImages) {
        log.info("Facade: 리뷰 수정 요청 위임 - UpdateService로 전달");
        return reviewUpdateService.updateReview(reviewId, request, newImages);
    }

    /**
     * 리뷰 수정 (간단 버전 - 이미지 없이)
     */
    @Transactional
    public void updateReviewSimple(Long reviewId, ReviewUpdateRequest request) {
        log.info("Facade: 리뷰 간단 수정 요청 위임 - UpdateService로 전달");
        reviewUpdateService.updateReviewSimple(reviewId, request);
    }

    /**
     * 리뷰 제목만 수정
     */
    @Transactional
    public void updateReviewTitle(Long reviewId, String newTitle) {
        log.info("Facade: 리뷰 제목 수정 요청 위임 - UpdateService로 전달");
        reviewUpdateService.updateReviewTitle(reviewId, newTitle);
    }

    /**
     * 리뷰 평점만 수정
     */
    @Transactional
    public void updateReviewRating(Long reviewId, Integer newRating) {
        log.info("Facade: 리뷰 평점 수정 요청 위임 - UpdateService로 전달");
        reviewUpdateService.updateReviewRating(reviewId, newRating);
    }

    /**
     * 리뷰 내용만 수정
     */
    @Transactional
    public void updateReviewContent(Long reviewId, String newContent) {
        log.info("Facade: 리뷰 내용 수정 요청 위임 - UpdateService로 전달");
        reviewUpdateService.updateReviewContent(reviewId, newContent);
    }

    // =================== DELETE 관련 메서드들 ===================

    /**
     * 리뷰 전체 삭제 (리뷰 + 모든 이미지)
     */
    @Transactional
    public ReviewDeleteResponse deleteReview(Long reviewId) {
        log.info("Facade: 리뷰 전체 삭제 요청 위임 - DeleteService로 전달");
        return reviewDeleteService.deleteReview(reviewId);
    }
    
    /**
     * 리뷰 이미지 일괄 삭제
     */
    @Transactional
    public ReviewImageDeleteResponse deleteReviewImages(Long reviewId, List<Long> imageIds) {
        log.info("Facade: 리뷰 이미지 일괄 삭제 요청 위임 - DeleteService로 전달");
        return reviewDeleteService.deleteReviewImages(reviewId, imageIds);
    }
    
    /**
     * 리뷰 이미지 개별 삭제
     */
    @Transactional
    public ReviewImageDeleteResponse deleteReviewImage(Long reviewId, Long imageId) {
        log.info("Facade: 리뷰 이미지 개별 삭제 요청 위임 - DeleteService로 전달");
        return reviewDeleteService.deleteReviewImage(reviewId, imageId);
    }
    
    /**
     * 리뷰의 모든 이미지 삭제 (리뷰 텍스트는 유지)
     */
    @Transactional
    public ReviewImageDeleteResponse deleteAllReviewImages(Long reviewId) {
        log.info("Facade: 리뷰 모든 이미지 삭제 요청 위임 - DeleteService로 전달");
        return reviewDeleteService.deleteAllReviewImages(reviewId);
    }
}