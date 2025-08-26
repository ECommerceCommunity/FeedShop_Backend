package com.cMall.feedShop.user.application.dto;

import com.cMall.feedShop.user.domain.model.UserStats;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserStatsResponse {
    private Long userId;
    private Integer totalPoints;

    // UserLevel 객체 대신 필요한 정보만 포함
    private String currentLevelName;
    private String levelDisplayName;
    private String levelEmoji;
    private String rewardDescription;

    private Integer pointsToNextLevel;
    private Double levelProgress;
    private Long userRank;
    private LocalDateTime levelUpdatedAt;


    public static UserStatsResponse from(UserStats userStats, Long userRank, Integer pointsToNextLevel, Double levelProgress) {
        return UserStatsResponse.builder()
                .userId(userStats.getUser().getId())
                .totalPoints(userStats.getTotalPoints())
                .currentLevelName(userStats.getCurrentLevel().getLevelName())
                .levelDisplayName(userStats.getCurrentLevel().getDisplayName())
                .levelEmoji(userStats.getCurrentLevel().getEmoji())
                .rewardDescription(userStats.getCurrentLevel().getRewardDescription())
                .pointsToNextLevel(pointsToNextLevel)
                .levelProgress(levelProgress)
                .userRank(userRank)
                .levelUpdatedAt(userStats.getLevelUpdatedAt())
                .build();
    }
}