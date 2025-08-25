package com.cMall.feedShop.event.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.event.domain.enums.MatchStatus;
import com.cMall.feedShop.feed.domain.Feed;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이벤트 매치 엔터티
 * 
 * <p>이벤트 내에서 발생하는 매칭 정보를 관리합니다.</p>
 * <p>주로 배틀 이벤트에서 두 참여자 간의 대결 정보를 저장합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Entity
@Table(name = "event_matches",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_event_match_event_group", columnNames = {"event_id", "match_group"})
       },
       indexes = {
           @Index(name = "idx_event_match_event", columnList = "event_id"),
           @Index(name = "idx_event_match_status", columnList = "status"),
           @Index(name = "idx_event_match_group", columnList = "match_group")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventMatch extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @Column(name = "match_group", nullable = false)
    private Integer matchGroup;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant1_id", nullable = false)
    private Feed participant1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant2_id")
    private Feed participant2;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MatchStatus status = MatchStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Feed winner;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
    
    @Builder
    public EventMatch(Event event, Integer matchGroup, Feed participant1, Feed participant2, String metadata) {
        this.event = event;
        this.matchGroup = matchGroup;
        this.participant1 = participant1;
        this.participant2 = participant2;
        this.metadata = metadata;
    }
    
    /**
     * 매치를 시작합니다.
     */
    public void start() {
        this.status = MatchStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
    }
    
    /**
     * 매치를 완료합니다.
     * 
     * @param winner 우승자
     */
    public void complete(Feed winner) {
        this.status = MatchStatus.COMPLETED;
        this.winner = winner;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * 매치 상태를 업데이트합니다.
     * 
     * @param newStatus 새로운 상태
     */
    public void updateStatus(MatchStatus newStatus) {
        this.status = newStatus;
        
        if (newStatus == MatchStatus.ACTIVE && this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        } else if (newStatus == MatchStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
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
     * 매치가 진행 중인지 확인합니다.
     * 
     * @return 진행 중 여부
     */
    public boolean isActive() {
        return status == MatchStatus.ACTIVE;
    }
    
    /**
     * 매치가 완료되었는지 확인합니다.
     * 
     * @return 완료 여부
     */
    public boolean isCompleted() {
        return status == MatchStatus.COMPLETED;
    }
    
    /**
     * 매치가 대기 중인지 확인합니다.
     * 
     * @return 대기 중 여부
     */
    public boolean isPending() {
        return status == MatchStatus.PENDING;
    }
    
    /**
     * 단일 참여자 매치인지 확인합니다.
     * 
     * @return 단일 참여자 여부
     */
    public boolean isSingleParticipant() {
        return participant2 == null;
    }
    
    /**
     * 배틀 매치용 메타데이터 생성
     */
    public static String createBattleMetadata(Integer round, String description) {
        return String.format("{\"round\": %d, \"description\": \"%s\"}", 
                round != null ? round : 1, 
                description != null ? description : "");
    }
}
