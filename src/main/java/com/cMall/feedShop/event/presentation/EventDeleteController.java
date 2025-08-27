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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event", description = "이벤트 관리 API")
public class EventDeleteController {
    private final EventDeleteService eventDeleteService;

    /**
     * 이벤트 삭제 (소프트 딜리트)
     */
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "이벤트 삭제", description = "이벤트를 소프트 딜리트합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이벤트 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @Parameter(description = "삭제할 이벤트 ID", required = true)
            @PathVariable Long eventId) {
        eventDeleteService.deleteEvent(eventId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("이벤트가 성공적으로 삭제되었습니다.")
                .build();
        return ResponseEntity.ok(response);
    }
} 