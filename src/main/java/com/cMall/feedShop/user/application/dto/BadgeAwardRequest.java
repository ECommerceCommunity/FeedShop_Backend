package com.cMall.feedShop.user.application.dto;

import com.cMall.feedShop.user.domain.model.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeAwardRequest {
    private Long userId;
    private BadgeType badgeType;
}
