package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.feed.application.dto.response.FeedVoteResponseDto;
import com.cMall.feedShop.feed.application.service.FeedVoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedVoteController {

    private final FeedVoteService feedVoteService;

    /**
     * 피드 투표
     * - 이벤트 참여 피드에만 투표 가능
     * - 투표 시 자동으로 리워드 지급 (포인트 100점 + 뱃지 점수 2점)
     */
    @PostMapping("/{feedId}/vote")
    public ResponseEntity<ApiResponse<FeedVoteResponseDto>> voteFeed(
            @PathVariable Long feedId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        // UserDetails에서 userId 추출 (실제 구현에서는 JWT 토큰에서 추출)
        Long userId = extractUserIdFromUserDetails(userDetails);
        
        FeedVoteResponseDto response = feedVoteService.voteFeed(feedId, userId);
        
        log.info("피드 투표 완료 - feedId: {}, userId: {}", feedId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 피드 투표 취소
     */
    @DeleteMapping("/{feedId}/vote")
    public ResponseEntity<ApiResponse<String>> cancelVote(
            @PathVariable Long feedId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("인증이 필요합니다."));
        }

        Long userId = extractUserIdFromUserDetails(userDetails);
        
        feedVoteService.cancelVote(feedId, userId);
        
        log.info("피드 투표 취소 완료 - feedId: {}, userId: {}", feedId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("투표가 취소되었습니다."));
    }

    /**
     * 사용자가 특정 피드에 투표했는지 확인
     */
    @GetMapping("/{feedId}/vote/check")
    public ResponseEntity<ApiResponse<Boolean>> hasVoted(
            @PathVariable Long feedId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.ok(ApiResponse.success(false));
        }

        Long userId = extractUserIdFromUserDetails(userDetails);
        boolean hasVoted = feedVoteService.hasVoted(feedId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(hasVoted));
    }

    /**
     * 특정 피드의 투표 개수 조회
     */
    @GetMapping("/{feedId}/vote/count")
    public ResponseEntity<ApiResponse<Long>> getVoteCount(@PathVariable Long feedId) {
        long voteCount = feedVoteService.getVoteCount(feedId);
        return ResponseEntity.ok(ApiResponse.success(voteCount));
    }

    /**
     * 특정 이벤트의 투표 개수 조회
     */
    @GetMapping("/events/{eventId}/vote/count")
    public ResponseEntity<ApiResponse<Long>> getEventVoteCount(@PathVariable Long eventId) {
        long voteCount = feedVoteService.getEventVoteCount(eventId);
        return ResponseEntity.ok(ApiResponse.success(voteCount));
    }

    /**
     * UserDetails에서 userId 추출 (임시 구현)
     * 실제 구현에서는 JWT 토큰에서 userId를 추출해야 함
     */
    private Long extractUserIdFromUserDetails(UserDetails userDetails) {
        // TODO: JWT 토큰에서 userId 추출 로직 구현
        // 현재는 임시로 username을 Long으로 변환
        try {
            return Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }
    }
}
