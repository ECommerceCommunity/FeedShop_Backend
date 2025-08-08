package com.cMall.feedShop.review.domain.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;

import java.util.List;

/**
 * Domain Layer의 ReviewImage Repository 인터페이스
 * 비즈니스 로직에 필요한 메서드들만 정의
 */
public interface ReviewImageRepository {

    /**
     * 리뷰의 활성 이미지들을 순서대로 조회 (Review 객체로)
     */
    List<ReviewImage> findActiveImagesByReview(Review review);

    /**
     * 리뷰의 활성 이미지들을 조회 (순서 무관)
     */
    List<ReviewImage> findByReviewAndIsDeletedFalse(Review review);

    /**
     * 리뷰 ID로 활성 이미지들을 순서대로 조회
     */
    List<ReviewImage> findActiveImagesByReviewId(Long reviewId);

    /**
     * 리뷰의 활성 이미지 개수 조회
     */
    Long countActiveImagesByReviewId(Long reviewId);

    /**
     * 여러 리뷰의 활성 이미지들을 한 번에 조회 (N+1 문제 해결)
     */
    List<ReviewImage> findActiveImagesByReviewIds(List<Long> reviewIds);

    /**
     * 기본 CRUD 메서드들
     */
    ReviewImage save(ReviewImage reviewImage);
    List<ReviewImage> saveAll(Iterable<ReviewImage> reviewImages);
    void delete(ReviewImage reviewImage);
    void deleteAll(Iterable<ReviewImage> reviewImages);

    /**
     * 특정 리뷰의 특정 이미지들만 조회 (성능 최적화)
     * @param reviewId 리뷰 ID
     * @param imageIds 조회할 이미지 ID 목록
     * @return 조건에 맞는 활성 이미지 목록
     */
    List<ReviewImage> findActiveImagesByReviewIdAndImageIds(Long reviewId, List<Long> imageIds);
}