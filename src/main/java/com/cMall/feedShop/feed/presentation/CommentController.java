package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.request.CommentCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.CommentListResponseDto;
import com.cMall.feedShop.feed.application.dto.response.CommentResponseDto;
import com.cMall.feedShop.feed.application.service.CommentService;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 API")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    /**
     * 댓글 생성
     */
    @PostMapping("/{feedId}/comments")
    @PreAuthorize("hasRole('USER')")
    @ApiResponseFormat
    @Operation(summary = "댓글 생성", description = "특정 피드에 댓글을 생성합니다.")
    public ApiResponse<CommentResponseDto> createComment(
            @Parameter(description = "피드 ID") @PathVariable Long feedId,
            @Parameter(description = "댓글 생성 요청") @Valid @RequestBody CommentCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserIdFromUserDetails(userDetails);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        CommentResponseDto responseDto = commentService.createComment(feedId, userId, requestDto);
        return ApiResponse.success(responseDto);
    }

    /**
     * 댓글 목록 조회
     */
    @GetMapping("/{feedId}/comments")
    @ApiResponseFormat
    @Operation(summary = "댓글 목록 조회", description = "특정 피드의 댓글 목록을 페이징으로 조회합니다.")
    public ApiResponse<CommentListResponseDto> getComments(
            @Parameter(description = "피드 ID") @PathVariable Long feedId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        CommentListResponseDto responseDto = commentService.getComments(feedId, page, size);
        return ApiResponse.success(responseDto);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{feedId}/comments/{commentId}")
    @PreAuthorize("hasRole('USER')")
    @ApiResponseFormat
    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다. (댓글 작성자만 가능)")
    public ApiResponse<Void> deleteComment(
            @Parameter(description = "피드 ID") @PathVariable Long feedId,
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserIdFromUserDetails(userDetails);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        commentService.deleteComment(feedId, commentId, userId);
        return ApiResponse.success(null);
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
