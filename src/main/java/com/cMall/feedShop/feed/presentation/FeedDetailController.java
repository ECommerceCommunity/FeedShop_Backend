package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.aop.ApiResponseFormat;
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
    @ApiResponseFormat(message = "요청하신 피드 상세 정보를 성공적으로 가져왔습니다.", status = 200)
    public ResponseEntity<ApiResponse<FeedDetailResponseDto>> getFeedDetail(@PathVariable Long feedId) {
        log.info("피드 상세 조회 요청 - feedId: {}", feedId);

        FeedDetailResponseDto feedDetail = feedDetailService.getFeedDetail(feedId);
        return ResponseEntity.ok(ApiResponse.success(feedDetail));
    }
}
