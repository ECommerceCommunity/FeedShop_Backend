package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewListResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.application.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰 조회 API", description = "리뷰 목록 및 상세 조회 API (로그인 불필요)")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/products/{productId}")
    @ApiResponseFormat(message = "상품 리뷰 목록을 성공적으로 조회했습니다.")
    @Operation(summary = "상품별 리뷰 목록 조회", description = "특정 상품의 리뷰 목록을 조회합니다. 로그인이 필요하지 않습니다.")
    public ApiResponse<ReviewListResponse> getProductReviews(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (1-100)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 방식 (latest: 최신순, points: 인기순)") @RequestParam(defaultValue = "latest") String sort) {
        ReviewListResponse response = reviewService.getProductReviews(productId, page, size, sort);
        return ApiResponse.success(response);
    }

    @GetMapping("/{reviewId}")
    @ApiResponseFormat(message = "리뷰 상세 정보를 성공적으로 조회했습니다.")
    @Operation(summary = "리뷰 상세 조회", description = "특정 리뷰의 상세 정보를 조회합니다. 로그인이 필요하지 않습니다.")
    public ApiResponse<ReviewResponse> getReview(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId) {
        ReviewResponse response = reviewService.getReview(reviewId);
        return ApiResponse.success(response);
    }
}