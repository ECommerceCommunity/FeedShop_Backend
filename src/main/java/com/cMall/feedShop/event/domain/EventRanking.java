package com.cMall.feedShop.event.domain;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_rankings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventRanking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ranking_id")
    private Long rankingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private EventParticipant participant;

    @Column(name = "vote_count")
    private Integer voteCount;

    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;
}
