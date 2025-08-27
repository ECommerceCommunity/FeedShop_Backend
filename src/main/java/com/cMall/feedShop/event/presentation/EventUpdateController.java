package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.event.application.dto.request.EventUpdateRequestDto;
import com.cMall.feedShop.event.application.service.EventUpdateService;
import com.cMall.feedShop.event.domain.enums.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event", description = "이벤트 관리 API")
public class EventUpdateController {
    private final EventUpdateService eventUpdateService;

    /**
     * 이벤트 수정 (JSON)
     */
    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "이벤트 수정", description = "JSON 형태로 이벤트를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이벤트 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> updateEvent(
            @PathVariable Long eventId,
            @RequestBody EventUpdateRequestDto requestDto
    ) {
        requestDto.setEventId(eventId);
        eventUpdateService.updateEvent(requestDto);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("이벤트가 성공적으로 수정되었습니다.")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 이벤트 수정 (Multipart - 이미지 포함)
     */
    @PutMapping("/{eventId}/multipart")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "이벤트 수정 (이미지 포함)", description = "이미지와 함께 이벤트를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이벤트 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> updateEventWithImages(
            @PathVariable Long eventId,
            @RequestParam("type") String type,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("participationMethod") String participationMethod,
            @RequestParam("selectionCriteria") String selectionCriteria,
            @RequestParam("precautions") String precautions,
            @RequestParam("purchaseStartDate") String purchaseStartDate,
            @RequestParam("purchaseEndDate") String purchaseEndDate,
            @RequestParam("eventStartDate") String eventStartDate,
            @RequestParam("eventEndDate") String eventEndDate,
            @RequestParam("announcement") String announcement,
            @RequestParam("maxParticipants") String maxParticipants,
            @RequestParam("rewards") String rewardsJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        EventUpdateRequestDto requestDto = EventUpdateRequestDto.builder()
                .type(EventType.valueOf(type.toUpperCase()))
                .title(title)
                .description(description)
                .participationMethod(participationMethod)
                .selectionCriteria(selectionCriteria)
                .precautions(precautions)
                .purchaseStartDate(LocalDate.parse(purchaseStartDate))
                .purchaseEndDate(LocalDate.parse(purchaseEndDate))
                .eventStartDate(LocalDate.parse(eventStartDate))
                .eventEndDate(LocalDate.parse(eventEndDate))
                .announcement(LocalDate.parse(announcement))
                .maxParticipants(Integer.parseInt(maxParticipants))
                .rewards(rewardsJson)
                .build();
        
        requestDto.setEventId(eventId);
        eventUpdateService.updateEventWithImages(requestDto, images);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("이벤트가 성공적으로 수정되었습니다.")
                .build();
        return ResponseEntity.ok(response);
    }
} 