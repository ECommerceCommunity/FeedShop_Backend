package com.cMall.feedShop.event.domain;

import com.cMall.feedShop.event.domain.enums.MatchStatus;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "battle_matches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BattleMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "battle_match_id")
    private Long battleMatchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant1_id", nullable = false)
    private EventParticipant participant1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant2_id", nullable = false)
    private EventParticipant participant2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private EventParticipant winner;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false)
    private MatchStatus matchStatus = MatchStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;
} 