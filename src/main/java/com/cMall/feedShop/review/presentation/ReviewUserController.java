package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.request.ReviewUpdateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewUpdateResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewDeleteResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewImageDeleteResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.application.service.ReviewService;
import com.cMall.feedShop.review.application.service.ReviewImageService;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.model.User;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user/reviews")
@RequiredArgsConstructor
@Tag(name = "사용자 리뷰 API", description = "사용자용 리뷰 작성/수정/삭제 API (로그인 필요)")
public class ReviewUserController {

    private final ReviewService reviewService;
    private final ReviewImageService reviewImageService;
    private final UserRepository userRepository;

    // ============= 리뷰 작성 API =============

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

    // ============= 리뷰 수정 API =============

    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰가 성공적으로 수정되었습니다.")
    @Operation(summary = "리뷰 수정", description = "기존 리뷰를 수정합니다.")
    public ApiResponse<ReviewUpdateResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestPart("review") ReviewUpdateRequest request,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) {

        log.info("리뷰 수정 API 호출: reviewId={}", reviewId);

        // ReviewService의 updateReview 메서드는 3개의 파라미터를 받음
        ReviewUpdateResponse response = reviewService.updateReview(reviewId, request, newImages);

        log.info("리뷰 수정 API 완료: reviewId={}", reviewId);

        return ApiResponse.success(response);
    }

    @PatchMapping("/{reviewId}/rating")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰 평점이 성공적으로 수정되었습니다.")
    @Operation(summary = "리뷰 평점만 수정", description = "리뷰의 평점만 수정합니다.")
    public ApiResponse<Void> updateReviewRating(
            @PathVariable Long reviewId,
            @RequestBody @Valid UpdateRatingRequest request) {

        log.info("리뷰 평점 수정 API 호출: reviewId={}, 새로운 평점={}", reviewId, request.getRating());

        // ReviewService의 updateReviewRating 메서드는 void를 반환
        reviewService.updateReviewRating(reviewId, request.getRating());

        log.info("리뷰 평점 수정 API 완료: reviewId={}", reviewId);

        return ApiResponse.success(null);
    }

    @PatchMapping("/{reviewId}/content")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰 내용이 성공적으로 수정되었습니다.")
    @Operation(summary = "리뷰 내용만 수정", description = "리뷰의 내용만 수정합니다.")
    public ApiResponse<Void> updateReviewContent(
            @PathVariable Long reviewId,
            @RequestBody @Valid UpdateContentRequest request) {

        log.info("리뷰 내용 수정 API 호출: reviewId={}", reviewId);

        // ReviewService의 updateReviewContent 메서드는 void를 반환
        reviewService.updateReviewContent(reviewId, request.getContent());

        log.info("리뷰 내용 수정 API 완료: reviewId={}", reviewId);

        return ApiResponse.success(null);
    }

    // ============= 리뷰 삭제 API =============

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰가 성공적으로 삭제되었습니다.")
    @Operation(summary = "리뷰 삭제", description = "리뷰와 연관된 모든 이미지를 함께 삭제합니다.")
    public ApiResponse<ReviewDeleteResponse> deleteReview(@PathVariable Long reviewId) {

        log.info("리뷰 삭제 API 호출: reviewId={}", reviewId);

        ReviewDeleteResponse response = reviewService.deleteReview(reviewId);

        log.info("리뷰 삭제 API 완료: reviewId={}, 삭제된 이미지 수={}",
                reviewId, response.getDeletedImageCount());

        return ApiResponse.success(response);
    }

    // ============= 리뷰 이미지 삭제 API =============

    @DeleteMapping("/{reviewId}/images")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "선택된 이미지들이 성공적으로 삭제되었습니다.")
    @Operation(summary = "리뷰 이미지 일괄 삭제", description = "리뷰의 특정 이미지들을 삭제합니다.")
    public ApiResponse<ReviewImageDeleteResponse> deleteReviewImages(
            @PathVariable Long reviewId,
            @RequestBody @Valid DeleteImagesRequest request) {

        log.info("리뷰 이미지 일괄 삭제 API 호출: reviewId={}, imageIds={}",
                reviewId, request.getImageIds());

        ReviewImageDeleteResponse response = reviewService.deleteReviewImages(reviewId, request.getImageIds());

        log.info("리뷰 이미지 일괄 삭제 API 완료: reviewId={}, 삭제된 이미지 수={}",
                reviewId, response.getDeletedImageCount());

        return ApiResponse.success(response);
    }

    @DeleteMapping("/{reviewId}/images/all")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰의 모든 이미지가 성공적으로 삭제되었습니다.")
    @Operation(summary = "리뷰 이미지 전체 삭제", description = "리뷰의 모든 이미지를 삭제합니다. (리뷰 텍스트는 유지)")
    public ApiResponse<ReviewImageDeleteResponse> deleteAllReviewImages(@PathVariable Long reviewId) {

        log.info("리뷰 이미지 전체 삭제 API 호출: reviewId={}", reviewId);

        ReviewImageDeleteResponse response = reviewService.deleteAllReviewImages(reviewId);

        log.info("리뷰 이미지 전체 삭제 API 완료: reviewId={}, 삭제된 이미지 수={}",
                reviewId, response.getDeletedImageCount());

        return ApiResponse.success(response);
    }

    // ============= 사용자 리뷰 조회/통계 API - 개선된 버전 =============

    /**
     * 내가 삭제한 리뷰 목록 조회 - @AuthenticationPrincipal 사용으로 개선
     */
    @GetMapping("/my/deleted")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "삭제된 리뷰 목록을 조회했습니다.")
    @Operation(summary = "내가 삭제한 리뷰 목록 조회", description = "현재 로그인한 사용자가 삭제한 리뷰들을 조회합니다.")
    public ApiResponse<List<ReviewResponse>> getMyDeletedReviews(
            @AuthenticationPrincipal User user) {

        log.info("내가 삭제한 리뷰 목록 조회 API 호출: userId={}", user.getId());

        // 데이터베이스 조회 없이 바로 사용자 ID 사용
        List<ReviewResponse> deletedReviews = reviewService.getUserDeletedReviews(user.getId());

        log.info("내가 삭제한 리뷰 목록 조회 완료: userId={}, 개수={}", user.getId(), deletedReviews.size());

        return ApiResponse.success(deletedReviews);
    }

    /**
     * 내가 삭제한 리뷰 개수 조회 - @AuthenticationPrincipal 사용으로 개선
     */
    @GetMapping("/my/deleted/count")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "삭제된 리뷰 개수를 조회했습니다.")
    @Operation(summary = "내가 삭제한 리뷰 개수 조회", description = "현재 로그인한 사용자가 삭제한 리뷰 개수를 조회합니다.")
    public ApiResponse<UserDeletedReviewCountResponse> getMyDeletedReviewCount(
            @AuthenticationPrincipal User user) {

        log.info("내가 삭제한 리뷰 개수 조회 API 호출: userId={}", user.getId());

        // 데이터베이스 조회 없이 바로 사용자 ID 사용
        Long deletedCount = reviewService.getUserDeletedReviewCount(user.getId());

        UserDeletedReviewCountResponse response = new UserDeletedReviewCountResponse(user.getId(), deletedCount);

        log.info("내가 삭제한 리뷰 개수 조회 완료: userId={}, 개수={}", user.getId(), deletedCount);

        return ApiResponse.success(response);
    }

    // ============= 헬퍼 메서드 (레거시 코드) =============

    /**
     * 현재 사용자 ID 가져오기 (레거시 메서드)
     * 주의: 이 메서드는 데이터베이스 조회를 수반하므로 성능상 불리함
     * 새로운 API에서는 @AuthenticationPrincipal User user 파라미터 사용 권장
     *
     * @deprecated 성능상의 이유로 @AuthenticationPrincipal 사용 권장
     */
    @Deprecated
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        String userEmail = authentication.getName();

        // UserRepository를 통해 사용자 조회 (성능상 비효율적)
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."))
                .getId();
    }

    // ============= 요청 DTO =============

    @Getter
    @NoArgsConstructor
    public static class DeleteImagesRequest {

        @NotNull(message = "삭제할 이미지 ID 목록은 필수입니다.")
        private List<@NotNull Long> imageIds;

        public DeleteImagesRequest(List<Long> imageIds) {
            this.imageIds = imageIds;
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

    // ============= 응답 DTO =============

    @Getter
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UserDeletedReviewCountResponse {
        private Long userId;
        private Long deletedReviewCount;
    }

    /*
    // 향후 추가될 수 있는 리뷰 추천 기능
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