package com.cMall.feedShop.event.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.event.domain.enums.ParticipationStatus;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

/**
 * 이벤트 참여자 엔터티
 * 
 * <p>이벤트에 참여하는 사용자와 피드 정보를 관리합니다.</p>
 * <p>metadata 필드를 통해 이벤트 유형별 추가 정보를 JSON 형태로 저장할 수 있습니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Entity
@Table(name = "event_participants",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_event_participant_event_user", columnNames = {"event_id", "user_id"}),
           @UniqueConstraint(name = "uk_event_participant_event_feed", columnNames = {"event_id", "feed_id"})
       },
       indexes = {
           @Index(name = "idx_event_participant_event", columnList = "event_id"),
           @Index(name = "idx_event_participant_user", columnList = "user_id"),
           @Index(name = "idx_event_participant_feed", columnList = "feed_id"),
           @Index(name = "idx_event_participant_status", columnList = "status")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventParticipant extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParticipationStatus status = ParticipationStatus.PARTICIPATING;
    
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
    
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
    
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;
    
    @Builder
    public EventParticipant(Event event, User user, Feed feed, String metadata) {
        this.event = event;
        this.user = user;
        this.feed = feed;
        this.metadata = metadata;
        this.joinedAt = LocalDateTime.now();
    }
    
    /**
     * 참여자 상태를 변경합니다.
     * 
     * @param newStatus 새로운 상태
     */
    public void updateStatus(ParticipationStatus newStatus) {
        this.status = newStatus;
        
        if (newStatus == ParticipationStatus.ELIMINATED) {
            this.withdrawnAt = LocalDateTime.now();
        }
    }
    
    /**
     * 메타데이터를 업데이트합니다.
     * 
     * @param metadata 새로운 메타데이터
     */
    public void updateMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    /**
     * 참여자가 활성 상태인지 확인합니다.
     * 
     * @return 활성 상태 여부
     */
    public boolean isActive() {
        return status == ParticipationStatus.PARTICIPATING;
    }
    
    /**
     * 참여자가 탈락했는지 확인합니다.
     * 
     * @return 탈락 여부
     */
    public boolean isEliminated() {
        return status == ParticipationStatus.ELIMINATED;
    }
    
    /**
     * 배틀 이벤트용 메타데이터 생성
     */
    public static String createBattleMetadata(Integer matchGroup, Integer matchPosition) {
        return String.format("{\"matchGroup\": %d, \"matchPosition\": %d}", 
                matchGroup != null ? matchGroup : 0, 
                matchPosition != null ? matchPosition : 0);
    }
    
    /**
     * 랭킹 이벤트용 메타데이터 생성
     */
    public static String createRankingMetadata(Integer currentRank, Long voteCount) {
        return String.format("{\"currentRank\": %d, \"voteCount\": %d}", 
                currentRank != null ? currentRank : 0, 
                voteCount != null ? voteCount : 0);
    }
}