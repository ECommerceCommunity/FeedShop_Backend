// 이벤트 목록 조회 응답 DTO
package com.cMall.feedShop.event.application.dto.response;

import lombok.*;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventListResponseDto {
    private List<EventSummaryDto> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
} 