package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.feed.application.service.FeedDeleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedDeleteController {

    private final FeedDeleteService feedDeleteService;

    /**
     * 피드 삭제 (FD-804)
     */
    @DeleteMapping("/{feedId}")
    @ApiResponseFormat(message = "요청하신 피드를 성공적으로 삭제했습니다.", status = 200)
    public ResponseEntity<ApiResponse<Void>> deleteFeed(@PathVariable Long feedId,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        log.info("피드 삭제 요청 - feedId: {}", feedId);
        feedDeleteService.deleteFeed(feedId, userDetails);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
