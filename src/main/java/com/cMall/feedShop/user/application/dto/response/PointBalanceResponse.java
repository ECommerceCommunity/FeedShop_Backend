package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.model.UserPoint;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PointBalanceResponse {
    private Integer currentPoints;
    private Integer totalEarnedPoints;
    private Integer totalUsedPoints;
    private Integer totalExpiredPoints;
    private LocalDateTime lastUpdatedAt;

    public static PointBalanceResponse from(UserPoint userPoint, Integer totalEarned, Integer totalUsed, Integer totalExpired) {
        return PointBalanceResponse.builder()
                .currentPoints(userPoint.getCurrentPoints())
                .totalEarnedPoints(totalEarned)
                .totalUsedPoints(totalUsed)
                .totalExpiredPoints(totalExpired)
                .lastUpdatedAt(userPoint.getUpdatedAt())
                .build();
    }
}

