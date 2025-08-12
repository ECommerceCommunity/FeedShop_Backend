package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.model.RewardHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RewardHistoryResponse {

    private Long historyId;
    private Long userId;
    private String userLoginId;
    private RewardType rewardType;
    private String rewardTypeDisplayName;
    private Integer points;
    private String description;
    private Long relatedId;
    private String relatedType;
    private Long adminId;
    private Boolean isProcessed;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;

    public static RewardHistoryResponse from(RewardHistory rewardHistory) {
        return RewardHistoryResponse.builder()
                .historyId(rewardHistory.getHistoryId())
                .userId(rewardHistory.getUser().getId())
                .userLoginId(rewardHistory.getUser().getLoginId())
                .rewardType(rewardHistory.getRewardType())
                .rewardTypeDisplayName(rewardHistory.getRewardType().getDisplayName())
                .points(rewardHistory.getPoints())
                .description(rewardHistory.getDescription())
                .relatedId(rewardHistory.getRelatedId())
                .relatedType(rewardHistory.getRelatedType())
                .adminId(rewardHistory.getAdminId())
                .isProcessed(rewardHistory.getIsProcessed())
                .processedAt(rewardHistory.getProcessedAt())
                .createdAt(rewardHistory.getCreatedAt())
                .build();
    }
}
