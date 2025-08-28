package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventListResponseDto;
import com.cMall.feedShop.event.application.dto.response.EventDetailResponseDto;
import com.cMall.feedShop.event.application.service.EventReadService;
import com.cMall.feedShop.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.cMall.feedShop.event.application.dto.response.EventSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event", description = "이벤트 관리 API")
public class EventReadController {
    private final EventReadService eventReadService;

    /**
     * 전체 이벤트 목록 조회 (페이징)
     */
    @GetMapping("/all")
    @Operation(summary = "전체 이벤트 목록 조회", description = "페이징을 지원하는 전체 이벤트 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이벤트 목록 조회 성공")
    })
    public EventListResponseDto getAllEvents(
            @Parameter(description = "페이지 번호 (0부터 시작)", required = false)
            @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", required = false)
            @RequestParam(required = false) Integer size,
            @Parameter(description = "정렬 기준", required = false)
            @RequestParam(required = false) String sort) {
        return eventReadService.getAllEvents(page, size, sort);
    }

    /**
     * 이벤트 검색/필터/정렬 (QueryDSL 기반)
     */
    @GetMapping("/search")
    @Operation(summary = "이벤트 검색/필터/정렬", description = "QueryDSL 기반으로 이벤트를 검색, 필터링, 정렬합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이벤트 검색 성공")
    })
    public EventListResponseDto searchEvents(
            @Parameter(description = "이벤트 검색 요청 정보", required = true)
            @ModelAttribute EventListRequestDto requestDto) {
        return eventReadService.searchEvents(requestDto);
    }

    /**
     * 이벤트 상세 조회
     */
    @GetMapping("/{eventId}")
    @Operation(summary = "이벤트 상세 조회", description = "특정 이벤트의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이벤트 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    public EventDetailResponseDto getEventDetail(
            @Parameter(description = "조회할 이벤트 ID", required = true)
            @PathVariable Long eventId) {
        return eventReadService.getEventDetail(eventId);
    }

    /**
     * 피드 생성에 참여 가능한 이벤트 목록 조회
     */
    @GetMapping("/feed-available")
    @Operation(summary = "피드 참여 가능 이벤트 목록", description = "피드 생성에 참여 가능한 이벤트 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이벤트 목록 조회 성공")
    })
    public ApiResponse<List<EventSummaryDto>> getFeedAvailableEvents() {
        List<EventSummaryDto> events = eventReadService.getFeedAvailableEvents();
        return ApiResponse.success(events);
    }
} 