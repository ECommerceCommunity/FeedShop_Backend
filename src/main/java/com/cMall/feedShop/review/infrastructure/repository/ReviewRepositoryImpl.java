package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewQueryRepository reviewQueryRepository;

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(review);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        return reviewJpaRepository.findById(reviewId);
    }

    @Override
    public void delete(Review review) {
        reviewJpaRepository.delete(review);
    }

    @Override
    public Page<Review> findActiveReviewsByProductId(Long productId, Pageable pageable) {
        return reviewQueryRepository.findActiveReviewsByProductId(productId, pageable);
    }

    @Override
    public Page<Review> findActiveReviewsByProductIdOrderByPoints(Long productId, Pageable pageable) {
        return reviewQueryRepository.findActiveReviewsByProductIdOrderByPoints(productId, pageable);
    }

    @Override
    public Double findAverageRatingByProductId(Long productId) {
        return reviewQueryRepository.findAverageRatingByProductId(productId);
    }

    @Override
    public Long countActiveReviewsByProductId(Long productId) {
        return reviewQueryRepository.countActiveReviewsByProductId(productId);
    }

    @Override
    public Map<Cushion, Long> getCushionDistributionByProductId(Long productId) {
        return reviewQueryRepository.getCushionDistributionByProductId(productId);
    }

    @Override
    public Map<SizeFit, Long> getSizeFitDistributionByProductId(Long productId) {
        return reviewQueryRepository.getSizeFitDistributionByProductId(productId);
    }

    @Override
    public Map<Stability, Long> getStabilityDistributionByProductId(Long productId) {
        return reviewQueryRepository.getStabilityDistributionByProductId(productId);
    }
    @Override
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        return reviewQueryRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean existsActiveReviewByUserIdAndProductId(Long userId, Long productId) {
        return reviewQueryRepository.existsActiveReviewByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<Review> findDeletedReviewsByUserId(Long userId) {
        return reviewQueryRepository.findDeletedReviewsByUserId(userId);
    }

    @Override
    public List<Review> findDeletedReviewsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return reviewQueryRepository.findDeletedReviewsBetween(startDate, endDate);
    }

    @Override
    public Long countDeletedReviewsByProductId(Long productId) {
        return reviewQueryRepository.countDeletedReviewsByProductId(productId);
    }

    @Override
    public Long countAllReviewsByProductId(Long productId) {
        return reviewQueryRepository.countAllReviewsByProductId(productId);
    }

    @Override
    public Long countDeletedReviewsByUserId(Long userId) {
        return reviewQueryRepository.countDeletedReviewsByUserId(userId);
    }
}