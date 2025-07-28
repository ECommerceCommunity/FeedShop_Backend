package com.cMall.feedShop.event.application.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventCreateResponseDto {
    private Long eventId;
    private String title;
    private String type;
    private String status;
    private Integer maxParticipants;
    private Object createdBy; // LocalDateTime 등 실제 타입에 맞게 조정 가능

    public static EventCreateResponseDto of(Long eventId, String title, String type, String status, Integer maxParticipants, Object createdBy) {
        return EventCreateResponseDto.builder()
                .eventId(eventId)
                .title(title)
                .type(type)
                .status(status)
                .maxParticipants(maxParticipants)
                .createdBy(createdBy)
                .build();
    }
} 