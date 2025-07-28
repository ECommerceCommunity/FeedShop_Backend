package com.cMall.feedShop.event.application.dto.response;

import com.cMall.feedShop.event.domain.enums.RewardKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardDto {
    
    private Long rewardId;
    private RewardKind rewardKind;
    private String rewardName;
    private Integer rewardAmount;
    private String description;
} 