package com.cMall.feedShop.event.domain;

import com.cMall.feedShop.event.domain.enums.RewardConditionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import com.cMall.feedShop.common.BaseTimeEntity;

/**
 * 이벤트 보상 엔티티
 * 
 * <p>이벤트에서 제공하는 보상 정보를 관리합니다.</p>
 * 
 * <p>보상 조건은 다음과 같이 처리됩니다:</p>
 * <ul>
 *   <li>숫자 문자열 (예: "1", "2", "3"): 등수 기반 보상</li>
 *   <li>문자열 (예: "participation", "voters"): 특별 조건 보상</li>
 * </ul>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Entity
@Table(name = "event_rewards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventReward extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /**
     * 보상 조건값
     * 
     * <p>숫자 문자열인 경우 등수, 그 외의 경우 특별 조건을 나타냅니다.</p>
     * <p>예시: "1", "2", "3" (등수) 또는 "participation", "voters" (특별 조건)</p>
     */
    @Column(name = "condition_value", nullable = false, length = 50)
    private String conditionValue;

    /**
     * 보상 내용
     * 
     * <p>실제 지급될 보상의 상세 내용을 저장합니다.</p>
     */
    @Column(name = "reward_value", nullable = false, columnDefinition = "TEXT")
    private String rewardValue;

    /**
     * 최대 수령자 수
     * 
     * <p>해당 보상을 받을 수 있는 최대 인원 수입니다.</p>
     * <p>기본값은 1이며, 특별 조건에 따라 조정될 수 있습니다.</p>
     */
    @Column(name = "max_recipients")
    private Integer maxRecipients = 1;

    /**
     * 조건값이 등수인지 확인합니다.
     * 
     * @return 등수 조건이면 true, 그렇지 않으면 false
     */
    public boolean isRankCondition() {
        return getConditionType() != null && getConditionType().isRank();
    }

    /**
     * 등수를 반환합니다 (등수 조건인 경우).
     * 
     * @return 등수, 등수 조건이 아니면 null
     */
    public Integer getRank() {
        if (isRankCondition()) {
            return Integer.parseInt(conditionValue);
        }
        return null;
    }

    /**
     * 조건 타입을 반환합니다.
     * 
     * @return RewardConditionType, 파싱할 수 없으면 null
     */
    public RewardConditionType getConditionType() {
        return RewardConditionType.fromString(conditionValue);
    }

    /**
     * 조건 설명을 반환합니다.
     * 
     * <p>등수 조건인 경우 "1등", "2등" 형태로, 특별 조건인 경우 enum의 description을 반환합니다.</p>
     * 
     * @return 조건 설명
     */
    public String getConditionDescription() {
        RewardConditionType type = getConditionType();
        if (type == null) {
            return conditionValue;
        }
        
        if (type.isRank()) {
            return conditionValue + "등";
        }
        
        return type.getDescription();
    }
}
