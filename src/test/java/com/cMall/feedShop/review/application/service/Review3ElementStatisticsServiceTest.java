package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.review.application.dto.response.Review3ElementStatisticsResponse;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Review3ElementStatisticsService 테스트")
class Review3ElementStatisticsServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private Review3ElementStatisticsService statisticsService;

    private Long productId;
    private Long totalReviews;
    private Map<Cushion, Long> cushionDistribution;
    private Map<SizeFit, Long> sizeFitDistribution;
    private Map<Stability, Long> stabilityDistribution;

    @BeforeEach
    void setUp() {
        productId = 1L;
        totalReviews = 10L;

        // Cushion 분포: SOFT=3, MEDIUM=4, FIRM=3
        cushionDistribution = new HashMap<>();
        cushionDistribution.put(Cushion.SOFT, 3L);
        cushionDistribution.put(Cushion.MEDIUM, 4L);
        cushionDistribution.put(Cushion.FIRM, 3L);

        // SizeFit 분포: SMALL=2, NORMAL=6, BIG=2
        sizeFitDistribution = new HashMap<>();
        sizeFitDistribution.put(SizeFit.SMALL, 2L);
        sizeFitDistribution.put(SizeFit.NORMAL, 6L);
        sizeFitDistribution.put(SizeFit.BIG, 2L);

        // Stability 분포: NORMAL=3, STABLE=5, VERY_STABLE=2
        stabilityDistribution = new HashMap<>();
        stabilityDistribution.put(Stability.NORMAL, 3L);
        stabilityDistribution.put(Stability.STABLE, 5L);
        stabilityDistribution.put(Stability.VERY_STABLE, 2L);
    }

    @Test
    @DisplayName("정상적인 통계 데이터가 주어졌을때_getProductStatistics 호출하면_올바른 통계가 반환된다")
    void givenValidStatisticsData_whenGetProductStatistics_thenReturnCorrectStatistics() {
        // given
        given(reviewRepository.countActiveReviewsByProductId(productId)).willReturn(totalReviews);
        given(reviewRepository.getCushionDistributionByProductId(productId)).willReturn(cushionDistribution);
        given(reviewRepository.getSizeFitDistributionByProductId(productId)).willReturn(sizeFitDistribution);
        given(reviewRepository.getStabilityDistributionByProductId(productId)).willReturn(stabilityDistribution);

        // when
        Review3ElementStatisticsResponse result = statisticsService.getProductStatistics(productId);

        // then
        assertThat(result.getTotalReviews()).isEqualTo(10L);

        // Cushion 통계 검증
        assertThat(result.getCushionStatistics().getDistribution()).isEqualTo(cushionDistribution);
        assertThat(result.getCushionStatistics().getMostSelected()).isEqualTo(Cushion.MEDIUM); // 4개로 가장 많음
        assertThat(result.getCushionStatistics().getPercentage().get(Cushion.MEDIUM)).isEqualTo(40.0); // 4/10 * 100
        assertThat(result.getCushionStatistics().getAverageScore()).isEqualTo(3.0); // (2*3 + 3*4 + 4*3) / 10 = 30/10 = 3.0

        // SizeFit 통계 검증
        assertThat(result.getSizeFitStatistics().getDistribution()).isEqualTo(sizeFitDistribution);
        assertThat(result.getSizeFitStatistics().getMostSelected()).isEqualTo(SizeFit.NORMAL); // 6개로 가장 많음
        assertThat(result.getSizeFitStatistics().getPercentage().get(SizeFit.NORMAL)).isEqualTo(60.0); // 6/10 * 100
        assertThat(result.getSizeFitStatistics().getAverageScore()).isEqualTo(3.0); // (2*2 + 3*6 + 4*2) / 10 = 30/10 = 3.0

        // Stability 통계 검증
        assertThat(result.getStabilityStatistics().getDistribution()).isEqualTo(stabilityDistribution);
        assertThat(result.getStabilityStatistics().getMostSelected()).isEqualTo(Stability.STABLE); // 5개로 가장 많음
        assertThat(result.getStabilityStatistics().getPercentage().get(Stability.STABLE)).isEqualTo(50.0); // 5/10 * 100
        assertThat(result.getStabilityStatistics().getAverageScore()).isEqualTo(3.9); // (3*3 + 4*5 + 5*2) / 10 = 39/10 = 3.9

        verify(reviewRepository, times(1)).countActiveReviewsByProductId(productId);
        verify(reviewRepository, times(1)).getCushionDistributionByProductId(productId);
        verify(reviewRepository, times(1)).getSizeFitDistributionByProductId(productId);
        verify(reviewRepository, times(1)).getStabilityDistributionByProductId(productId);
    }

    @Test
    @DisplayName("리뷰가 없는 상품일때_getProductStatistics 호출하면_빈 통계가 반환된다")
    void givenNoReviews_whenGetProductStatistics_thenReturnEmptyStatistics() {
        // given
        given(reviewRepository.countActiveReviewsByProductId(productId)).willReturn(0L);

        // when
        Review3ElementStatisticsResponse result = statisticsService.getProductStatistics(productId);

        // then
        assertThat(result.getTotalReviews()).isEqualTo(0L);
        assertThat(result.getCushionStatistics().getDistribution()).isEmpty();
        assertThat(result.getCushionStatistics().getMostSelected()).isNull();
        assertThat(result.getCushionStatistics().getAverageScore()).isEqualTo(0.0);
        assertThat(result.getSizeFitStatistics().getDistribution()).isEmpty();
        assertThat(result.getStabilityStatistics().getDistribution()).isEmpty();

        verify(reviewRepository, times(1)).countActiveReviewsByProductId(productId);
        verify(reviewRepository, times(0)).getCushionDistributionByProductId(productId); // 호출되지 않아야 함
    }

    @Test
    @DisplayName("한개 옵션만 선택된 경우_getProductStatistics 호출하면_100퍼센트 통계가 반환된다")
    void givenSingleOptionSelected_whenGetProductStatistics_thenReturn100PercentStatistics() {
        // given
        Map<Cushion, Long> singleCushionDistribution = new HashMap<>();
        singleCushionDistribution.put(Cushion.MEDIUM, 5L);

        Map<SizeFit, Long> singleSizeFitDistribution = new HashMap<>();
        singleSizeFitDistribution.put(SizeFit.NORMAL, 5L);

        Map<Stability, Long> singleStabilityDistribution = new HashMap<>();
        singleStabilityDistribution.put(Stability.STABLE, 5L);

        given(reviewRepository.countActiveReviewsByProductId(productId)).willReturn(5L);
        given(reviewRepository.getCushionDistributionByProductId(productId)).willReturn(singleCushionDistribution);
        given(reviewRepository.getSizeFitDistributionByProductId(productId)).willReturn(singleSizeFitDistribution);
        given(reviewRepository.getStabilityDistributionByProductId(productId)).willReturn(singleStabilityDistribution);

        // when
        Review3ElementStatisticsResponse result = statisticsService.getProductStatistics(productId);

        // then
        assertThat(result.getTotalReviews()).isEqualTo(5L);
        assertThat(result.getCushionStatistics().getPercentage().get(Cushion.MEDIUM)).isEqualTo(100.0);
        assertThat(result.getCushionStatistics().getMostSelected()).isEqualTo(Cushion.MEDIUM);
        assertThat(result.getSizeFitStatistics().getPercentage().get(SizeFit.NORMAL)).isEqualTo(100.0);
        assertThat(result.getStabilityStatistics().getPercentage().get(Stability.STABLE)).isEqualTo(100.0);
    }

    @Test
    @DisplayName("극값들이 포함된 분포일때_getProductStatistics 호출하면_올바른 평균이 계산된다")
    void givenExtremeValues_whenGetProductStatistics_thenCalculateCorrectAverage() {
        // given - 극값들 포함 (VERY_SOFT=1점, VERY_FIRM=5점)
        Map<Cushion, Long> extremeCushionDistribution = new HashMap<>();
        extremeCushionDistribution.put(Cushion.VERY_SOFT, 1L); // 1점
        extremeCushionDistribution.put(Cushion.VERY_FIRM, 1L); // 5점

        given(reviewRepository.countActiveReviewsByProductId(productId)).willReturn(2L);
        given(reviewRepository.getCushionDistributionByProductId(productId)).willReturn(extremeCushionDistribution);
        given(reviewRepository.getSizeFitDistributionByProductId(productId)).willReturn(new HashMap<>());
        given(reviewRepository.getStabilityDistributionByProductId(productId)).willReturn(new HashMap<>());

        // when
        Review3ElementStatisticsResponse result = statisticsService.getProductStatistics(productId);

        // then
        // (1*1 + 5*1) / 2 = 6/2 = 3.0
        assertThat(result.getCushionStatistics().getAverageScore()).isEqualTo(3.0);
    }
}