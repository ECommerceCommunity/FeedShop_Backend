package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.event.application.dto.request.EventResultCreateRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventResultResponseDto;
import com.cMall.feedShop.event.application.service.EventResultManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 이벤트 결과 API 컨트롤러
 * 
 * <p>새로운 아키텍처를 기반으로 하는 이벤트 결과 관련 API 엔드포인트를 제공합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/events")
@RequiredArgsConstructor
public class EventResultController {

    private final EventResultManagementService eventResultManagementService;

    /**
     * 이벤트 결과 생성
     * 
     * @param requestDto 이벤트 결과 생성 요청
     * @return 생성된 이벤트 결과
     */
    @PostMapping("/{eventId}/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventResultResponseDto>> createEventResult(
            @PathVariable Long eventId,
            @RequestBody EventResultCreateRequestDto requestDto) {
        
        log.info("이벤트 결과 생성 API 호출 - eventId: {}", eventId);
        
        // eventId를 요청 DTO에 설정
        requestDto = EventResultCreateRequestDto.builder()
                .eventId(eventId)
                .forceRecalculate(requestDto.getForceRecalculate())
                .build();
        
        EventResultResponseDto result = eventResultManagementService.createEventResult(requestDto);
        
        return ResponseEntity.ok(ApiResponse.success("이벤트 결과가 성공적으로 생성되었습니다.", result));
    }

    /**
     * 이벤트 결과 조회
     * 
     * @param eventId 이벤트 ID
     * @return 이벤트 결과
     */
    @GetMapping("/{eventId}/results")
    public ResponseEntity<ApiResponse<EventResultResponseDto>> getEventResult(@PathVariable Long eventId) {
        log.info("이벤트 결과 조회 API 호출 - eventId: {}", eventId);
        
        EventResultResponseDto result = eventResultManagementService.getEventResult(eventId);
        
        return ResponseEntity.ok(ApiResponse.success("이벤트 결과를 성공적으로 조회했습니다.", result));
    }

    /**
     * 이벤트 결과 존재 여부 확인
     * 
     * @param eventId 이벤트 ID
     * @return 결과 존재 여부
     */
    @GetMapping("/{eventId}/results/exists")
    public ResponseEntity<ApiResponse<Boolean>> hasEventResult(@PathVariable Long eventId) {
        log.info("이벤트 결과 존재 여부 확인 API 호출 - eventId: {}", eventId);
        
        boolean exists = eventResultManagementService.hasEventResult(eventId);
        
        return ResponseEntity.ok(ApiResponse.success("이벤트 결과 존재 여부를 확인했습니다.", exists));
    }

    /**
     * 이벤트 결과 삭제
     * 
     * @param eventId 이벤트 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{eventId}/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEventResult(@PathVariable Long eventId) {
        log.info("이벤트 결과 삭제 API 호출 - eventId: {}", eventId);
        
        eventResultManagementService.deleteEventResult(eventId);
        
        return ResponseEntity.ok(ApiResponse.success("이벤트 결과가 성공적으로 삭제되었습니다.", null));
    }

    /**
     * 이벤트 결과 재계산
     * 
     * @param eventId 이벤트 ID
     * @return 재계산된 이벤트 결과
     */
    @PostMapping("/{eventId}/results/recalculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventResultResponseDto>> recalculateEventResult(@PathVariable Long eventId) {
        log.info("이벤트 결과 재계산 API 호출 - eventId: {}", eventId);
        
        EventResultResponseDto result = eventResultManagementService.recalculateEventResult(eventId);
        
        return ResponseEntity.ok(ApiResponse.success("이벤트 결과가 성공적으로 재계산되었습니다.", result));
    }
}
