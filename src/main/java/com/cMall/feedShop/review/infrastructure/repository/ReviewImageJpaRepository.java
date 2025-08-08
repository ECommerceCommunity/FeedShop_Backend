package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewImageJpaRepository extends JpaRepository<ReviewImage, Long> {

    /**
     * 리뷰 ID로 활성 이미지들을 조회 (순서 무관)
     */
    @Query("SELECT ri FROM ReviewImage ri WHERE ri.review.reviewId = :reviewId AND ri.isDeleted = false")
    List<ReviewImage> findActiveImagesByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 리뷰 ID로 활성 이미지들을 순서대로 조회
     */
    @Query("SELECT ri FROM ReviewImage ri WHERE ri.review.reviewId = :reviewId AND ri.isDeleted = false ORDER BY ri.imageOrder ASC")
    List<ReviewImage> findActiveImagesByReviewIdOrderByImageOrder(@Param("reviewId") Long reviewId);

    /**
     * 리뷰의 활성 이미지 개수 조회 - Long 타입으로 통일
     */
    @Query("SELECT COUNT(ri) FROM ReviewImage ri WHERE ri.review.reviewId = :reviewId AND ri.isDeleted = false")
    Long countActiveImagesByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 🔥 추가: Review 객체로 활성 이미지들을 순서대로 조회
     */
    @Query("SELECT ri FROM ReviewImage ri WHERE ri.review = :review AND ri.isDeleted = false ORDER BY ri.imageOrder ASC")
    List<ReviewImage> findActiveImagesByReview(@Param("review") Review review);

    /**
     * 🔥 추가: Review 객체로 활성 이미지들을 조회 (순서 무관)
     */
    @Query("SELECT ri FROM ReviewImage ri WHERE ri.review = :review AND ri.isDeleted = false")
    List<ReviewImage> findByReviewAndIsDeletedFalse(@Param("review") Review review);

    /**
     * 🔥 추가: 여러 리뷰의 활성 이미지들을 한 번에 조회 (N+1 문제 해결)
     */
    @Query("SELECT ri FROM ReviewImage ri WHERE ri.review.reviewId IN :reviewIds AND ri.isDeleted = false ORDER BY ri.review.reviewId, ri.imageOrder ASC")
    List<ReviewImage> findActiveImagesByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    @Query("SELECT ri FROM ReviewImage ri WHERE ri.review.reviewId = :reviewId " +
            "AND ri.reviewImageId IN :imageIds AND ri.isDeleted = false " +
            "ORDER BY ri.imageOrder ASC")
    List<ReviewImage> findActiveImagesByReviewIdAndImageIds(
            @Param("reviewId") Long reviewId,
            @Param("imageIds") List<Long> imageIds);

}