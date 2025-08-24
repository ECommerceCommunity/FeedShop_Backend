package com.cMall.feedShop.user.application.dto;

import com.cMall.feedShop.user.domain.model.ActivityType;
import com.cMall.feedShop.user.domain.model.UserActivity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserActivityResponse {
    private Long id;
    private ActivityType activityType;
    private String activityDescription;
    private Integer pointsEarned;
    private String description;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
    
    public static UserActivityResponse from(UserActivity activity) {
        return UserActivityResponse.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType())
                .activityDescription(activity.getActivityType().getDescription())
                .pointsEarned(activity.getPointsEarned())
                .description(activity.getDescription())
                .referenceId(activity.getReferenceId())
                .referenceType(activity.getReferenceType())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
