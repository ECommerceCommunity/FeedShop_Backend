package com.cMall.feedShop.review.domain.repository;

import com.cMall.feedShop.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewRepository {

    // 기본 CRUD
    Review save(Review review);
    Optional<Review> findById(Long reviewId);
    void delete(Review review);

    // 상품별 활성 리뷰 조회 (최신순)
    Page<Review> findActiveReviewsByProductId(Long productId, Pageable pageable);

    // 상품별 활성 리뷰 조회 (점수순)
    Page<Review> findActiveReviewsByProductIdOrderByPoints(Long productId, Pageable pageable);

    // 상품별 평균 평점
    Double findAverageRatingByProductId(Long productId);

    // 상품별 리뷰 개수
    Long countActiveReviewsByProductId(Long productId);
}