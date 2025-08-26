package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.review.application.dto.response.ReportedReviewResponse;
import com.cMall.feedShop.review.application.service.ReviewReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰 신고 관리 API", description = "관리자용 리뷰 신고 관리 API")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class ReviewReportAdminController {

    private final ReviewReportService reviewReportService;

    @GetMapping("/reports")
    @ApiResponseFormat(message = "신고된 리뷰 목록을 성공적으로 조회했습니다.")
    @Operation(summary = "신고된 리뷰 목록 조회", description = "관리자가 신고된 리뷰 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신고된 리뷰 목록을 성공적으로 조회했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    public ApiResponse<PaginatedResponse<ReportedReviewResponse>> getReportedReviews(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        PaginatedResponse<ReportedReviewResponse> response = reviewReportService.getReportedReviews(page, size);
        return ApiResponse.success(response);
    }

    @PostMapping("/{reviewId}/hide")
    @ApiResponseFormat(message = "리뷰가 성공적으로 숨김 처리되었습니다.")
    @Operation(summary = "리뷰 숨김 처리", description = "관리자가 신고된 리뷰를 숨김 처리합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰가 성공적으로 숨김 처리되었습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "숨김 처리할 리뷰를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    public ApiResponse<Void> hideReview(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            Authentication authentication) {

        String adminLoginId = getUserLoginIdFromAuthentication(authentication);

        reviewReportService.hideReview(reviewId, adminLoginId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{reviewId}/show")
    @ApiResponseFormat(message = "리뷰 숨김이 성공적으로 해제되었습니다.")
    @Operation(summary = "리뷰 숨김 해제", description = "관리자가 숨김 처리된 리뷰를 다시 노출시킵니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 숨김이 성공적으로 해제되었습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "숨김 해제할 리뷰를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.")
    })
    public ApiResponse<Void> showReview(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            Authentication authentication) {

        String adminLoginId = getUserLoginIdFromAuthentication(authentication);

        reviewReportService.showReview(reviewId, adminLoginId);
        return ApiResponse.success(null);
    }

    private String getUserLoginIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return authentication.getName();
        }
    }
}