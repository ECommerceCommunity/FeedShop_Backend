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

@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@Tag(name = "FeedVote", description = "피드 투표 API")
public class FeedVoteController {

    private final FeedVoteService feedVoteService;
    private final UserRepository userRepository;

    /**
     * 피드 투표
     */
    @PostMapping("/{feedId}/vote")
    @PreAuthorize("hasRole('USER')")
    @ApiResponseFormat
    @Operation(summary = "피드 투표", description = "이벤트 피드에 투표합니다. 한 번만 투표 가능합니다.")
    public ApiResponse<FeedVoteResponseDto> voteFeed(
            @Parameter(description = "피드 ID") @PathVariable Long feedId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserIdFromUserDetails(userDetails);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        FeedVoteResponseDto responseDto = feedVoteService.voteFeed(feedId, userId);
        return ApiResponse.success(responseDto);
    }

    /**
     * 피드 투표 개수 조회
     */
    @GetMapping("/{feedId}/vote/count")
    @ApiResponseFormat
    @Operation(summary = "피드 투표 개수 조회", description = "특정 피드의 투표 개수를 조회합니다.")
    public ApiResponse<Integer> getVoteCount(
            @Parameter(description = "피드 ID") @PathVariable Long feedId) {

        int voteCount = feedVoteService.getVoteCount(feedId);
        return ApiResponse.success(voteCount);
    }

    /**
     * 사용자 투표 여부 확인
     */
    @GetMapping("/{feedId}/vote/check")
    @PreAuthorize("hasRole('USER')")
    @ApiResponseFormat
    @Operation(summary = "투표 여부 확인", description = "현재 사용자가 특정 피드에 투표했는지 확인합니다.")
    public ApiResponse<Boolean> hasVoted(
            @Parameter(description = "피드 ID") @PathVariable Long feedId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserIdFromUserDetails(userDetails);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        boolean hasVoted = feedVoteService.hasVoted(feedId, userId);
        return ApiResponse.success(hasVoted);
    }

    /**
     * UserDetails에서 userId 추출
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("UserDetails가 null입니다.");
            return null;
        }

        String username = userDetails.getUsername();
        log.info("UserDetails에서 추출한 username: {}", username);

        // 1. 먼저 email로 시도
        var userOptional = userRepository.findByEmail(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            log.info("email로 사용자 찾음 - ID: {} (email: {})", user.getId(), username);
            return user.getId();
        }

        // 2. email로 찾지 못하면 loginId로 시도
        userOptional = userRepository.findByLoginId(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            log.info("loginId로 사용자 찾음 - ID: {} (loginId: {})", user.getId(), username);
            return user.getId();
        }

        log.warn("username '{}'로 사용자를 찾을 수 없습니다 (email, loginId 모두 시도)", username);
        return null;
    }
}
