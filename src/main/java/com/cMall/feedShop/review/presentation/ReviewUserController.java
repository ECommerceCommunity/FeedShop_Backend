package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.request.ReviewUpdateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewUpdateResponse;
import com.cMall.feedShop.review.application.service.ReviewService;
import com.cMall.feedShop.review.application.service.ReviewImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user/reviews")
@RequiredArgsConstructor
@Tag(name = "사용자 리뷰 API", description = "사용자용 리뷰 작성/수정/삭제 API (로그인 필요)")
public class ReviewUserController {

    private final ReviewService reviewService;
    private final ReviewImageService reviewImageService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰가 성공적으로 작성되었습니다.")
    @Operation(summary = "리뷰 작성", description = "새로운 리뷰를 작성합니다. 로그인이 필요합니다.")
    public ApiResponse<ReviewCreateResponse> createReview(
            @Valid @RequestPart("review") ReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        ReviewCreateResponse response = reviewService.createReview(request, images);
        return ApiResponse.success(response);
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰가 성공적으로 수정되었습니다.")
    @Operation(summary = "리뷰 수정",
            description = "자신이 작성한 리뷰를 수정합니다. 텍스트 정보와 이미지를 함께 수정할 수 있습니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰가 성공적으로 수정되었습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력 데이터입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인이 작성한 리뷰만 수정할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 리뷰를 찾을 수 없습니다.")
    })
    public ApiResponse<ReviewUpdateResponse> updateReview(
            @Parameter(description = "수정할 리뷰 ID")
            @PathVariable Long reviewId,

            @Parameter(description = "수정할 리뷰 정보")
            @Valid @RequestPart("review") ReviewUpdateRequest request,

            @Parameter(description = "새로 추가할 이미지 파일들 (선택사항)")
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) {

        log.info("리뷰 수정 API 호출: reviewId={}, hasNewImages={}",
                reviewId, newImages != null && !newImages.isEmpty());

        ReviewUpdateResponse response = reviewService.updateReview(reviewId, request, newImages);

        log.info("리뷰 수정 API 완료: reviewId={}, 새 이미지 수={}, 삭제된 이미지 수={}",
                reviewId,
                response.getNewImageUrls().size(),
                response.getDeletedImageIds().size());

        return ApiResponse.success(response);
    }

    @PatchMapping("/{reviewId}/title")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰 제목이 성공적으로 수정되었습니다.")
    @Operation(summary = "리뷰 제목 수정", description = "리뷰의 제목만 수정합니다.")
    public ApiResponse<Void> updateReviewTitle(
            @PathVariable Long reviewId,
            @RequestBody @Valid UpdateTitleRequest request) {

        reviewService.updateReviewTitle(reviewId, request.getTitle());
        return ApiResponse.success(null);
    }

    @PatchMapping("/{reviewId}/rating")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰 평점이 성공적으로 수정되었습니다.")
    @Operation(summary = "리뷰 평점 수정", description = "리뷰의 평점만 수정합니다.")
    public ApiResponse<Void> updateReviewRating(
            @PathVariable Long reviewId,
            @RequestBody @Valid UpdateRatingRequest request) {

        reviewService.updateReviewRating(reviewId, request.getRating());
        return ApiResponse.success(null);
    }

    @PatchMapping("/{reviewId}/content")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰 내용이 성공적으로 수정되었습니다.")
    @Operation(summary = "리뷰 내용 수정", description = "리뷰의 내용만 수정합니다.")
    public ApiResponse<Void> updateReviewContent(
            @PathVariable Long reviewId,
            @RequestBody @Valid UpdateContentRequest request) {

        reviewService.updateReviewContent(reviewId, request.getContent());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{reviewId}/images/{imageId}")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "이미지가 성공적으로 삭제되었습니다.")
    @Operation(summary = "리뷰 이미지 삭제", description = "리뷰의 특정 이미지를 삭제합니다.")
    public ApiResponse<Void> deleteReviewImage(
            @PathVariable Long reviewId,
            @PathVariable Long imageId) {

        boolean success = reviewImageService.deleteSingleImage(reviewId, imageId);

        if (!success) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND, "삭제할 이미지를 찾을 수 없습니다.");
        }

        return ApiResponse.success(null);
    }

    // =================== 간단한 수정용 DTO들 ===================

    @Getter
    @NoArgsConstructor
    public static class UpdateTitleRequest {
        @NotBlank(message = "리뷰 제목은 필수입니다.")
        @Size(max = 100, message = "리뷰 제목은 100자를 초과할 수 없습니다.")
        private String title;

        public UpdateTitleRequest(String title) {
            this.title = title;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRatingRequest {
        @NotNull(message = "평점은 필수입니다.")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
        private Integer rating;

        public UpdateRatingRequest(Integer rating) {
            this.rating = rating;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateContentRequest {
        @NotBlank(message = "리뷰 내용은 필수입니다.")
        @Size(min = 10, max = 1000, message = "리뷰 내용은 10자 이상 1000자 이하여야 합니다.")
        private String content;

        public UpdateContentRequest(String content) {
            this.content = content;
        }
    }

    /*
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