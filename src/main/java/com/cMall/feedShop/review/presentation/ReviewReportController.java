package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.review.application.dto.request.ReviewReportRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewReportResponse;
import com.cMall.feedShop.review.application.service.ReviewReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "리뷰 신고 API", description = "사용자용 리뷰 신고 API (로그인 필요)")
@SecurityRequirement(name = "jwtAuth")
public class ReviewReportController {

    private final ReviewReportService reviewReportService;

    @PostMapping("/{reviewId}/report")
    @ApiResponseFormat(message = "리뷰 신고가 성공적으로 접수되었습니다.")
    @Operation(
            summary = "리뷰 신고",
            description = "부적절한 리뷰를 신고합니다. 신고 사유와 상세 내용을 입력해야 하며, 자신이 작성한 리뷰는 신고할 수 없습니다. 동일한 리뷰를 중복 신고할 수도 없습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "리뷰 신고 접수 성공",
                    content = @Content(schema = @Schema(implementation = ReviewReportResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (자신의 리뷰 신고, 유효성 검증 실패)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자 (로그인 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "신고할 리뷰를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 신고한 리뷰 (중복 신고 불가)"
            )
    })
    public ApiResponse<ReviewReportResponse> reportReview(
            @Parameter(description = "신고할 리뷰 ID", required = true, example = "123") @PathVariable Long reviewId,
            @Parameter(description = "리뷰 신고 요청 정보 (신고 사유, 상세 내용 포함)", required = true) @Valid @RequestBody ReviewReportRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        String userLoginId = getUserLoginIdFromAuthentication(authentication);
        ReviewReportResponse response = reviewReportService.reportReview(reviewId, userLoginId, request);
        return ApiResponse.success(response);
    }

    private String getUserLoginIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String username = null;

        log.info("DEBUG - Authentication principal 타입: {}", principal.getClass().getSimpleName());
        
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
            log.info("DEBUG - JWT 토큰에서 추출한 username (UserDetails): {}", username);
        } else {
            username = authentication.getName();
            log.info("DEBUG - JWT 토큰에서 추출한 username (getName): {}", username);
        }
        
        log.info("DEBUG - 최종 반환할 loginId: {}", username);
        return username;
    }
}