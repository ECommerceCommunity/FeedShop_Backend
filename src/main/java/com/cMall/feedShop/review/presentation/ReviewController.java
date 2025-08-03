package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.review.application.dto.response.Review3ElementStatisticsResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewListResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.application.service.Review3ElementStatisticsService;
import com.cMall.feedShop.review.application.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰 조회 API", description = "리뷰 목록 및 상세 조회 API (로그인 불필요)")
public class ReviewController {

    private final ReviewService reviewService;
    private final Review3ElementStatisticsService statisticsService;
  
    @GetMapping("/products/{productId}")
    @ApiResponseFormat(message = "상품 리뷰 목록을 성공적으로 조회했습니다.")
    @Operation(summary = "상품별 리뷰 목록 조회", description = "특정 상품의 리뷰 목록을 조회합니다. 로그인이 필요하지 않습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 리뷰 목록을 성공적으로 조회했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 상품을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터입니다.")
    })
    public com.cMall.feedShop.common.dto.ApiResponse<ReviewListResponse> getProductReviews(
            @Parameter(description = "상품 ID") @PathVariable Long productId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (1-100)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 방식 (latest: 최신순, points: 인기순)") @RequestParam(defaultValue = "latest") String sort) {
        ReviewListResponse response = reviewService.getProductReviews(productId, page, size, sort);
        return com.cMall.feedShop.common.dto.ApiResponse.success(response);
    }

    @GetMapping("/{reviewId}")
    @ApiResponseFormat(message = "리뷰 상세 정보를 성공적으로 조회했습니다.")
    @Operation(summary = "리뷰 상세 조회", description = "특정 리뷰의 상세 정보를 조회합니다. 로그인이 필요하지 않습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 상세 정보를 성공적으로 조회했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 리뷰를 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "삭제되었거나 숨김 처리된 리뷰입니다.")
    })
    public com.cMall.feedShop.common.dto.ApiResponse<ReviewResponse> getReview(
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId) {
        ReviewResponse response = reviewService.getReview(reviewId);
        return com.cMall.feedShop.common.dto.ApiResponse.success(response);

    }

    @GetMapping("/products/{productId}/statistics")
    @ApiResponseFormat(message = "상품 3요소 평가 통계를 성공적으로 조회했습니다.")
    @Operation(summary = "상품별 3요소 평가 통계 조회",
            description = "특정 상품의 Cushion, SizeFit, Stability 평가 통계를 조회합니다. 로그인이 필요하지 않습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 3요소 평가 통계를 성공적으로 조회했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 상품을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 상품 ID입니다.")
    })
    public com.cMall.feedShop.common.dto.ApiResponse<Review3ElementStatisticsResponse> getProductStatistics(
            @Parameter(description = "상품 ID") @PathVariable Long productId) {
        Review3ElementStatisticsResponse response = statisticsService.getProductStatistics(productId);
        return com.cMall.feedShop.common.dto.ApiResponse.success(response);
    }

}