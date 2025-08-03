package com.cMall.feedShop.event.domain;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import com.cMall.feedShop.common.BaseTimeEntity;

@Entity
@Table(name = "event_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_details_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "purchase_start_date")
    private LocalDate purchaseStartDate;

    @Column(name = "purchase_end_date")
    private LocalDate purchaseEndDate;

    @Column(name = "event_start_date")
    private LocalDate eventStartDate;

    @Column(name = "event_end_date")
    private LocalDate eventEndDate;

    @Column(name = "announcement")
    private LocalDate announcement;

    @Column(name = "participation_method", columnDefinition = "TEXT")
    private String participationMethod;

    @Column(name = "selection_criteria", columnDefinition = "TEXT")
    private String selectionCriteria;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "precautions", columnDefinition = "TEXT")
    private String precautions;



    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * 팩토리 메서드: 이벤트와 함께 상세정보 생성 (빌더 패턴 활용)
     */
    public static EventDetail createForEvent(Event event, String title, String description, 
                                          String participationMethod, String selectionCriteria, String precautions) {
        EventDetail eventDetail = EventDetail.builder()
                .title(title)
                .description(description)
                .participationMethod(participationMethod)
                .selectionCriteria(selectionCriteria)
                .precautions(precautions)
                .build();
        eventDetail.setEvent(event);
        return eventDetail;
    }

    /**
     * 팩토리 메서드: 날짜 정보와 함께 상세정보 생성 (빌더 패턴 활용)
     */
    public static EventDetail createForEventWithDates(Event event, String title, String description, 
                                                    String participationMethod, String selectionCriteria, String precautions,
                                                    LocalDate purchaseStartDate, LocalDate purchaseEndDate,
                                                    LocalDate eventStartDate, LocalDate eventEndDate, LocalDate announcement) {
        EventDetail eventDetail = EventDetail.builder()
                .title(title)
                .description(description)
                .participationMethod(participationMethod)
                .selectionCriteria(selectionCriteria)
                .precautions(precautions)
                .purchaseStartDate(purchaseStartDate)
                .purchaseEndDate(purchaseEndDate)
                .eventStartDate(eventStartDate)
                .eventEndDate(eventEndDate)
                .announcement(announcement)
                .build();
        eventDetail.setEvent(event);
        return eventDetail;
    }

    /**
     * 이벤트 상세 정보 수정 (빌더 패턴 활용)
     */
    public void updateFromDto(com.cMall.feedShop.event.application.dto.request.EventUpdateRequestDto dto) {
        this.title = dto.getTitle() != null ? dto.getTitle() : this.title;
        this.description = dto.getDescription() != null ? dto.getDescription() : this.description;
        this.purchaseStartDate = dto.getPurchaseStartDate() != null ? dto.getPurchaseStartDate() : this.purchaseStartDate;
        this.purchaseEndDate = dto.getPurchaseEndDate() != null ? dto.getPurchaseEndDate() : this.purchaseEndDate;
        this.eventStartDate = dto.getEventStartDate() != null ? dto.getEventStartDate() : this.eventStartDate;
        this.eventEndDate = dto.getEventEndDate() != null ? dto.getEventEndDate() : this.eventEndDate;
        this.announcement = dto.getAnnouncement() != null ? dto.getAnnouncement() : this.announcement;
        this.participationMethod = dto.getParticipationMethod() != null ? dto.getParticipationMethod() : this.participationMethod;
        this.selectionCriteria = dto.getSelectionCriteria() != null ? dto.getSelectionCriteria() : this.selectionCriteria;
        this.imageUrl = dto.getImageUrl() != null ? dto.getImageUrl() : this.imageUrl;
        this.precautions = dto.getPrecautions() != null ? dto.getPrecautions() : this.precautions;
    }
}
