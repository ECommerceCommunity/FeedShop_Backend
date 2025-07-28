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
        if (image != null && !image.isEmpty()) {
            // TODO: 실제 이미지 업로드 로직 구현
            // 임시로 파일명을 imageUrl로 설정
            String fileName = StringUtils.cleanPath(image.getOriginalFilename());
            requestDto.setImageUrl("/uploads/events/" + fileName);
        }
        
        EventCreateResponseDto responseDto = eventCreateService.createEvent(requestDto);
        ApiResponse<EventCreateResponseDto> response = ApiResponse.<EventCreateResponseDto>builder()
                .success(true)
                .message("이벤트가 성공적으로 생성되었습니다.")
                .data(responseDto)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 