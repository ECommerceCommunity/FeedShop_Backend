package com.cMall.feedShop.user.application.dto;

import com.cMall.feedShop.user.domain.model.BadgeType;
import com.cMall.feedShop.user.domain.model.UserBadge;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BadgeResponse {
    private Long id;
    private String badgeName;
    private String badgeDescription;
    private String badgeImageUrl;
    private BadgeType badgeType;
    private LocalDateTime awardedAt;
    private Boolean isDisplayed;

    public static BadgeResponse from(UserBadge userBadge) {
        return BadgeResponse.builder()
                .id(userBadge.getId())
                .badgeName(userBadge.getBadgeName())
                .badgeDescription(userBadge.getBadgeDescription())
                .badgeImageUrl(userBadge.getBadgeImageUrl())
                .badgeType(userBadge.getBadgeType())
                .awardedAt(userBadge.getAwardedAt())
                .isDisplayed(userBadge.getIsDisplayed())
                .build();
    }
}
