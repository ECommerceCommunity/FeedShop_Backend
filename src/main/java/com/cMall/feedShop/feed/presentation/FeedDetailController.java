package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.service.FeedDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 피드 상세 조회 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedDetailController {

    private final FeedDetailService feedDetailService;

    /**
     * 피드 상세 조회 (FD-803)
     *
     * @param feedId 피드 ID
     * @return 피드 상세 정보
     */
    @GetMapping("/{feedId}")
    public ResponseEntity<ApiResponse<FeedDetailResponseDto>> getFeedDetail(@PathVariable Long feedId) {
        log.info("피드 상세 조회 요청 - feedId: {}", feedId);

        try {
            FeedDetailResponseDto feedDetail = feedDetailService.getFeedDetail(feedId);
            
            ApiResponse<FeedDetailResponseDto> response = ApiResponse.<FeedDetailResponseDto>builder()
                    .success(true)
                    .message("피드 상세 정보를 성공적으로 조회했습니다.")
                    .data(feedDetail)
                    .build();

            log.info("피드 상세 조회 완료 - feedId: {}", feedId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("피드 상세 조회 실패 - feedId: {}, 오류: {}", feedId, e.getMessage());
            
            ApiResponse<FeedDetailResponseDto> errorResponse = ApiResponse.<FeedDetailResponseDto>builder()
                    .success(false)
                    .message("피드 상세 조회에 실패했습니다: " + e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
