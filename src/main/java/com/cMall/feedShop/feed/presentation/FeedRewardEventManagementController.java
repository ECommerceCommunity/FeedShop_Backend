package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.feed.application.dto.request.FeedRewardEventSearchRequest;
import com.cMall.feedShop.feed.application.dto.response.FeedRewardEventResponseDto;
import com.cMall.feedShop.feed.application.service.FeedRewardEventManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 피드 리워드 이벤트 관리 컨트롤러
 * 관리자가 리워드 이벤트를 조회하고 관리할 수 있는 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/feed-reward-events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FeedRewardEventManagementController {

    private final FeedRewardEventManagementService feedRewardEventManagementService;

    /**
     * 리워드 이벤트 목록 조회 (페이지네이션)
     */
    @GetMapping
    @ApiResponseFormat(message = "리워드 이벤트 목록을 성공적으로 조회했습니다.")
    public ResponseEntity<Page<FeedRewardEventResponseDto>> getRewardEvents(
            @ModelAttribute FeedRewardEventSearchRequest request) {
        
        log.info("리워드 이벤트 목록 조회 요청 - request: {}", request);
        Page<FeedRewardEventResponseDto> response = feedRewardEventManagementService.getRewardEvents(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 사용자의 리워드 이벤트 조회
     */
    @GetMapping("/user/{userId}")
    @ApiResponseFormat(message = "사용자별 리워드 이벤트를 성공적으로 조회했습니다.")
    public ResponseEntity<List<FeedRewardEventResponseDto>> getRewardEventsByUser(@PathVariable Long userId) {
        log.info("사용자별 리워드 이벤트 조회 요청 - userId: {}", userId);
        List<FeedRewardEventResponseDto> response = feedRewardEventManagementService.getRewardEventsByUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 피드의 리워드 이벤트 조회
     */
    @GetMapping("/feed/{feedId}")
    @ApiResponseFormat(message = "피드별 리워드 이벤트를 성공적으로 조회했습니다.")
    public ResponseEntity<List<FeedRewardEventResponseDto>> getRewardEventsByFeed(@PathVariable Long feedId) {
        log.info("피드별 리워드 이벤트 조회 요청 - feedId: {}", feedId);
        List<FeedRewardEventResponseDto> response = feedRewardEventManagementService.getRewardEventsByFeed(feedId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 이벤트 상세 조회
     */
    @GetMapping("/{eventId}")
    @ApiResponseFormat(message = "리워드 이벤트 상세 정보를 성공적으로 조회했습니다.")
    public ResponseEntity<FeedRewardEventResponseDto> getRewardEventDetail(@PathVariable Long eventId) {
        log.info("리워드 이벤트 상세 조회 요청 - eventId: {}", eventId);
        FeedRewardEventResponseDto response = feedRewardEventManagementService.getRewardEventDetail(eventId);
        return ResponseEntity.ok(response);
    }

    /**
     * 리워드 이벤트 통계 조회
     */
    @GetMapping("/statistics")
    @ApiResponseFormat(message = "리워드 이벤트 통계를 성공적으로 조회했습니다.")
    public ResponseEntity<Map<String, Object>> getRewardEventStatistics() {
        log.info("리워드 이벤트 통계 조회 요청");
        Map<String, Object> response = feedRewardEventManagementService.getRewardEventStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * 일별 리워드 이벤트 통계 조회
     */
    @GetMapping("/statistics/daily")
    @ApiResponseFormat(message = "일별 리워드 이벤트 통계를 성공적으로 조회했습니다.")
    public ResponseEntity<Map<String, Object>> getDailyRewardEventStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("일별 리워드 이벤트 통계 조회 요청 - startDate: {}, endDate: {}", startDate, endDate);
        Map<String, Object> response = feedRewardEventManagementService.getDailyRewardEventStatistics(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * 수동으로 특정 이벤트 처리
     */
    @PostMapping("/{eventId}/process")
    @ApiResponseFormat(message = "리워드 이벤트를 수동으로 성공적으로 처리했습니다.")
    public ResponseEntity<Void> processEventManually(@PathVariable Long eventId) {
        log.info("수동 리워드 이벤트 처리 요청 - eventId: {}", eventId);
        feedRewardEventManagementService.processEventManually(eventId);
        return ResponseEntity.ok().build();
    }

    /**
     * 실패한 이벤트 재처리
     */
    @PostMapping("/retry-failed")
    @ApiResponseFormat(message = "실패한 리워드 이벤트를 성공적으로 재처리했습니다.")
    public ResponseEntity<Void> retryFailedEvents() {
        log.info("실패한 리워드 이벤트 재처리 요청");
        feedRewardEventManagementService.retryFailedEvents();
        return ResponseEntity.ok().build();
    }

    /**
     * 리워드 이벤트 상태별 요약 정보
     */
    @GetMapping("/summary")
    @ApiResponseFormat(message = "리워드 이벤트 요약 정보를 성공적으로 조회했습니다.")
    public ResponseEntity<Map<String, Object>> getRewardEventSummary() {
        log.info("리워드 이벤트 요약 정보 조회 요청");
        Map<String, Object> statistics = feedRewardEventManagementService.getRewardEventStatistics();
        
        // 요약 정보만 추출
        Map<String, Object> summary = Map.of(
                "totalEvents", statistics.get("totalEvents"),
                "pendingEvents", statistics.get("pendingEvents"),
                "processedEvents", statistics.get("processedEvents"),
                "failedEvents", statistics.get("failedEvents"),
                "totalPoints", statistics.get("totalPoints")
        );
        
        return ResponseEntity.ok(summary);
    }
}
