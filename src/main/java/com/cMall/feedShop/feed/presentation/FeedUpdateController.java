package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.feed.application.dto.request.FeedUpdateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.service.FeedUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedUpdateController {

    private final FeedUpdateService feedUpdateService;

    /**
     * 피드 수정 (이미지 포함) (FD-805)
     */
    @PutMapping(value = "/{feedId}", consumes = {"multipart/form-data"})
    @ApiResponseFormat(message = "피드를 성공적으로 수정했습니다.", status = 200)
    public ResponseEntity<ApiResponse<FeedDetailResponseDto>> updateFeedWithImages(
            @PathVariable Long feedId,
            @RequestPart("feedData") @Valid FeedUpdateRequestDto request,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("피드 수정 요청 (이미지 포함) - feedId: {}", feedId);
        FeedDetailResponseDto result = feedUpdateService.updateFeedWithImages(feedId, request, newImages, userDetails);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 피드 수정 (이미지 없음) (FD-805)
     */
    @PutMapping(value = "/{feedId}/text-only", consumes = {"application/json"})
    @ApiResponseFormat(message = "피드를 성공적으로 수정했습니다.", status = 200)
    public ResponseEntity<ApiResponse<FeedDetailResponseDto>> updateFeed(
            @PathVariable Long feedId,
            @Valid @RequestBody FeedUpdateRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("피드 수정 요청 (텍스트만) - feedId: {}", feedId);
        FeedDetailResponseDto result = feedUpdateService.updateFeed(feedId, request, userDetails);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
