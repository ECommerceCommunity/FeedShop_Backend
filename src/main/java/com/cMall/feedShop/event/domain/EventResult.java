package com.cMall.feedShop.event.domain;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 이벤트 결과 엔티티
 * 
 * <p>이벤트 종료 후 우승자 및 순위 정보를 저장합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Entity
@Table(name = "event_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false)
    private ResultType resultType;

    @Column(name = "announced_at", nullable = false)
    private LocalDateTime announcedAt;

    @Column(name = "total_participants")
    private Integer totalParticipants;

    @Column(name = "total_votes")
    private Long totalVotes;

    @OneToMany(mappedBy = "eventResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventResultDetail> resultDetails = new ArrayList<>();

    /**
     * 이벤트 결과 생성
     */
    public static EventResult createForEvent(Event event, ResultType resultType, 
                                           Integer totalParticipants, Long totalVotes) {
        return EventResult.builder()
                .event(event)
                .resultType(resultType)
                .announcedAt(LocalDateTime.now())
                .totalParticipants(totalParticipants)
                .totalVotes(totalVotes)
                .build();
    }

    /**
     * 결과 상세 정보 추가
     */
    public void addResultDetail(EventResultDetail detail) {
        this.resultDetails.add(detail);
        detail.setEventResult(this);
    }

    /**
     * 결과 타입 열거형
     */
    public enum ResultType {
        BATTLE_WINNER("배틀 우승자"),
        RANKING_TOP3("랭킹 TOP3");

        private final String displayName;

        ResultType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
