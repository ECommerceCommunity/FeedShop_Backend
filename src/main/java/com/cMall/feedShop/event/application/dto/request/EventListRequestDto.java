// 이벤트 목록 조회 요청 DTO
package com.cMall.feedShop.event.application.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EventListRequestDto {
    private Integer page; // 페이지 번호
    private Integer size; // 페이지당 개수
    private String status; // 이벤트 상태 (upcoming/ongoing/ended/all)
    private String type; // 이벤트 유형 (battle/mission/multiple/all)
    private String keyword; // 이벤트명 검색어
    private String sort; // 정렬 기준 (event_start_date,desc 등)
} 