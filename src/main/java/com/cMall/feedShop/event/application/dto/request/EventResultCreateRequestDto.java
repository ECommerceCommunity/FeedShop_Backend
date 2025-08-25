package com.cMall.feedShop.event.application.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이벤트 결과 생성 요청 DTO
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventResultCreateRequestDto {
    
    private Long eventId;
    private Boolean forceRecalculate;
    
    @Builder
    public EventResultCreateRequestDto(Long eventId, Boolean forceRecalculate) {
        this.eventId = eventId;
        this.forceRecalculate = forceRecalculate != null ? forceRecalculate : false;
    }
}
