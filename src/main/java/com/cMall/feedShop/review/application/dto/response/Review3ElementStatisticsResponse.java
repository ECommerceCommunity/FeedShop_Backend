package com.cMall.feedShop.review.application.dto.response;

import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class Review3ElementStatisticsResponse {
    private Long totalReviews;

    // Cushion 통계
    private CushionStatistics cushionStatistics;

    // SizeFit 통계
    private SizeFitStatistics sizeFitStatistics;

    // Stability 통계
    private StabilityStatistics stabilityStatistics;

    @Getter
    @Builder
    public static class CushionStatistics {
        private Map<Cushion, Long> distribution; // 각 옵션별 개수
        private Map<Cushion, Double> percentage; // 각 옵션별 비율
        private Cushion mostSelected; // 가장 많이 선택된 옵션
        private Double averageScore; // 평균 점수 (1-5점으로 환산)
    }

    @Getter
    @Builder
    public static class SizeFitStatistics {
        private Map<SizeFit, Long> distribution;
        private Map<SizeFit, Double> percentage;
        private SizeFit mostSelected;
        private Double averageScore;
    }

    @Getter
    @Builder
    public static class StabilityStatistics {
        private Map<Stability, Long> distribution;
        private Map<Stability, Double> percentage;
        private Stability mostSelected;
        private Double averageScore;
    }
}
