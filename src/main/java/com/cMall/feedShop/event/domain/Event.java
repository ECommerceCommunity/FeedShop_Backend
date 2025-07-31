package com.cMall.feedShop.event.domain;

import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.common.util.TimeUtil;
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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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

    /**
     * 팩토리 메서드: EventDetail과 함께 이벤트 생성 (빌더 패턴 활용)
     */
    public static Event createWithDetail(EventType type, Integer maxParticipants, EventDetail eventDetail) {
        Event event = Event.builder()
                .type(type)
                .maxParticipants(maxParticipants)
                .status(EventStatus.UPCOMING)
                .build();
        event.setEventDetail(eventDetail);
        return event;
    }

    /**
     * 팩토리 메서드: EventReward들과 함께 이벤트 생성 (빌더 패턴 활용)
     */
    public static Event createWithRewards(EventType type, Integer maxParticipants, List<EventReward> rewards) {
        Event event = Event.builder()
                .type(type)
                .maxParticipants(maxParticipants)
                .status(EventStatus.UPCOMING)
                .build();
        event.setRewards(rewards);
        return event;
    }

    /**
     * 이벤트 상태를 자동으로 계산하여 업데이트
     */
    public void updateStatusAutomatically() {
        if (eventDetail == null || eventDetail.getEventStartDate() == null || eventDetail.getEventEndDate() == null) {
            return;
        }
        LocalDate today = TimeUtil.nowDate(); // 한국 시간대 기준 현재 날짜
        if (today.isBefore(eventDetail.getEventStartDate())) {
            this.status = EventStatus.UPCOMING;
        } else if (today.isAfter(eventDetail.getEventEndDate())) {
            this.status = EventStatus.ENDED;
        } else {
            this.status = EventStatus.ONGOING;
        }
    }

    /**
     * 현재 상태가 자동 계산된 상태와 일치하는지 확인
     */
    public boolean isStatusUpToDate() {
        EventStatus calculatedStatus = calculateStatus();
        return this.status == calculatedStatus;
    }

    /**
     * 현재 날짜 기준으로 이벤트 상태 계산
     */
    public EventStatus calculateStatus() {
        if (eventDetail == null || eventDetail.getEventStartDate() == null || eventDetail.getEventEndDate() == null) {
            return this.status;
        }
        LocalDate today = TimeUtil.nowDate(); // 한국 시간대 기준 현재 날짜
        if (today.isBefore(eventDetail.getEventStartDate())) return EventStatus.UPCOMING;
        if (today.isAfter(eventDetail.getEventEndDate())) return EventStatus.ENDED;
        return EventStatus.ONGOING;
    }

    /**
     * 이벤트 정보 수정 (빌더 패턴 활용)
     */
    public void updateFromDto(com.cMall.feedShop.event.application.dto.request.EventUpdateRequestDto dto) {
        this.type = dto.getType() != null ? com.cMall.feedShop.event.domain.enums.EventType.valueOf(dto.getType()) : this.type;
        this.status = dto.getStatus() != null ? com.cMall.feedShop.event.domain.enums.EventStatus.valueOf(dto.getStatus()) : this.status;
        this.maxParticipants = dto.getMaxParticipants() != null ? dto.getMaxParticipants() : this.maxParticipants;
        if (this.eventDetail != null) {
            this.eventDetail.updateFromDto(dto);
        }
    }

    /**
     * 이벤트 정보 업데이트 (영속성 유지)
     */
    public void update(EventType type, Integer maxParticipants) {
        if (type != null) {
            this.type = type;
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

    /**
     * 소프트 딜리트
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
