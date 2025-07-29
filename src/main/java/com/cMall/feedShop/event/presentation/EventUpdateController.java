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

import java.time.LocalDate;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventUpdateController {
    private final EventUpdateService eventUpdateService;

    /**
     * 이벤트 수정 (JSON)
     */
    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
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
     * 이벤트 수정 (Multipart)
     */
    @PutMapping("/{eventId}/multipart")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateEventWithImage(
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
            @RequestParam(value = "image", required = false) MultipartFile image
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
        eventUpdateService.updateEvent(requestDto);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("이벤트가 성공적으로 수정되었습니다.")
                .build();
        return ResponseEntity.ok(response);
    }
} 