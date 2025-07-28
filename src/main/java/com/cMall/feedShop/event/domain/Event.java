package com.cMall.feedShop.event.domain;

import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.cMall.feedShop.common.BaseTimeEntity;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Event extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "created_by", updatable = false)
    private LocalDateTime createdBy;

    @Column(name = "updated_by")
    private LocalDateTime updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User createdUser;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private EventDetail eventDetail;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventReward> rewards;

    // 연관관계 설정 메서드
    public void setEventDetail(EventDetail eventDetail) {
        this.eventDetail = eventDetail;
        if (eventDetail != null) {
            eventDetail.setEvent(this);
        }
    }

    public void setRewards(List<EventReward> rewards) {
        this.rewards = rewards;
        if (rewards != null) {
            rewards.forEach(reward -> reward.setEvent(this));
        }
    }

    public void updateStatusAutomatically() {
        if (eventDetail == null || eventDetail.getEventStartDate() == null || eventDetail.getEventEndDate() == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (today.isBefore(eventDetail.getEventStartDate())) {
            this.status = EventStatus.UPCOMING;
        } else if (today.isAfter(eventDetail.getEventEndDate())) {
            this.status = EventStatus.ENDED;
        } else {
            this.status = EventStatus.ONGOING;
        }
    }

    public EventStatus calculateStatus() {
        if (eventDetail == null || eventDetail.getEventStartDate() == null || eventDetail.getEventEndDate() == null) {
            return this.status;
        }
        LocalDate today = LocalDate.now();
        if (today.isBefore(eventDetail.getEventStartDate())) return EventStatus.UPCOMING;
        if (today.isAfter(eventDetail.getEventEndDate())) return EventStatus.ENDED;
        return EventStatus.ONGOING;
    }

    // 기타 연관관계 필요시 여기에 추가

    /**
     * 이벤트 정보 업데이트 (영속성 유지)
     */
    public void update(String type, Integer maxParticipants) {
        if (type != null) {
            this.type = EventType.valueOf(type);
        }
        if (maxParticipants != null) {
            this.maxParticipants = maxParticipants;
        }
        this.updatedBy = LocalDateTime.now();
    }

    /**
     * 이벤트 상태 업데이트
     */
    public void updateStatus(EventStatus status) {
        this.status = status;
        this.updatedBy = LocalDateTime.now();
    }
}
