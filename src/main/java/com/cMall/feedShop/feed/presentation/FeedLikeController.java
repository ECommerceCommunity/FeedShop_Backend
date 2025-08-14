package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.response.LikeToggleResponseDto;
import com.cMall.feedShop.feed.application.dto.response.LikeUserResponseDto;
import com.cMall.feedShop.feed.application.service.FeedLikeService;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
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
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedLikeController {

    private final FeedLikeService feedLikeService;
    private final UserRepository userRepository;

    @PostMapping("/{feedId}/likes/toggle")
    @ApiResponseFormat(message = "좋아요 상태가 변경되었습니다.", status = 200)
    public ResponseEntity<ApiResponse<LikeToggleResponseDto>> toggleLike(@PathVariable Long feedId,
                                                                         @AuthenticationPrincipal UserDetails userDetails) {
        log.info("좋아요 토글 요청 - feedId: {}", feedId);
        
        // 사용자 ID 추출
        Long userId = getUserIdFromUserDetails(userDetails);
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보를 찾을 수 없습니다."));
        }
        
        LikeToggleResponseDto result = feedLikeService.toggleLike(feedId, userId);
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
        
        // 사용자 ID 추출
        Long userId = getUserIdFromUserDetails(userDetails);
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보를 찾을 수 없습니다."));
        }
        
        List<Long> result = feedLikeService.getMyLikedFeedIds(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * UserDetails에서 사용자 ID를 추출하는 헬퍼 메서드
     *
     * @param userDetails JWT 토큰에서 추출된 사용자 정보
     * @return 사용자 ID
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("UserDetails가 null입니다.");
            return null;
        }

        String loginId = userDetails.getUsername();
        log.debug("UserDetails에서 사용자 정보 추출 완료");

        Optional<User> userOptional = userRepository.findByLoginId(loginId);
        if (userOptional.isEmpty()) {
            log.warn("login_id로 사용자를 찾을 수 없습니다");
            return null;
        }

        User user = userOptional.get();
        log.debug("사용자 ID 추출 완료: {}", user.getId());
        return user.getId();
    }
}
