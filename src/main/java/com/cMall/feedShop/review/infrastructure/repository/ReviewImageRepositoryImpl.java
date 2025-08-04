package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.ReviewImage;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ReviewImageRepository의 JPA 구현체
 * Domain Repository 인터페이스를 JpaRepository로 구현
 */
@Repository
@RequiredArgsConstructor
public class ReviewImageRepositoryImpl implements ReviewImageRepository {

    private final ReviewImageJpaRepository jpaRepository;

    @Override
    public List<ReviewImage> findActiveImagesByReview(Review review) {
        return jpaRepository.findActiveImagesByReview(review);
    }

    @Override
    public List<ReviewImage> findByReviewAndIsDeletedFalse(Review review) {
        return jpaRepository.findByReviewAndIsDeletedFalse(review);
    }

    @Override
    public List<ReviewImage> findActiveImagesByReviewId(Long reviewId) {
        return jpaRepository.findActiveImagesByReviewId(reviewId);
    }

    @Override
    public Long countActiveImagesByReviewId(Long reviewId) {
        return jpaRepository.countActiveImagesByReviewId(reviewId);
    }

    @Override
    public List<ReviewImage> findActiveImagesByReviewIds(List<Long> reviewIds) {
        return jpaRepository.findActiveImagesByReviewIds(reviewIds);
    }

    @Override
    public ReviewImage save(ReviewImage reviewImage) {
        return jpaRepository.save(reviewImage);
    }

    @Override
    public List<ReviewImage> saveAll(Iterable<ReviewImage> reviewImages) {
        return jpaRepository.saveAll(reviewImages);
    }

    @Override
    public void delete(ReviewImage reviewImage) {
        jpaRepository.delete(reviewImage);
    }

    @Override
    public void deleteAll(Iterable<ReviewImage> reviewImages) {
        jpaRepository.deleteAll(reviewImages);
    }
}