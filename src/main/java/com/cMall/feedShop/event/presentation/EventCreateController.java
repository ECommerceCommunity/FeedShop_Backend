package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventCreateResponseDto;
import com.cMall.feedShop.event.application.service.EventCreateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventCreateController {
    private final EventCreateService eventCreateService;

    /**
     * 이벤트 생성
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventCreateResponseDto>> createEvent(
        @ModelAttribute EventCreateRequestDto requestDto,
        @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        // 이미지 파일 처리
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            // TODO: 실제 이미지 업로드 로직 구현
            // 임시로 파일명을 imageUrl로 설정
            String fileName = StringUtils.cleanPath(image.getOriginalFilename());
            imageUrl = "/uploads/events/" + fileName;
        }
        
        // 이미지 URL이 있는 경우 새로운 DTO 생성
        EventCreateRequestDto finalRequestDto = requestDto;
        if (imageUrl != null) {
            finalRequestDto = EventCreateRequestDto.builder()
                .type(requestDto.getType())
                .maxParticipants(requestDto.getMaxParticipants())
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .imageUrl(imageUrl)
                .participationMethod(requestDto.getParticipationMethod())
                .selectionCriteria(requestDto.getSelectionCriteria())
                .precautions(requestDto.getPrecautions())
                .purchaseStartDate(requestDto.getPurchaseStartDate())
                .purchaseEndDate(requestDto.getPurchaseEndDate())
                .eventStartDate(requestDto.getEventStartDate())
                .eventEndDate(requestDto.getEventEndDate())
                .announcement(requestDto.getAnnouncement())
                .rewards(requestDto.getRewards())
                .build();
        } else {
            // 이미지가 없는 경우에도 기존 DTO 사용
            finalRequestDto = requestDto;
        }

        EventCreateResponseDto responseDto = eventCreateService.createEvent(finalRequestDto);
        ApiResponse<EventCreateResponseDto> response = ApiResponse.<EventCreateResponseDto>builder()
                .success(true)
                .message("이벤트가 성공적으로 생성되었습니다.")
                .data(responseDto)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 