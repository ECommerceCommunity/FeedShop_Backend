package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.response.FeedVoteResponseDto;
import com.cMall.feedShop.feed.application.service.FeedVoteService;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@Tag(name = "Feed Vote", description = "피드 투표 관련 API")
public class FeedVoteController {

    private final FeedVoteService feedVoteService;
    private final UserRepository userRepository;

    /**
     * 피드 투표
     * - 이벤트 참여 피드에만 투표 가능
     * - 투표 시 자동으로 리워드 지급 (포인트 100점 + 뱃지 점수 2점)
     */
    @PostMapping("/{feedId}/vote")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "피드 투표", description = "이벤트 참여 피드에 투표하고 자동으로 리워드를 지급합니다.")
    public ResponseEntity<ApiResponse<FeedVoteResponseDto>> voteFeed(
            @Parameter(description = "피드 ID") @PathVariable Long feedId,
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "피드 투표 취소", description = "피드 투표를 취소합니다.")
    public ResponseEntity<ApiResponse<String>> cancelVote(
            @Parameter(description = "피드 ID") @PathVariable Long feedId,
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
    @Operation(summary = "투표 여부 확인", description = "사용자가 특정 피드에 투표했는지 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> hasVoted(
            @Parameter(description = "피드 ID") @PathVariable Long feedId,
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
    @Operation(summary = "투표 개수 조회", description = "특정 피드의 투표 개수를 조회합니다.")
    public ResponseEntity<ApiResponse<Long>> getVoteCount(
            @Parameter(description = "피드 ID") @PathVariable Long feedId) {
        long voteCount = feedVoteService.getVoteCount(feedId);
        return ResponseEntity.ok(ApiResponse.success(voteCount));
    }

    /**
     * 특정 이벤트의 투표 개수 조회
     */
    @GetMapping("/events/{eventId}/vote/count")
    @Operation(summary = "이벤트 투표 개수 조회", description = "특정 이벤트의 총 투표 개수를 조회합니다.")
    public ResponseEntity<ApiResponse<Long>> getEventVoteCount(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId) {
        long voteCount = feedVoteService.getEventVoteCount(eventId);
        return ResponseEntity.ok(ApiResponse.success(voteCount));
    }

    /**
     * 투표 수 동기화 (관리자용)
     */
    @PostMapping("/{feedId}/vote/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "투표 수 동기화", description = "특정 피드의 투표 수를 실제 투표 데이터와 동기화합니다.")
    public ResponseEntity<ApiResponse<String>> syncVoteCount(
            @Parameter(description = "피드 ID") @PathVariable Long feedId) {
        feedVoteService.syncVoteCount(feedId);
        return ResponseEntity.ok(ApiResponse.success("투표 수 동기화가 완료되었습니다."));
    }

    /**
     * 전체 투표 수 동기화 (관리자용)
     */
    @PostMapping("/vote/sync-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 투표 수 동기화", description = "모든 피드의 투표 수를 실제 투표 데이터와 동기화합니다.")
    public ResponseEntity<ApiResponse<String>> syncAllVoteCounts() {
        feedVoteService.syncAllVoteCounts();
        return ResponseEntity.ok(ApiResponse.success("전체 투표 수 동기화가 완료되었습니다."));
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
