package com.cMall.feedShop.event.domain;

import com.cMall.feedShop.event.domain.enums.ParticipationStatus;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_participants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_status", nullable = false)
    private ParticipationStatus participationStatus = ParticipationStatus.PARTICIPATING;

    @Column(name = "participation_date", nullable = false)
    private LocalDateTime participationDate;

    @Column(name = "feed_id")
    private Long feedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
}