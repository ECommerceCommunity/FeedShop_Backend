package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.enums.RewardType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_policies")
@Getter
@NoArgsConstructor
public class RewardPolicy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, unique = true)
    private RewardType rewardType;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "daily_limit")
    private Integer dailyLimit; // 일일 획득 제한

    @Column(name = "monthly_limit")
    private Integer monthlyLimit; // 월간 획득 제한

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Builder
    public RewardPolicy(RewardType rewardType, Integer points, String description, 
                       Boolean isActive, Integer dailyLimit, Integer monthlyLimit,
                       LocalDateTime validFrom, LocalDateTime validTo) {
        this.rewardType = rewardType;
        this.points = points;
        this.description = description;
        this.isActive = isActive != null ? isActive : true;
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    // 정책이 유효한지 확인
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        
        if (!isActive) {
            return false;
        }
        
        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }
        
        if (validTo != null && now.isAfter(validTo)) {
            return false;
        }
        
        return true;
    }

    // 정책 업데이트
    public void updatePolicy(Integer points, String description, Boolean isActive,
                           Integer dailyLimit, Integer monthlyLimit,
                           LocalDateTime validFrom, LocalDateTime validTo) {
        if (points != null) this.points = points;
        if (description != null) this.description = description;
        if (isActive != null) this.isActive = isActive;
        if (dailyLimit != null) this.dailyLimit = dailyLimit;
        if (monthlyLimit != null) this.monthlyLimit = monthlyLimit;
        if (validFrom != null) this.validFrom = validFrom;
        if (validTo != null) this.validTo = validTo;
    }

    // 정책 비활성화
    public void deactivate() {
        this.isActive = false;
    }
}
