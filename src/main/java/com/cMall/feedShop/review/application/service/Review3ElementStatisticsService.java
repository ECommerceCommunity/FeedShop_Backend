package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.review.application.dto.response.Review3ElementStatisticsResponse;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class Review3ElementStatisticsService {

    private final ReviewRepository reviewRepository;

    public Review3ElementStatisticsResponse getProductStatistics(Long productId) {
        Long totalReviews = reviewRepository.countActiveReviewsByProductId(productId);

        if (totalReviews == 0) {
            return createEmptyStatistics();
        }

        // 각 요소별 분포 조회
        Map<Cushion, Long> cushionDistribution = reviewRepository.getCushionDistributionByProductId(productId);
        Map<SizeFit, Long> sizeFitDistribution = reviewRepository.getSizeFitDistributionByProductId(productId);
        Map<Stability, Long> stabilityDistribution = reviewRepository.getStabilityDistributionByProductId(productId);

        return Review3ElementStatisticsResponse.builder()
                .totalReviews(totalReviews)
                .cushionStatistics(buildCushionStatistics(cushionDistribution, totalReviews))
                .sizeFitStatistics(buildSizeFitStatistics(sizeFitDistribution, totalReviews))
                .stabilityStatistics(buildStabilityStatistics(stabilityDistribution, totalReviews))
                .build();
    }

    private Review3ElementStatisticsResponse.CushionStatistics buildCushionStatistics(
            Map<Cushion, Long> distribution, Long totalReviews) {

        Map<Cushion, Double> percentage = calculatePercentages(distribution, totalReviews);
        Cushion mostSelected = findMostSelected(distribution);
        Double averageScore = calculateCushionAverageScore(distribution, totalReviews);

        return Review3ElementStatisticsResponse.CushionStatistics.builder()
                .distribution(distribution)
                .percentage(percentage)
                .mostSelected(mostSelected)
                .averageScore(averageScore)
                .build();
    }

    private Review3ElementStatisticsResponse.SizeFitStatistics buildSizeFitStatistics(
            Map<SizeFit, Long> distribution, Long totalReviews) {

        Map<SizeFit, Double> percentage = calculatePercentages(distribution, totalReviews);
        SizeFit mostSelected = findMostSelected(distribution);
        Double averageScore = calculateSizeFitAverageScore(distribution, totalReviews);

        return Review3ElementStatisticsResponse.SizeFitStatistics.builder()
                .distribution(distribution)
                .percentage(percentage)
                .mostSelected(mostSelected)
                .averageScore(averageScore)
                .build();
    }

    private Review3ElementStatisticsResponse.StabilityStatistics buildStabilityStatistics(
            Map<Stability, Long> distribution, Long totalReviews) {

        Map<Stability, Double> percentage = calculatePercentages(distribution, totalReviews);
        Stability mostSelected = findMostSelected(distribution);
        Double averageScore = calculateStabilityAverageScore(distribution, totalReviews);

        return Review3ElementStatisticsResponse.StabilityStatistics.builder()
                .distribution(distribution)
                .percentage(percentage)
                .mostSelected(mostSelected)
                .averageScore(averageScore)
                .build();
    }

    private <T> Map<T, Double> calculatePercentages(Map<T, Long> distribution, Long total) {
        Map<T, Double> percentages = new HashMap<>();
        distribution.forEach((key, count) -> {
            double percentage = (count.doubleValue() / total.doubleValue()) * 100.0;
            percentages.put(key, Math.round(percentage * 10.0) / 10.0); // 소수점 1자리
        });
        return percentages;
    }

    private <T> T findMostSelected(Map<T, Long> distribution) {
        return distribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // Cushion을 1-5점으로 환산 (VERY_FIRM=1, FIRM=2, MEDIUM=3, SOFT=4, VERY_SOFT=5)
    private Double calculateCushionAverageScore(Map<Cushion, Long> distribution, Long totalReviews) {
        double totalScore = 0.0;
        for (Map.Entry<Cushion, Long> entry : distribution.entrySet()) {
            int score = getCushionScore(entry.getKey());
            totalScore += score * entry.getValue();
        }
        return Math.round((totalScore / totalReviews) * 10.0) / 10.0;
    }

    // SizeFit을 1-5점으로 환산 (VERY_SMALL=1, SMALL=2, NORMAL=3, BIG=4, VERY_BIG=5)
    private Double calculateSizeFitAverageScore(Map<SizeFit, Long> distribution, Long totalReviews) {
        double totalScore = 0.0;
        for (Map.Entry<SizeFit, Long> entry : distribution.entrySet()) {
            int score = getSizeFitScore(entry.getKey());
            totalScore += score * entry.getValue();
        }
        return Math.round((totalScore / totalReviews) * 10.0) / 10.0;
    }

    // Stability를 1-5점으로 환산 (VERY_UNSTABLE=1, UNSTABLE=2, NORMAL=3, STABLE=4, VERY_STABLE=5)
    private Double calculateStabilityAverageScore(Map<Stability, Long> distribution, Long totalReviews) {
        double totalScore = 0.0;
        for (Map.Entry<Stability, Long> entry : distribution.entrySet()) {
            int score = getStabilityScore(entry.getKey());
            totalScore += score * entry.getValue();
        }
        return Math.round((totalScore / totalReviews) * 10.0) / 10.0;
    }

    private int getCushionScore(Cushion cushion) {
        return switch (cushion) {
            case VERY_FIRM -> 1;    // 매우 딱딱함
            case FIRM -> 2;         // 딱딱함
            case MEDIUM -> 3;       // 적당함
            case SOFT -> 4;         // 푹신함
            case VERY_SOFT -> 5;    // 매우 푹신함
        };
    }

    private int getSizeFitScore(SizeFit sizeFit) {
        return switch (sizeFit) {
            case VERY_SMALL -> 1;
            case SMALL -> 2;
            case NORMAL -> 3;
            case BIG -> 4;
            case VERY_BIG -> 5;
        };
    }

    private int getStabilityScore(Stability stability) {
        return switch (stability) {
            case VERY_UNSTABLE -> 1;
            case UNSTABLE -> 2;
            case NORMAL -> 3;
            case STABLE -> 4;
            case VERY_STABLE -> 5;
        };
    }

    private Review3ElementStatisticsResponse createEmptyStatistics() {
        return Review3ElementStatisticsResponse.builder()
                .totalReviews(0L)
                .cushionStatistics(Review3ElementStatisticsResponse.CushionStatistics.builder()
                        .distribution(new HashMap<>())
                        .percentage(new HashMap<>())
                        .mostSelected(null)
                        .averageScore(0.0)
                        .build())
                .sizeFitStatistics(Review3ElementStatisticsResponse.SizeFitStatistics.builder()
                        .distribution(new HashMap<>())
                        .percentage(new HashMap<>())
                        .mostSelected(null)
                        .averageScore(0.0)
                        .build())
                .stabilityStatistics(Review3ElementStatisticsResponse.StabilityStatistics.builder()
                        .distribution(new HashMap<>())
                        .percentage(new HashMap<>())
                        .mostSelected(null)
                        .averageScore(0.0)
                        .build())
                .build();
    }
}
