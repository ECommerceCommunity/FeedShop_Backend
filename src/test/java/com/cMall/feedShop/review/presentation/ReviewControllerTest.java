package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.review.application.dto.response.Review3ElementStatisticsResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewListResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.application.service.Review3ElementStatisticsService;
import com.cMall.feedShop.review.application.service.ReviewService;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.exception.ReviewNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("ReviewController 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private Review3ElementStatisticsService statisticsService;

    @InjectMocks
    private ReviewController reviewController;

    private ObjectMapper objectMapper;
    private ReviewResponse sampleReview;
    private ReviewListResponse sampleReviewList;
    private Review3ElementStatisticsResponse sampleStatistics;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // 샘플 리뷰 데이터 생성
        sampleReview = ReviewResponse.builder()
                .reviewId(1L)
                .productId(100L)
                .userId(10L)
                .userName("테스트유저")
                .title("좋은 신발입니다")
                .rating(5)
                .content("편안하고 쿠션감이 좋습니다. 사이즈는 평소보다 조금 작게 나온 것 같아요.")
                .sizeFit(SizeFit.SMALL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .points(10)
                .hasImages(false)
                .images(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // ReviewListResponse.of() 메서드 사용
        PageImpl<ReviewResponse> reviewPage = new PageImpl<>(Arrays.asList(sampleReview), PageRequest.of(0, 20), 1);
        sampleReviewList = ReviewListResponse.of(reviewPage, 4.5, 150L);

        sampleStatistics = Review3ElementStatisticsResponse.builder()
                .totalReviews(100L)
                .cushionStatistics(createCushionStats())
                .sizeFitStatistics(createSizeFitStats())
                .stabilityStatistics(createStabilityStats())
                .build();
    }

    private Review3ElementStatisticsResponse.SizeFitStatistics createSizeFitStats() {
        Map<SizeFit, Long> distribution = new HashMap<>();
        distribution.put(SizeFit.SMALL, 30L);
        distribution.put(SizeFit.NORMAL, 50L);
        distribution.put(SizeFit.BIG, 20L);

        Map<SizeFit, Double> percentage = new HashMap<>();
        percentage.put(SizeFit.SMALL, 30.0);
        percentage.put(SizeFit.NORMAL, 50.0);
        percentage.put(SizeFit.BIG, 20.0);

        return Review3ElementStatisticsResponse.SizeFitStatistics.builder()
                .distribution(distribution)
                .percentage(percentage)
                .mostSelected(SizeFit.NORMAL)
                .averageScore(3.0)
                .build();
    }

    private Review3ElementStatisticsResponse.CushionStatistics createCushionStats() {
        Map<Cushion, Long> distribution = new HashMap<>();
        distribution.put(Cushion.FIRM, 20L);
        distribution.put(Cushion.MEDIUM, 40L);
        distribution.put(Cushion.SOFT, 40L);

        Map<Cushion, Double> percentage = new HashMap<>();
        percentage.put(Cushion.FIRM, 20.0);
        percentage.put(Cushion.MEDIUM, 40.0);
        percentage.put(Cushion.SOFT, 40.0);

        return Review3ElementStatisticsResponse.CushionStatistics.builder()
                .distribution(distribution)
                .percentage(percentage)
                .mostSelected(Cushion.MEDIUM)
                .averageScore(3.2)
                .build();
    }

    private Review3ElementStatisticsResponse.StabilityStatistics createStabilityStats() {
        Map<Stability, Long> distribution = new HashMap<>();
        distribution.put(Stability.UNSTABLE, 10L);
        distribution.put(Stability.NORMAL, 30L);
        distribution.put(Stability.STABLE, 60L);

        Map<Stability, Double> percentage = new HashMap<>();
        percentage.put(Stability.UNSTABLE, 10.0);
        percentage.put(Stability.NORMAL, 30.0);
        percentage.put(Stability.STABLE, 60.0);

        return Review3ElementStatisticsResponse.StabilityStatistics.builder()
                .distribution(distribution)
                .percentage(percentage)
                .mostSelected(Stability.STABLE)
                .averageScore(4.5)
                .build();
    }

    @Nested
    @DisplayName("상품별 리뷰 목록 조회 API")
    class GetProductReviews {

        @Test
        @DisplayName("성공 - 기본 파라미터로 리뷰 목록 조회")
        void getProductReviews_Success_WithDefaultParams() {
            // given
            Long productId = 100L;
            given(reviewService.getProductReviews(eq(productId), eq(0), eq(20), eq("latest")))
                    .willReturn(sampleReviewList);

            // when
            ApiResponse<ReviewListResponse> response = reviewController.getProductReviews(productId, 0, 20, "latest");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getReviews()).hasSize(1);
            assertThat(response.getData().getReviews().get(0).getReviewId()).isEqualTo(1L);
            assertThat(response.getData().getReviews().get(0).getTitle()).isEqualTo("좋은 신발입니다");
            assertThat(response.getData().getAverageRating()).isEqualTo(4.5);
            assertThat(response.getData().getTotalReviews()).isEqualTo(150L);

            verify(reviewService).getProductReviews(productId, 0, 20, "latest");
        }

        @Test
        @DisplayName("성공 - 커스텀 파라미터로 리뷰 목록 조회")
        void getProductReviews_Success_WithCustomParams() {
            // given
            Long productId = 100L;
            int page = 1, size = 10;
            String sort = "points";
            given(reviewService.getProductReviews(eq(productId), eq(page), eq(size), eq(sort)))
                    .willReturn(sampleReviewList);

            // when
            ApiResponse<ReviewListResponse> response = reviewController.getProductReviews(productId, page, size, sort);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getReviews()).hasSize(1);

            verify(reviewService).getProductReviews(productId, page, size, sort);
        }

        @Test
        @DisplayName("성공 - 빈 리뷰 목록 반환")
        void getProductReviews_Success_EmptyList() {
            // given
            Long productId = 999L;
            PageImpl<ReviewResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
            ReviewListResponse emptyResponse = ReviewListResponse.of(emptyPage, 0.0, 0L);
            
            given(reviewService.getProductReviews(eq(productId), eq(0), eq(20), eq("latest")))
                    .willReturn(emptyResponse);

            // when
            ApiResponse<ReviewListResponse> response = reviewController.getProductReviews(productId, 0, 20, "latest");

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getReviews()).isEmpty();
            assertThat(response.getData().getAverageRating()).isEqualTo(0.0);
            assertThat(response.getData().getTotalReviews()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("필터링된 상품별 리뷰 목록 조회 API")
    class GetProductReviewsWithFilters {

        @Test
        @DisplayName("성공 - 평점 필터 적용")
        void getProductReviewsWithFilters_Success_WithRatingFilter() {
            // given
            Long productId = 100L;
            Integer rating = 5;
            given(reviewService.getProductReviewsWithFilters(eq(productId), eq(0), eq(20), eq("latest"), eq(rating), eq(null), eq(null), eq(null)))
                    .willReturn(sampleReviewList);

            // when
            ApiResponse<ReviewListResponse> response = reviewController.getProductReviewsWithFilters(productId, 0, 20, "latest", rating, null, null, null);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getReviews()).hasSize(1);

            verify(reviewService).getProductReviewsWithFilters(productId, 0, 20, "latest", rating, null, null, null);
        }

        @Test
        @DisplayName("성공 - 모든 필터 적용")
        void getProductReviewsWithFilters_Success_WithAllFilters() {
            // given
            Long productId = 100L;
            Integer rating = 5;
            String sizeFit = "SMALL";
            String cushion = "SOFT";
            String stability = "STABLE";
            
            given(reviewService.getProductReviewsWithFilters(eq(productId), eq(0), eq(20), eq("latest"), eq(rating), eq(sizeFit), eq(cushion), eq(stability)))
                    .willReturn(sampleReviewList);

            // when
            ApiResponse<ReviewListResponse> response = reviewController.getProductReviewsWithFilters(productId, 0, 20, "latest", rating, sizeFit, cushion, stability);

            // then
            assertThat(response.isSuccess()).isTrue();

            verify(reviewService).getProductReviewsWithFilters(productId, 0, 20, "latest", rating, sizeFit, cushion, stability);
        }
    }

    @Nested
    @DisplayName("리뷰 상세 조회 API")
    class GetReview {

        @Test
        @DisplayName("성공 - 리뷰 상세 정보 조회")
        void getReview_Success() {
            // given
            Long reviewId = 1L;
            given(reviewService.getReview(eq(reviewId))).willReturn(sampleReview);

            // when
            ApiResponse<ReviewResponse> response = reviewController.getReview(reviewId);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getReviewId()).isEqualTo(1L);
            assertThat(response.getData().getTitle()).isEqualTo("좋은 신발입니다");
            assertThat(response.getData().getRating()).isEqualTo(5);
            assertThat(response.getData().getSizeFit()).isEqualTo(SizeFit.SMALL);
            assertThat(response.getData().getCushion()).isEqualTo(Cushion.SOFT);
            assertThat(response.getData().getStability()).isEqualTo(Stability.STABLE);

            verify(reviewService).getReview(reviewId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 리뷰")
        void getReview_Fail_NotFound() {
            // given
            Long reviewId = 999L;
            given(reviewService.getReview(eq(reviewId)))
                    .willThrow(new ReviewNotFoundException("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다."));

            // when & then
            assertThatThrownBy(() -> reviewController.getReview(reviewId))
                    .isInstanceOf(ReviewNotFoundException.class)
                    .hasMessage("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다.");

            verify(reviewService).getReview(reviewId);
        }

        @Test
        @DisplayName("실패 - 삭제된 리뷰 접근")
        void getReview_Fail_DeletedReview() {
            // given
            Long reviewId = 1L;
            given(reviewService.getReview(eq(reviewId)))
                    .willThrow(new ReviewNotFoundException("삭제되었거나 숨김 처리된 리뷰입니다."));

            // when & then
            assertThatThrownBy(() -> reviewController.getReview(reviewId))
                    .isInstanceOf(ReviewNotFoundException.class)
                    .hasMessage("삭제되었거나 숨김 처리된 리뷰입니다.");
        }
    }

    @Nested
    @DisplayName("상품별 3요소 평가 통계 조회 API")
    class GetProductStatistics {

        @Test
        @DisplayName("성공 - 3요소 평가 통계 조회")
        void getProductStatistics_Success() {
            // given
            Long productId = 100L;
            given(statisticsService.getProductStatistics(eq(productId))).willReturn(sampleStatistics);

            // when
            ApiResponse<Review3ElementStatisticsResponse> response = reviewController.getProductStatistics(productId);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getTotalReviews()).isEqualTo(100L);
            assertThat(response.getData().getSizeFitStatistics().getDistribution().get(SizeFit.SMALL)).isEqualTo(30L);
            assertThat(response.getData().getSizeFitStatistics().getDistribution().get(SizeFit.NORMAL)).isEqualTo(50L);
            assertThat(response.getData().getSizeFitStatistics().getDistribution().get(SizeFit.BIG)).isEqualTo(20L);
            assertThat(response.getData().getCushionStatistics().getPercentage().get(Cushion.SOFT)).isEqualTo(40.0);
            assertThat(response.getData().getStabilityStatistics().getPercentage().get(Stability.STABLE)).isEqualTo(60.0);

            verify(statisticsService).getProductStatistics(productId);
        }

        @Test
        @DisplayName("성공 - 리뷰가 없는 상품의 통계 조회")
        void getProductStatistics_Success_NoReviews() {
            // given
            Long productId = 999L;
            Review3ElementStatisticsResponse emptyStats = Review3ElementStatisticsResponse.builder()
                    .totalReviews(0L)
                    .sizeFitStatistics(Review3ElementStatisticsResponse.SizeFitStatistics.builder()
                            .distribution(Collections.emptyMap())
                            .percentage(Collections.emptyMap())
                            .build())
                    .cushionStatistics(Review3ElementStatisticsResponse.CushionStatistics.builder()
                            .distribution(Collections.emptyMap())
                            .percentage(Collections.emptyMap())
                            .build())
                    .stabilityStatistics(Review3ElementStatisticsResponse.StabilityStatistics.builder()
                            .distribution(Collections.emptyMap())
                            .percentage(Collections.emptyMap())
                            .build())
                    .build();
            
            given(statisticsService.getProductStatistics(eq(productId))).willReturn(emptyStats);

            // when
            ApiResponse<Review3ElementStatisticsResponse> response = reviewController.getProductStatistics(productId);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData().getTotalReviews()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("파라미터 검증 테스트")
    class ParameterValidation {

        @Test
        @DisplayName("음수 페이지 파라미터 처리")
        void handleNegativePageParameter() {
            // given
            Long productId = 100L;
            int negativePage = -1;
            
            // 실제 컨트롤러에서 음수 페이지가 그대로 전달되는 경우를 테스트
            given(reviewService.getProductReviews(eq(productId), eq(negativePage), eq(20), eq("latest")))
                    .willReturn(sampleReviewList);

            // when
            ApiResponse<ReviewListResponse> response = reviewController.getProductReviews(productId, negativePage, 20, "latest");

            // then
            assertThat(response.isSuccess()).isTrue();
            verify(reviewService).getProductReviews(productId, negativePage, 20, "latest");
        }

        @Test
        @DisplayName("잘못된 정렬 옵션 처리")
        void handleInvalidSortOption() {
            // given
            Long productId = 100L;
            String invalidSort = "invalid";
            given(reviewService.getProductReviews(eq(productId), eq(0), eq(20), eq(invalidSort)))
                    .willReturn(sampleReviewList);

            // when
            ApiResponse<ReviewListResponse> response = reviewController.getProductReviews(productId, 0, 20, invalidSort);

            // then
            assertThat(response.isSuccess()).isTrue(); // 서비스에서 처리하므로 OK

            verify(reviewService).getProductReviews(productId, 0, 20, invalidSort);
        }
    }
}