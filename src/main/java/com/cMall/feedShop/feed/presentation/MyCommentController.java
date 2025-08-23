package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.response.MyCommentListResponseDto;
import com.cMall.feedShop.feed.application.service.MyCommentService;
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
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "MyComment", description = "내 댓글 관리 API")
public class MyCommentController {

    private final MyCommentService myCommentService;
    private final UserRepository userRepository;

    /**
     * 내가 작성한 댓글 목록 조회
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @ApiResponseFormat
    @Operation(summary = "내 댓글 목록 조회", description = "내가 작성한 모든 댓글 목록을 페이징으로 조회합니다.")
    public ApiResponse<MyCommentListResponseDto> getMyComments(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserIdFromUserDetails(userDetails);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        MyCommentListResponseDto responseDto = myCommentService.getMyComments(userId, page, size);
        return ApiResponse.success(responseDto);
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
