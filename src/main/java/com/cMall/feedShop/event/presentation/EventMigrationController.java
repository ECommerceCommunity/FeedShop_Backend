package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.event.application.service.EventMigrationService;
import com.cMall.feedShop.event.application.service.EventRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 이벤트 마이그레이션 API 컨트롤러
 * 
 * <p>기존 시스템에서 새로운 시스템으로의 점진적 마이그레이션을 위한 API를 제공합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/events/migration")
@RequiredArgsConstructor
public class EventMigrationController {

    private final EventMigrationService eventMigrationService;
    private final EventRewardService eventRewardService;

    /**
     * 특정 이벤트 데이터 마이그레이션
     * 
     * @param eventId 이벤트 ID
     * @return 마이그레이션 결과
     */
    @PostMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventMigrationService.MigrationResult>> migrateEventData(
            @PathVariable Long eventId) {
        
        log.info("이벤트 데이터 마이그레이션 API 호출 - eventId: {}", eventId);
        
        EventMigrationService.MigrationResult result = eventMigrationService.migrateEventData(eventId);
        
        return ResponseEntity.ok(ApiResponse.success("이벤트 데이터 마이그레이션이 완료되었습니다.", result));
    }

    /**
     * 모든 이벤트 데이터 마이그레이션
     * 
     * @return 전체 마이그레이션 결과
     */
    @PostMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EventMigrationService.MigrationResult>>> migrateAllEventData() {
        
        log.info("전체 이벤트 데이터 마이그레이션 API 호출");
        
        List<EventMigrationService.MigrationResult> results = eventMigrationService.migrateAllEventData();
        
        return ResponseEntity.ok(ApiResponse.success("전체 이벤트 데이터 마이그레이션이 완료되었습니다.", results));
    }

    /**
     * 이벤트 리워드 지급
     * 
     * @param eventId 이벤트 ID
     * @return 리워드 지급 결과
     */
    @PostMapping("/{eventId}/rewards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventRewardService.RewardProcessResult>> processEventRewards(
            @PathVariable Long eventId) {
        
        log.info("이벤트 리워드 지급 API 호출 - eventId: {}", eventId);
        
        EventRewardService.RewardProcessResult result = eventRewardService.processEventRewards(eventId);
        
        return ResponseEntity.ok(ApiResponse.success("이벤트 리워드 지급이 완료되었습니다.", result));
    }

    /**
     * 특정 참여자 리워드 재지급
     * 
     * @param eventId 이벤트 ID
     * @param userId 사용자 ID
     * @return 재지급 결과
     */
    @PostMapping("/{eventId}/rewards/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventRewardService.RewardDetail>> reprocessParticipantReward(
            @PathVariable Long eventId,
            @PathVariable Long userId) {
        
        log.info("참여자 리워드 재지급 API 호출 - eventId: {}, userId: {}", eventId, userId);
        
        EventRewardService.RewardDetail result = eventRewardService.reprocessParticipantReward(eventId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("참여자 리워드 재지급이 완료되었습니다.", result));
    }
}
