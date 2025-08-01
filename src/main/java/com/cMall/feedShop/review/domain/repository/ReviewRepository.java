// 1. ReviewRepository 인터페이스 (전체)
package com.cMall.feedShop.review.domain.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
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

    // ========== 새로 추가: 3요소 통계를 위한 메서드들 ==========
    Map<Cushion, Long> getCushionDistributionByProductId(Long productId);
    Map<SizeFit, Long> getSizeFitDistributionByProductId(Long productId);
    Map<Stability, Long> getStabilityDistributionByProductId(Long productId);
}