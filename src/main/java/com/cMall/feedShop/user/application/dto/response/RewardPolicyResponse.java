package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.model.RewardPolicy;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RewardPolicyResponse {

    private Long policyId;
    private RewardType rewardType;
    private String rewardTypeDisplayName;
    private String rewardTypeDescription;
    private Integer points;
    private String description;
    private Boolean isActive;
    private Integer dailyLimit;
    private Integer monthlyLimit;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RewardPolicyResponse from(RewardPolicy rewardPolicy) {
        return RewardPolicyResponse.builder()
                .policyId(rewardPolicy.getPolicyId())
                .rewardType(rewardPolicy.getRewardType())
                .rewardTypeDisplayName(rewardPolicy.getRewardType().getDisplayName())
                .rewardTypeDescription(rewardPolicy.getRewardType().getDescription())
                .points(rewardPolicy.getPoints())
                .description(rewardPolicy.getDescription())
                .isActive(rewardPolicy.getIsActive())
                .dailyLimit(rewardPolicy.getDailyLimit())
                .monthlyLimit(rewardPolicy.getMonthlyLimit())
                .validFrom(rewardPolicy.getValidFrom())
                .validTo(rewardPolicy.getValidTo())
                .createdAt(rewardPolicy.getCreatedAt())
                .updatedAt(rewardPolicy.getUpdatedAt())
                .build();
    }
}
