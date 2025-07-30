package com.cMall.feedShop.event.application.dto.request;

import com.cMall.feedShop.event.domain.enums.EventType;
import lombok.*;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventUpdateRequestDto {
    private Long eventId;
    private String title;
    private String description;
    private EventType type;
    private String status;
    
    @Min(value = 1, message = "최대 참여자 수는 1명 이상이어야 합니다.")
    private Integer maxParticipants;
    
    private LocalDate purchaseStartDate;
    private LocalDate purchaseEndDate;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private LocalDate announcement;
    private String participationMethod;
    private String selectionCriteria;
    private String imageUrl;
    private String precautions;
    private String rewards;

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
} 