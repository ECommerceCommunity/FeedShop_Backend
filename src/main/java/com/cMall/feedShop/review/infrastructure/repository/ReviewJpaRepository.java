package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    // 상품별 활성 리뷰 조회 (최신순)
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.isBlinded = false ORDER BY r.createdAt DESC")
    Page<Review> findActiveReviewsByProductId(@Param("productId") Long productId, Pageable pageable);

    // 상품별 활성 리뷰 조회 (점수순)
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.isBlinded = false ORDER BY r.points DESC, r.createdAt DESC")
    Page<Review> findActiveReviewsByProductIdOrderByPoints(@Param("productId") Long productId, Pageable pageable);

    // 상품별 평균 평점
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.isBlinded = false")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    // 상품별 리뷰 개수
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.isBlinded = false")
    Long countActiveReviewsByProductId(@Param("productId") Long productId);
}