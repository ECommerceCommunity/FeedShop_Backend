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
@Table(name = "events", indexes = {
    @Index(name = "idx_event_deleted_at", columnList = "deleted_at"),
    @Index(name = "idx_event_status", columnList = "status")
})
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
    private EventStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

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

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventImage> images;

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

    public void setImages(List<EventImage> images) {
        this.images = images;
        if (images != null) {
            images.forEach(image -> image.setEvent(this));
        }
    }

    /**
     * 정적 팩토리 메서드: 기본 이벤트 생성
     */
    public static Event createWithDetail(EventType type, Integer maxParticipants, User createdUser) {
        return Event.builder()
                .type(type)
                .status(EventStatus.UPCOMING) // 기본값: 예정
                .maxParticipants(maxParticipants)
                .createdUser(createdUser)
                .createdBy(LocalDateTime.now())
                .build();
    }

    /**
     * 이벤트 정보 수정 (빌더 패턴 활용)
     */
    public void updateFromDto(com.cMall.feedShop.event.application.dto.request.EventUpdateRequestDto dto) {
        this.type = dto.getType() != null ? dto.getType() : this.type;
        this.status = dto.getStatus() != null ? EventStatus.valueOf(dto.getStatus()) : this.status;
        this.maxParticipants = dto.getMaxParticipants() != null ? dto.getMaxParticipants() : this.maxParticipants;
        if (this.eventDetail != null) {
            this.eventDetail.updateFromDto(dto);
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
