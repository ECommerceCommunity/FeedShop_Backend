package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.response.LikeToggleResponseDto;
import com.cMall.feedShop.feed.application.dto.response.LikeUserResponseDto;
import com.cMall.feedShop.feed.application.service.FeedLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/{feedId}/likes")
    @ApiResponseFormat(message = "좋아요 사용자 목록입니다.", status = 200)
    public ResponseEntity<ApiResponse<PaginatedResponse<LikeUserResponseDto>>> getLikedUsers(
            @PathVariable Long feedId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("좋아요 사용자 목록 조회 요청 - feedId: {}, page: {}, size: {}", feedId, page, size);
        PaginatedResponse<LikeUserResponseDto> result = feedLikeService.getLikedUsers(feedId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my-likes")
    @ApiResponseFormat(message = "내가 좋아요한 피드 목록입니다.", status = 200)
    public ResponseEntity<ApiResponse<List<Long>>> getMyLikedFeeds(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("내 좋아요 피드 목록 조회 요청");
        List<Long> result = feedLikeService.getMyLikedFeedIds(userDetails);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
