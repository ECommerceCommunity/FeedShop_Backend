package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 삭제/통계 기능 테스트")
class ReviewDeleteServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("사용자 삭제된 리뷰 개수 조회 - 성공")
    void getUserDeletedReviewCount_Success() {
        // Given
        Long userId = 1L;
        Long expectedCount = 5L;

        given(reviewRepository.countDeletedReviewsByUserId(userId)).willReturn(expectedCount);

        // When
        Long result = reviewService.getUserDeletedReviewCount(userId);

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(reviewRepository).countDeletedReviewsByUserId(userId);
    }

    @Test
    @DisplayName("상품 리뷰 통계 조회 - 성공")
    void getProductReviewStats_Success() {
        // Given
        Long productId = 1L;
        Long activeCount = 10L;
        Long deletedCount = 3L;
        Long totalCount = 13L;
        Double averageRating = 4.2;

        given(reviewRepository.countActiveReviewsByProductId(productId)).willReturn(activeCount);
        given(reviewRepository.countDeletedReviewsByProductId(productId)).willReturn(deletedCount);
        given(reviewRepository.countAllReviewsByProductId(productId)).willReturn(totalCount);
        given(reviewRepository.findAverageRatingByProductId(productId)).willReturn(averageRating);

        // When
        ReviewService.ReviewStatsResponse response = reviewService.getProductReviewStats(productId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(productId);
        assertThat(response.getActiveReviewCount()).isEqualTo(activeCount);
        assertThat(response.getDeletedReviewCount()).isEqualTo(deletedCount);
        assertThat(response.getTotalReviewCount()).isEqualTo(totalCount);
        assertThat(response.getAverageRating()).isEqualTo(averageRating);
        assertThat(response.getDeletionRate()).isEqualTo(23.076923076923077); // 3/13 * 100

        verify(reviewRepository).countActiveReviewsByProductId(productId);
        verify(reviewRepository).countDeletedReviewsByProductId(productId);
        verify(reviewRepository).countAllReviewsByProductId(productId);
        verify(reviewRepository).findAverageRatingByProductId(productId);
    }

    @Test
    @DisplayName("기간별 삭제된 리뷰 통계 조회 - 성공")
    void getDeletedReviewStatsBetween_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        
        // Mock Review 객체들 생성
        com.cMall.feedShop.review.domain.Review mockReview1 = mock(com.cMall.feedShop.review.domain.Review.class);
        com.cMall.feedShop.review.domain.Review mockReview2 = mock(com.cMall.feedShop.review.domain.Review.class);
        
        given(mockReview1.getUser()).willReturn(mock(com.cMall.feedShop.user.domain.model.User.class));
        given(mockReview1.getProduct()).willReturn(mock(com.cMall.feedShop.product.domain.model.Product.class));
        given(mockReview1.getUser().getId()).willReturn(1L);
        given(mockReview1.getProduct().getProductId()).willReturn(1L);
        
        given(mockReview2.getUser()).willReturn(mock(com.cMall.feedShop.user.domain.model.User.class));
        given(mockReview2.getProduct()).willReturn(mock(com.cMall.feedShop.product.domain.model.Product.class));
        given(mockReview2.getUser().getId()).willReturn(2L);
        given(mockReview2.getProduct().getProductId()).willReturn(2L);
        
        List<com.cMall.feedShop.review.domain.Review> deletedReviews = Arrays.asList(mockReview1, mockReview2);

        given(reviewRepository.findDeletedReviewsBetween(startDate, endDate)).willReturn(deletedReviews);

        // When
        ReviewService.PeriodReviewStatsResponse response = reviewService.getDeletedReviewStatsBetween(startDate, endDate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStartDate()).isEqualTo(startDate);
        assertThat(response.getEndDate()).isEqualTo(endDate);
        assertThat(response.getTotalDeletedCount()).isEqualTo(2L);
        assertThat(response.getUniqueUserCount()).isEqualTo(2L);
        assertThat(response.getUniqueProductCount()).isEqualTo(2L);

        verify(reviewRepository).findDeletedReviewsBetween(startDate, endDate);
    }

}