package com.cMall.feedShop.event.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import com.cMall.feedShop.common.BaseTimeEntity;

@Entity
@Table(name = "event_rewards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventReward extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_reward_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "condition_value", nullable = false, length = 50)
    private String conditionValue; // "1", "2", "3", "participation", "voters", "views", "likes", "random"

    @Column(name = "reward_value", nullable = false, columnDefinition = "TEXT")
    private String rewardValue; // 보상 내용

    @Column(name = "max_recipients")
    private Integer maxRecipients = 1; // 기본값 1

    // 연관관계 설정 메서드
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * 조건값이 등수인지 확인
     */
    public boolean isRankCondition() {
        try {
            Integer.parseInt(conditionValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 등수 반환 (등수 조건인 경우)
     */
    public Integer getRank() {
        if (isRankCondition()) {
            return Integer.parseInt(conditionValue);
        }
        return null;
    }

    /**
     * 조건 타입 반환
     */
    public String getConditionType() {
        if (isRankCondition()) {
            return "RANK";
        }
        return conditionValue.toUpperCase();
    }
}
