package com.cMall.feedShop.user.application.dto;

import com.cMall.feedShop.user.domain.model.UserLevel;
import com.cMall.feedShop.user.domain.model.UserStats;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserStatsResponse {
    private Long userId;
    private Integer totalPoints;
    private UserLevel currentLevel;
    private String levelDisplayName;
    private String levelEmoji;
    private String rewardDescription;
    private Integer pointsToNextLevel;
    private Double levelProgress;
    private Long userRank;
    private LocalDateTime levelUpdatedAt;
    
    public static UserStatsResponse from(UserStats userStats, Long userRank, java.util.List<com.cMall.feedShop.user.domain.model.UserLevel> allLevels) {
        return UserStatsResponse.builder()
                .userId(userStats.getUser().getId())
                .totalPoints(userStats.getTotalPoints())
                .currentLevel(userStats.getCurrentLevel())
                .levelDisplayName(userStats.getCurrentLevel().getDisplayName())
                .levelEmoji(userStats.getCurrentLevel().getEmoji())
                .rewardDescription(userStats.getCurrentLevel().getRewardDescription())
                .pointsToNextLevel(userStats.getPointsToNextLevel(allLevels))
                .levelProgress(userStats.getLevelProgress(allLevels))
                .userRank(userRank)
                .levelUpdatedAt(userStats.getLevelUpdatedAt())
                .build();
    }
}
