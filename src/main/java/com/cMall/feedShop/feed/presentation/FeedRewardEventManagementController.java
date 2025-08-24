package com.cMall.feedShop.feed.presentation;

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
    public ResponseEntity<Page<FeedRewardEventResponseDto>> getRewardEvents(
            @ModelAttribute FeedRewardEventSearchRequest request) {
        
        log.info("리워드 이벤트 목록 조회 요청 - request: {}", request);
        
        try {
            Page<FeedRewardEventResponseDto> response = feedRewardEventManagementService.getRewardEvents(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청 파라미터 - {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("리워드 이벤트 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 사용자의 리워드 이벤트 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedRewardEventResponseDto>> getRewardEventsByUser(@PathVariable Long userId) {
        log.info("사용자별 리워드 이벤트 조회 요청 - userId: {}", userId);
        
        try {
            List<FeedRewardEventResponseDto> response = feedRewardEventManagementService.getRewardEventsByUser(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자별 리워드 이벤트 조회 중 오류 발생 - userId: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 피드의 리워드 이벤트 조회
     */
    @GetMapping("/feed/{feedId}")
    public ResponseEntity<List<FeedRewardEventResponseDto>> getRewardEventsByFeed(@PathVariable Long feedId) {
        log.info("피드별 리워드 이벤트 조회 요청 - feedId: {}", feedId);
        
        try {
            List<FeedRewardEventResponseDto> response = feedRewardEventManagementService.getRewardEventsByFeed(feedId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("피드별 리워드 이벤트 조회 중 오류 발생 - feedId: {}", feedId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 이벤트 상세 조회
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<FeedRewardEventResponseDto> getRewardEventDetail(@PathVariable Long eventId) {
        log.info("리워드 이벤트 상세 조회 요청 - eventId: {}", eventId);
        
        try {
            FeedRewardEventResponseDto response = feedRewardEventManagementService.getRewardEventDetail(eventId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("리워드 이벤트를 찾을 수 없음 - eventId: {}", eventId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("리워드 이벤트 상세 조회 중 오류 발생 - eventId: {}", eventId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 리워드 이벤트 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRewardEventStatistics() {
        log.info("리워드 이벤트 통계 조회 요청");
        
        try {
            Map<String, Object> response = feedRewardEventManagementService.getRewardEventStatistics();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("리워드 이벤트 통계 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 일별 리워드 이벤트 통계 조회
     */
    @GetMapping("/statistics/daily")
    public ResponseEntity<Map<String, Object>> getDailyRewardEventStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("일별 리워드 이벤트 통계 조회 요청 - startDate: {}, endDate: {}", startDate, endDate);
        
        try {
            Map<String, Object> response = feedRewardEventManagementService.getDailyRewardEventStatistics(startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("일별 리워드 이벤트 통계 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 수동으로 특정 이벤트 처리
     */
    @PostMapping("/{eventId}/process")
    public ResponseEntity<Void> processEventManually(@PathVariable Long eventId) {
        log.info("수동 리워드 이벤트 처리 요청 - eventId: {}", eventId);
        
        try {
            feedRewardEventManagementService.processEventManually(eventId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("리워드 이벤트 처리 실패 - eventId: {}, reason: {}", eventId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("수동 리워드 이벤트 처리 중 오류 발생 - eventId: {}", eventId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 실패한 이벤트 재처리
     */
    @PostMapping("/retry-failed")
    public ResponseEntity<Void> retryFailedEvents() {
        log.info("실패한 리워드 이벤트 재처리 요청");
        
        try {
            feedRewardEventManagementService.retryFailedEvents();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("실패한 리워드 이벤트 재처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 리워드 이벤트 상태별 요약 정보
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getRewardEventSummary() {
        log.info("리워드 이벤트 요약 정보 조회 요청");
        
        try {
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
        } catch (Exception e) {
            log.error("리워드 이벤트 요약 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
