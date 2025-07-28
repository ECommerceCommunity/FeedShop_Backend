package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/reviews")
@RequiredArgsConstructor
@Tag(name = "사용자 리뷰 API", description = "사용자용 리뷰 작성/수정/삭제 API (로그인 필요)")
public class ReviewUserController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰가 성공적으로 작성되었습니다.")
    @Operation(summary = "리뷰 작성", description = "새로운 리뷰를 작성합니다. 로그인이 필요합니다.")
    public ApiResponse<ReviewCreateResponse> createReview(
            @Valid @RequestBody ReviewCreateRequest request) {
        ReviewCreateResponse response = reviewService.createReview(request);
        return ApiResponse.success(response);
    }

    // TODO: SPRINT 2에서 추가 예정
    /*
    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰가 성공적으로 수정되었습니다.")
    @Operation(summary = "리뷰 수정", description = "자신이 작성한 리뷰를 수정합니다.")
    public ApiResponse<Void> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        reviewService.updateReview(reviewId, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰가 성공적으로 삭제되었습니다.")
    @Operation(summary = "리뷰 삭제", description = "자신이 작성한 리뷰를 삭제합니다.")
    public ApiResponse<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{reviewId}/points")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰 추천이 완료되었습니다.")
    @Operation(summary = "리뷰 추천", description = "리뷰에 추천을 추가합니다.")
    public ApiResponse<Void> addReviewPoint(@PathVariable Long reviewId) {
        reviewService.addReviewPoint(reviewId);
        return ApiResponse.success(null);
    }
    */
}