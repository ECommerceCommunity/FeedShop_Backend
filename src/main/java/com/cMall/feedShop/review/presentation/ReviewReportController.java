package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.review.application.dto.request.ReviewReportRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewReportResponse;
import com.cMall.feedShop.review.application.service.ReviewReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "리뷰 신고 API", description = "리뷰 신고 관련 API")
public class ReviewReportController {

    private final ReviewReportService reviewReportService;

    @PostMapping("/{reviewId}/report")
    @ApiResponseFormat(message = "리뷰 신고가 성공적으로 접수되었습니다.")
    @Operation(summary = "리뷰 신고", description = "부적절한 리뷰를 신고합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 신고가 성공적으로 접수되었습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "자신이 작성한 리뷰는 신고할 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "신고할 리뷰를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 신고한 리뷰입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다.")
    })
    public ApiResponse<ReviewReportResponse> reportReview(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @Valid @RequestBody ReviewReportRequest request,
            Authentication authentication) {

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