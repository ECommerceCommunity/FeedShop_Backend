package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.event.application.service.EventDeleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventDeleteController {
    private final EventDeleteService eventDeleteService;

    /**
     * 이벤트 삭제 (소프트 딜리트)
     */
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long eventId) {
        eventDeleteService.deleteEvent(eventId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("이벤트가 성공적으로 삭제되었습니다.")
                .build();
        return ResponseEntity.ok(response);
    }
} 