package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventCreateResponseDto;
import com.cMall.feedShop.event.application.service.EventCreateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event", description = "이벤트 관리 API")
public class EventCreateController {
    private final EventCreateService eventCreateService;
    private final ObjectMapper objectMapper;

    /**
     * 이벤트 생성 (이미지 없음)
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "이벤트 생성", description = "이미지 없이 이벤트를 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "이벤트 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<ApiResponse<EventCreateResponseDto>> createEvent(
            @Parameter(description = "이벤트 생성 요청 정보", required = true)
            @RequestBody EventCreateRequestDto requestDto) {
        log.info("이벤트 생성 요청 받음: title={}, type={}", requestDto.getTitle(), requestDto.getType());
        
        try {
            EventCreateResponseDto responseDto = eventCreateService.createEvent(requestDto);
            ApiResponse<EventCreateResponseDto> response = ApiResponse.<EventCreateResponseDto>builder()
                    .success(true)
                    .message("이벤트가 성공적으로 생성되었습니다.")
                    .data(responseDto)
                    .build();
            log.info("이벤트 생성 성공: eventId={}", responseDto.getEventId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("이벤트 생성 실패: {}", e.getMessage(), e);
            ApiResponse<EventCreateResponseDto> errorResponse = ApiResponse.<EventCreateResponseDto>builder()
                    .success(false)
                    .message("이벤트 생성에 실패했습니다: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * 이벤트 생성 (이미지 포함)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "이벤트 생성 (이미지 포함)", description = "이미지와 함께 이벤트를 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "이벤트 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<ApiResponse<EventCreateResponseDto>> createEventWithImages(
        @RequestParam("type") String type,
        @RequestParam("title") String title,
        @RequestParam("description") String description,
        @RequestParam(value = "participationMethod", required = false) String participationMethod,
        @RequestParam(value = "selectionCriteria", required = false) String selectionCriteria,
        @RequestParam(value = "precautions", required = false) String precautions,
        @RequestParam("purchaseStartDate") String purchaseStartDate,
        @RequestParam("purchaseEndDate") String purchaseEndDate,
        @RequestParam("eventStartDate") String eventStartDate,
        @RequestParam("eventEndDate") String eventEndDate,
        @RequestParam(value = "announcement", required = false) String announcement,
        @RequestParam("maxParticipants") Integer maxParticipants,
        @RequestParam("rewards") String rewardsJson,
        @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        log.info("이벤트 생성 요청 받음: title={}, type={}", title, type);
        log.info("보상 JSON: {}", rewardsJson);
        
        try {
            // JSON 문자열을 List로 파싱
            List<EventCreateRequestDto.EventRewardRequestDto> rewards = objectMapper.readValue(
                rewardsJson, 
                new TypeReference<List<EventCreateRequestDto.EventRewardRequestDto>>() {}
            );
            log.info("보상 정보 파싱 완료: {}", rewards);
            
            // 이미지 파일 처리
            if (images != null && !images.isEmpty()) {
                log.info("이미지 파일 업로드: {} 개의 파일", images.size());
            } else {
                log.info("이미지 파일 없음 (선택사항)");
            }
            
            // EventCreateRequestDto 생성
            EventCreateRequestDto requestDto = EventCreateRequestDto.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.valueOf(type))
                .title(title)
                .description(description)
                .participationMethod(participationMethod)
                .selectionCriteria(selectionCriteria)
                .precautions(precautions)
                .purchaseStartDate(java.time.LocalDate.parse(purchaseStartDate))
                .purchaseEndDate(java.time.LocalDate.parse(purchaseEndDate))
                .eventStartDate(java.time.LocalDate.parse(eventStartDate))
                .eventEndDate(java.time.LocalDate.parse(eventEndDate))
                .announcement(announcement != null ? java.time.LocalDate.parse(announcement) : null)
                .maxParticipants(maxParticipants)
                .rewards(rewards)
                .build();

            EventCreateResponseDto responseDto = eventCreateService.createEventWithImages(requestDto, images);
            ApiResponse<EventCreateResponseDto> response = ApiResponse.<EventCreateResponseDto>builder()
                    .success(true)
                    .message("이벤트가 성공적으로 생성되었습니다.")
                    .data(responseDto)
                    .build();
            log.info("이벤트 생성 성공: eventId={}", responseDto.getEventId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("이벤트 생성 실패: {}", e.getMessage(), e);
            ApiResponse<EventCreateResponseDto> errorResponse = ApiResponse.<EventCreateResponseDto>builder()
                    .success(false)
                    .message("이벤트 생성에 실패했습니다: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
} 