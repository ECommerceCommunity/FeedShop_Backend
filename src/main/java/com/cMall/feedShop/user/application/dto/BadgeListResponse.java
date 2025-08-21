package com.cMall.feedShop.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BadgeListResponse {
    private List<BadgeResponse> badges;
    private long totalCount;
    private long displayedCount;
    
    public static BadgeListResponse of(List<BadgeResponse> badges, long totalCount, long displayedCount) {
        return BadgeListResponse.builder()
                .badges(badges)
                .totalCount(totalCount)
                .displayedCount(displayedCount)
                .build();
    }
}
