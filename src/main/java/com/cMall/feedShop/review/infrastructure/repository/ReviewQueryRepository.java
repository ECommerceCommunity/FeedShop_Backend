package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReviewQueryRepository {
    // 상품별 활성 리뷰 조회 (최신순)
    Page<Review> findActiveReviewsByProductId(Long productId, Pageable pageable);

    // 상품별 활성 리뷰 조회 (점수순)
    Page<Review> findActiveReviewsByProductIdOrderByPoints(Long productId, Pageable pageable);

    // 상품별 평균 평점
    Double findAverageRatingByProductId(Long productId);

    // 상품별 리뷰 개수
    Long countActiveReviewsByProductId(Long productId);

    // 3요소 통계를 위한 메서드들
    Map<Cushion, Long> getCushionDistributionByProductId(Long productId);
    Map<SizeFit, Long> getSizeFitDistributionByProductId(Long productId);
    Map<Stability, Long> getStabilityDistributionByProductId(Long productId);

    /**
     * 사용자가 특정 상품에 대해 이미 리뷰를 작성했는지 확인
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * 사용자가 특정 상품에 대해 작성한 활성 리뷰가 있는지 확인
     */
    boolean existsActiveReviewByUserIdAndProductId(Long userId, Long productId);

    // ========== 삭제 관련 통계 메서드들 ==========
    
    /**
     * 사용자가 삭제한 리뷰 목록 조회
     */
    List<Review> findDeletedReviewsByUserId(Long userId);
    
    /**
     * 특정 기간 내에 삭제된 리뷰들 조회
     */
    List<Review> findDeletedReviewsBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 상품별 삭제된 리뷰 개수
     */
    Long countDeletedReviewsByProductId(Long productId);
    
    /**
     * 상품별 전체 리뷰 개수 (활성 + 삭제)
     */
    Long countAllReviewsByProductId(Long productId);
    
    /**
     * 사용자별 삭제된 리뷰 개수
     */
    Long countDeletedReviewsByUserId(Long userId);
}