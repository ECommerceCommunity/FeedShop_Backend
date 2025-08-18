package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.enums.RewardType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reward_histories")
@Getter
@NoArgsConstructor
public class RewardHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    private RewardType rewardType;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "related_id")
    private Long relatedId; // 관련 엔티티 ID (리뷰 ID, 이벤트 ID 등)

    @Column(name = "related_type", length = 50)
    private String relatedType; // 관련 엔티티 타입

    @Column(name = "admin_id")
    private Long adminId; // 관리자 지급 시 관리자 ID

    @Column(name = "is_processed", nullable = false)
    private Boolean isProcessed = false; // 포인트 적립 처리 여부

    @Column(name = "processed_at")
    private java.time.LocalDateTime processedAt; // 처리 시간

    @Builder
    public RewardHistory(User user, RewardType rewardType, Integer points, String description,
                        Long relatedId, String relatedType, Long adminId) {
        this.user = user;
        this.rewardType = rewardType;
        this.points = points;
        this.description = description;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
        this.adminId = adminId;
    }

    // 포인트 적립 처리 완료
    public void markAsProcessed() {
        this.isProcessed = true;
        this.processedAt = java.time.LocalDateTime.now();
    }

    // 처리 취소
    public void markAsUnprocessed() {
        this.isProcessed = false;
        this.processedAt = null;
    }
}
