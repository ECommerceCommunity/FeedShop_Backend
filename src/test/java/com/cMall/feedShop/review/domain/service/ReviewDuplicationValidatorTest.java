package com.cMall.feedShop.review.domain.service;

import com.cMall.feedShop.review.domain.exception.DuplicateReviewException;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 중복 검증 테스트")
class ReviewDuplicationValidatorTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewDuplicationValidator duplicationValidator;

    private Long userId;
    private Long productId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        productId = 1L;
    }

    @Test
    @DisplayName("활성 중복 리뷰가 없으면 검증을 통과한다")
    void validateNoDuplicateActiveReview_Success() {
        // given
        given(reviewRepository.existsActiveReviewByUserIdAndProductId(userId, productId))
                .willReturn(false);

        // when & then
        duplicationValidator.validateNoDuplicateActiveReview(userId, productId);
        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("활성 중복 리뷰가 있으면 예외가 발생한다")
    void validateNoDuplicateActiveReview_ThrowsException() {
        // given
        given(reviewRepository.existsActiveReviewByUserIdAndProductId(userId, productId))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> duplicationValidator.validateNoDuplicateActiveReview(userId, productId))
                .isInstanceOf(DuplicateReviewException.class)
                .hasMessageContaining("상품 ID 1에 대한 리뷰를 이미 작성하셨습니다");
    }

    @Test
    @DisplayName("중복 리뷰 여부를 정확히 반환한다")
    void hasActiveReview() {
        // given
        given(reviewRepository.existsActiveReviewByUserIdAndProductId(userId, productId))
                .willReturn(true);

        // when
        boolean result = duplicationValidator.hasActiveReview(userId, productId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("모든 상태 중복 리뷰 검증이 올바르게 작동한다")
    void validateNoDuplicateReview() {
        // given
        given(reviewRepository.existsByUserIdAndProductId(userId, productId))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> duplicationValidator.validateNoDuplicateReview(userId, productId))
                .isInstanceOf(DuplicateReviewException.class)
                .hasMessageContaining("상품 ID 1에 대한 리뷰를 이미 작성하셨습니다");
    }

    @Test
    @DisplayName("활성 리뷰가 없으면 false를 반환한다")
    void hasActiveReview_False() {
        // given
        given(reviewRepository.existsActiveReviewByUserIdAndProductId(userId, productId))
                .willReturn(false);

        // when
        boolean result = duplicationValidator.hasActiveReview(userId, productId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("모든 상태 중복 리뷰가 없으면 검증을 통과한다")
    void validateNoDuplicateReview_Success() {
        // given
        given(reviewRepository.existsByUserIdAndProductId(userId, productId))
                .willReturn(false);

        // when & then
        duplicationValidator.validateNoDuplicateReview(userId, productId);
        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("다른 사용자의 리뷰는 중복으로 간주하지 않는다")
    void validateNoDuplicateActiveReview_DifferentUser() {
        // given
        Long otherUserId = 2L;
        given(reviewRepository.existsActiveReviewByUserIdAndProductId(otherUserId, productId))
                .willReturn(false);

        // when & then
        duplicationValidator.validateNoDuplicateActiveReview(otherUserId, productId);
        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("다른 상품의 리뷰는 중복으로 간주하지 않는다")
    void validateNoDuplicateActiveReview_DifferentProduct() {
        // given
        Long otherProductId = 2L;
        given(reviewRepository.existsActiveReviewByUserIdAndProductId(userId, otherProductId))
                .willReturn(false);

        // when & then
        duplicationValidator.validateNoDuplicateActiveReview(userId, otherProductId);
        // 예외가 발생하지 않으면 성공
    }
}