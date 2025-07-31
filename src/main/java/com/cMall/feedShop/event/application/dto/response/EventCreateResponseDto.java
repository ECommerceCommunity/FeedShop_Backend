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
    private java.time.LocalDateTime createdAt;

    public static EventCreateResponseDto of(Long eventId, String title, String type, String status, Integer maxParticipants, java.time.LocalDateTime createdAt) {
        return EventCreateResponseDto.builder()
                .eventId(eventId)
                .title(title)
                .type(type)
                .status(status)
                .maxParticipants(maxParticipants)
                .createdAt(createdAt)
                .build();
    }
} 