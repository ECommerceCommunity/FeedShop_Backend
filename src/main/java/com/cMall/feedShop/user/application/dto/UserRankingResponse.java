package com.cMall.feedShop.user.application.dto;

import com.cMall.feedShop.user.domain.model.UserLevel;
import com.cMall.feedShop.user.domain.model.UserStats;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRankingResponse {
    private Long userId;
    private String username;
    private String nickname;
    private Integer totalPoints;
    private UserLevel currentLevel;
    private String levelDisplayName;
    private String levelEmoji;
    private Long rank;
    
    public static UserRankingResponse from(UserStats userStats, Long rank) {
        return UserRankingResponse.builder()
                .userId(userStats.getUser().getId())
                .username(userStats.getUser().getUsername())
                .nickname(userStats.getUser().getUserProfile() != null ? 
                         userStats.getUser().getUserProfile().getNickname() : null)
                .totalPoints(userStats.getTotalPoints())
                .currentLevel(userStats.getCurrentLevel())
                .levelDisplayName(userStats.getCurrentLevel().getDisplayName())
                .levelEmoji(userStats.getCurrentLevel().getEmoji())
                .rank(rank)
                .build();
    }
}
