package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;

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
        return reviewJpaRepository.findActiveReviewsByProductId(productId, pageable);
    }

    @Override
    public Page<Review> findActiveReviewsByProductIdOrderByPoints(Long productId, Pageable pageable) {
        return reviewJpaRepository.findActiveReviewsByProductIdOrderByPoints(productId, pageable);
    }

    @Override
    public Double findAverageRatingByProductId(Long productId) {
        return reviewJpaRepository.findAverageRatingByProductId(productId);
    }

    @Override
    public Long countActiveReviewsByProductId(Long productId) {
        return reviewJpaRepository.countActiveReviewsByProductId(productId);
    }
}