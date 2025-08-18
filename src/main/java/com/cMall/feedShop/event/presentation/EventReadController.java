package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventListResponseDto;
import com.cMall.feedShop.event.application.dto.response.EventDetailResponseDto;
import com.cMall.feedShop.event.application.service.EventReadService;
import com.cMall.feedShop.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.cMall.feedShop.event.application.dto.response.EventSummaryDto;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventReadController {
    private final EventReadService eventReadService;

    /**
     * 전체 이벤트 목록 조회 (페이징)
     */
    @GetMapping("/all")
    public EventListResponseDto getAllEvents(@RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer size) {
        return eventReadService.getAllEvents(page, size);
    }

    /**
     * 이벤트 검색/필터/정렬 (QueryDSL 기반)
     */
    @GetMapping("/search")
    public EventListResponseDto searchEvents(@ModelAttribute EventListRequestDto requestDto) {
        return eventReadService.searchEvents(requestDto);
    }

    /**
     * 이벤트 상세 조회
     */
    @GetMapping("/{eventId}")
    public EventDetailResponseDto getEventDetail(@PathVariable Long eventId) {
        return eventReadService.getEventDetail(eventId);
    }

    /**
     * 피드 생성에 참여 가능한 이벤트 목록 조회
     */
    @GetMapping("/feed-available")
    public ApiResponse<List<EventSummaryDto>> getFeedAvailableEvents() {
        List<EventSummaryDto> events = eventReadService.getFeedAvailableEvents();
        return ApiResponse.success(events);
    }
} 