package com.cMall.feedShop.feed.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.enums.RewardType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 피드 리워드 이벤트 엔티티
 * 피드 관련 액션 발생 시 리워드 지급을 위한 중간 엔티티
 */
@Entity
@Table(name = "feed_reward_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedRewardEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private com.cMall.feedShop.feed.domain.entity.Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.cMall.feedShop.user.domain.model.User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    private RewardType rewardType;

    @Column(name = "event_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "related_data", length = 1000)
    private String relatedData; // JSON 형태로 관련 데이터 저장 (좋아요 수, 댓글 수 등)

    @Column(name = "processed_at")
    private LocalDateTime processedAt; // 리워드 처리 완료 시간

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0; // 재시도 횟수

    @Column(name = "error_message", length = 1000)
    private String errorMessage; // 오류 발생 시 메시지

    @Builder
    public FeedRewardEvent(com.cMall.feedShop.feed.domain.entity.Feed feed, com.cMall.feedShop.user.domain.model.User user, 
                          RewardType rewardType, Integer points, String description, String relatedData) {
        this.feed = feed;
        this.user = user;
        this.rewardType = rewardType;
        this.points = points;
        this.description = description;
        this.relatedData = relatedData;
        this.eventStatus = EventStatus.PENDING;
        this.retryCount = 0;
    }

    /**
     * 리워드 처리 완료
     */
    public void markAsProcessed() {
        this.eventStatus = EventStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 리워드 처리 실패
     */
    public void markAsFailed(String errorMessage) {
        this.eventStatus = EventStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    /**
     * 재처리 시도
     */
    public void retry() {
        this.eventStatus = EventStatus.PENDING;
        this.errorMessage = null;
    }

    /**
     * 이벤트 상태 열거형
     */
    public enum EventStatus {
        PENDING("대기중"),
        PROCESSING("처리중"),
        PROCESSED("처리완료"),
        FAILED("처리실패"),
        CANCELLED("취소됨");

        private final String displayName;

        EventStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
