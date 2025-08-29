package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewListResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 테스트 (CRUD 서비스 Facade)")
class ReviewServiceTest {

    @Mock
    private ReviewCreateService reviewCreateService;
    
    @Mock
    private ReviewReadService reviewReadService;
    
    @Mock
    private ReviewUpdateService reviewUpdateService;
    
    @Mock
    private ReviewDeleteService reviewDeleteService;

    @InjectMocks
    private ReviewService reviewService;

    private ReviewCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = ReviewCreateRequest.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("정말 편하고 좋습니다. 추천해요!")
                .productId(1L)
                .build();
    }

    @Test
    @DisplayName("리뷰를 성공적으로 생성할 수 있다 (Facade 패턴)")
    void createReviewSuccessfully() {
        // given
        ReviewCreateResponse expectedResponse = ReviewCreateResponse.builder()
                .reviewId(1L)
                .message("리뷰가 성공적으로 등록되었습니다.")
                .pointsEarned(100)
                .build();
        
        given(reviewCreateService.createReview(createRequest, null)).willReturn(expectedResponse);

        // when
        ReviewCreateResponse response = reviewService.createReview(createRequest, null);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("리뷰가 성공적으로 등록되었습니다.");
        verify(reviewCreateService).createReview(createRequest, null);
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 리뷰를 생성하려 하면 예외가 발생한다")
    void createReviewWithUnauthenticatedUser() {
        // given
        given(reviewCreateService.createReview(createRequest, null))
                .willThrow(new BusinessException(null, "로그인이 필요합니다"));

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(createRequest, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("로그인이 필요합니다");
        verify(reviewCreateService).createReview(createRequest, null);
    }

    @Test
    @DisplayName("상품별 리뷰 목록을 성공적으로 조회할 수 있다")
    void getProductReviewsSuccessfully() {
        // given
        ReviewListResponse expectedResponse = ReviewListResponse.builder()
                .reviews(List.of())
                .totalElements(1L)
                .averageRating(4.5)
                .totalReviews(10L)
                .build();
        
        given(reviewReadService.getProductReviews(1L, 0, 20, "latest")).willReturn(expectedResponse);

        // when
        ReviewListResponse response = reviewService.getProductReviews(1L, 0, 20, "latest");

        // then
        assertThat(response.getTotalElements()).isEqualTo(1L);
        assertThat(response.getAverageRating()).isEqualTo(4.5);
        assertThat(response.getTotalReviews()).isEqualTo(10L);
        verify(reviewReadService).getProductReviews(1L, 0, 20, "latest");
    }

    @Test
    @DisplayName("리뷰 상세 정보를 성공적으로 조회할 수 있다")
    void getReviewSuccessfully() {
        // given
        ReviewResponse expectedResponse = ReviewResponse.builder()
                .reviewId(1L)
                .title("좋은 신발입니다")
                .rating(5)
                .userName("테스트사용자")
                .build();
        
        given(reviewReadService.getReview(1L)).willReturn(expectedResponse);

        // when
        ReviewResponse response = reviewService.getReview(1L);

        // then
        assertThat(response.getReviewId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("좋은 신발입니다");
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getUserName()).isEqualTo("테스트사용자");
        verify(reviewReadService).getReview(1L);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰를 조회하면 예외가 발생한다")
    void getReviewNotFound() {
        // given
        given(reviewReadService.getReview(999L))
                .willThrow(new ReviewNotFoundException("리뷰를 찾을 수 없습니다"));

        // when & then
        assertThatThrownBy(() -> reviewService.getReview(999L))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("리뷰를 찾을 수 없습니다");
        verify(reviewReadService).getReview(999L);
    }

    @Test
    @DisplayName("리뷰 수정 권한을 확인할 수 있다")
    void canUpdateReview() {
        // given
        given(reviewReadService.canUpdateReview(1L, 1L)).willReturn(true);
        given(reviewReadService.canUpdateReview(1L, 2L)).willReturn(false);

        // when
        boolean canUpdate = reviewService.canUpdateReview(1L, 1L);
        boolean cannotUpdate = reviewService.canUpdateReview(1L, 2L);

        // then
        assertThat(canUpdate).isTrue();
        assertThat(cannotUpdate).isFalse();
        verify(reviewReadService).canUpdateReview(1L, 1L);
        verify(reviewReadService).canUpdateReview(1L, 2L);
    }

    @Test
    @DisplayName("사용자 삭제된 리뷰 개수를 조회할 수 있다")
    void getUserDeletedReviewCount() {
        // given
        given(reviewReadService.getUserDeletedReviewCount(1L)).willReturn(5L);

        // when
        Long count = reviewService.getUserDeletedReviewCount(1L);

        // then
        assertThat(count).isEqualTo(5L);
        verify(reviewReadService).getUserDeletedReviewCount(1L);
    }
}