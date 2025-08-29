package com.cMall.feedShop.review.application.dto.response;

import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@Schema(description = "상품별 3요소 평가 통계 응늵")
public class Review3ElementStatisticsResponse {
    @Schema(description = "전체 리뷰 수", example = "150")
    private Long totalReviews;

    @Schema(description = "쿠션감 통계")
    // Cushion 통계
    private CushionStatistics cushionStatistics;

    @Schema(description = "사이즈 착용감 통계")
    // SizeFit 통계
    private SizeFitStatistics sizeFitStatistics;

    @Schema(description = "안정성 통계")
    // Stability 통계
    private StabilityStatistics stabilityStatistics;

    @Getter
    @Builder
    @Schema(description = "쿠션감 통계 정보")
    public static class CushionStatistics {
        @Schema(description = "쿠션감 옵션별 개수 분포")
        private Map<Cushion, Long> distribution; // 각 옵션별 개수
        
        @Schema(description = "쿠션감 옵션별 비율 (백분율)")
        private Map<Cushion, Double> percentage; // 각 옵션별 비율
        
        @Schema(description = "가장 많이 선택된 쿠션감", example = "MEDIUM")
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
