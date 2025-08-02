package com.cMall.feedShop.review.domain.service;

import com.cMall.feedShop.review.domain.exception.DuplicateReviewException;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewDuplicationValidator {

    private final ReviewRepository reviewRepository;

    /**
     * 활성 상태의 중복 리뷰가 있는지 검증
     * @param userId 사용자 ID
     * @param productId 상품 ID
     * @throws DuplicateReviewException 중복 리뷰가 있는 경우
     */
    public void validateNoDuplicateActiveReview(Long userId, Long productId) {
        if (reviewRepository.existsActiveReviewByUserIdAndProductId(userId, productId)) {
            throw new DuplicateReviewException(productId);
        }
    }

    /**
     * 모든 상태의 중복 리뷰가 있는지 검증 (삭제된 리뷰 포함)
     * @param userId 사용자 ID
     * @param productId 상품 ID
     * @throws DuplicateReviewException 중복 리뷰가 있는 경우
     */
    public void validateNoDuplicateReview(Long userId, Long productId) {
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new DuplicateReviewException(productId);
        }
    }

    /**
     * 중복 리뷰 여부만 확인 (예외 발생 안함)
     * @param userId 사용자 ID
     * @param productId 상품 ID
     * @return 중복 리뷰 존재 여부
     */
    public boolean hasActiveReview(Long userId, Long productId) {
        return reviewRepository.existsActiveReviewByUserIdAndProductId(userId, productId);
    }
}