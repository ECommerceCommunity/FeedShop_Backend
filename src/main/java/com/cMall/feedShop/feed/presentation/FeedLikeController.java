package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.feed.application.dto.response.LikeToggleResponseDto;
import com.cMall.feedShop.feed.application.service.FeedLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedLikeController {

    private final FeedLikeService feedLikeService;

    @PostMapping("/{feedId}/likes/toggle")
    @ApiResponseFormat(message = "좋아요 상태가 변경되었습니다.", status = 200)
    public ResponseEntity<ApiResponse<LikeToggleResponseDto>> toggleLike(@PathVariable Long feedId,
                                                                         @AuthenticationPrincipal UserDetails userDetails) {
        log.info("좋아요 토글 요청 - feedId: {}", feedId);
        LikeToggleResponseDto result = feedLikeService.toggleLike(feedId, userDetails);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
