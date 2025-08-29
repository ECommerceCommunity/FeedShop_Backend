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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final com.cMall.feedShop.user.application.service.PointService pointService;

    // ============= 리뷰 작성 API =============

    /**
     * 리뷰 작성 - JSON 요청 (이미지 없음)
     */
    @PostMapping(consumes = "application/json")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰가 성공적으로 작성되었습니다.")
    @Operation(
            summary = "리뷰 작성 (JSON)", 
            description = "이미지 없이 JSON 형식으로 리뷰를 작성합니다. 작성 완료 시 포인트가 지급됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "리뷰 작성 성공",
                    content = @Content(schema = @Schema(implementation = ReviewCreateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 필드 누락, 유효성 검증 실패)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 해당 상품에 리뷰를 작성한 경우"
            )
    })
    public ApiResponse<ReviewCreateResponse> createReviewJson(
            @Parameter(description = "리뷰 작성 요청 정보", required = true) @Valid @RequestBody ReviewCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        log.error("=== 리뷰 작성 디버깅 시작 ===");
        log.error("User: {}", user != null ? user.getEmail() : "NULL");
        log.error("ProductId: {}", request.getProductId());
        log.error("Rating: {}", request.getRating());
        log.error("Content: {}", request.getContent());
        
        try {
            log.info("JSON 리뷰 작성 API 호출: productId={}, rating={}", request.getProductId(), request.getRating());
            
            // 1. 리뷰 작성 (트랜잭션 포함)
            log.error("reviewService.createReview 호출 전");
            ReviewCreateResponse baseResponse = reviewService.createReview(request, null);
            log.error("reviewService.createReview 호출 후 성공");
            
            // 2. 트랜잭션 완료 후 별도로 포인트 조회
            log.error("포인트 조회 시작");
            Integer currentPoints = getCurrentPointsSafely(user);
            log.error("포인트 조회 완료: {}", currentPoints);
            
            // 3. 최종 응답 생성
            ReviewCreateResponse finalResponse = baseResponse.withCurrentPoints(currentPoints);
            
            log.info("JSON 리뷰 작성 API 완료: reviewId={}, pointsEarned={}, currentPoints={}", 
                    finalResponse.getReviewId(), finalResponse.getPointsEarned(), finalResponse.getCurrentPoints());
            
            return ApiResponse.success(finalResponse);
        } catch (Exception e) {
            log.error("리뷰 작성 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 리뷰 작성 - FormData 개별 필드 방식 (프론트엔드 호환)
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰가 성공적으로 작성되었습니다.")
    @Operation(
            summary = "리뷰 작성 (FormData)", 
            description = "FormData 개별 필드와 이미지 파일로 리뷰를 작성합니다. 프론트엔드 호환성을 위한 API입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "리뷰 작성 성공",
                    content = @Content(schema = @Schema(implementation = ReviewCreateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 필드 누락, 이미지 파일 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 해당 상품에 리뷰를 작성한 경우"
            )
    })
    public ApiResponse<ReviewCreateResponse> createReviewFormData(
            @Parameter(description = "상품 ID", required = true) @Valid @NotNull(message = "상품 ID는 필수입니다.") @RequestParam Long productId,
            @Parameter(description = "리뷰 제목", required = true) @Valid @NotBlank(message = "리뷰 제목은 필수입니다.") @RequestParam String title,
            @Parameter(description = "평점 (1-5점)", required = true) @Valid @NotNull(message = "평점은 필수입니다.") @Min(1) @Max(5) @RequestParam Integer rating,
            @Parameter(description = "리뷰 내용", required = true) @Valid @NotBlank(message = "리뷰 내용은 필수입니다.") @RequestParam String content,
            @Parameter(description = "사이즈 착용감 (SMALL: 작음, NORMAL: 적당, LARGE: 큼)", required = true) @Valid @NotBlank(message = "사이즈 착용감은 필수입니다.") @RequestParam String sizeFit,
            @Parameter(description = "쿠션감 (HARD: 딱딱, MODERATE: 보통, SOFT: 부드러움, VERY_SOFT: 매우 부드러움)", required = true) @Valid @NotBlank(message = "쿠션감은 필수입니다.") @RequestParam String cushion,
            @Parameter(description = "안정성 (UNSTABLE: 불안정, MODERATE: 보통, STABLE: 안정적)", required = true) @Valid @NotBlank(message = "안정성은 필수입니다.") @RequestParam String stability,
            @Parameter(description = "리뷰 이미지 파일들 (선택사항, 최대 5개)") @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        log.info("FormData 리뷰 작성 API 호출: productId={}, rating={}", productId, rating);
        
        // 개별 파라미터를 ReviewCreateRequest로 변환
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .productId(productId)
                .title(title)
                .rating(rating)
                .content(content)
                .sizeFit(com.cMall.feedShop.review.domain.enums.SizeFit.valueOf(sizeFit))
                .cushion(com.cMall.feedShop.review.domain.enums.Cushion.valueOf(cushion))
                .stability(com.cMall.feedShop.review.domain.enums.Stability.valueOf(stability))
                .build();
        
        // 1. 리뷰 작성 (트랜잭션 포함)
        ReviewCreateResponse baseResponse = reviewService.createReview(request, images);
        
        // 2. 트랜잭션 완료 후 별도로 포인트 조회
        Integer currentPoints = getCurrentPointsSafely(user);
        
        // 3. 최종 응답 생성
        ReviewCreateResponse finalResponse = baseResponse.withCurrentPoints(currentPoints);
        
        log.info("FormData 리뷰 작성 API 완료: reviewId={}, pointsEarned={}, currentPoints={}", 
                finalResponse.getReviewId(), finalResponse.getPointsEarned(), finalResponse.getCurrentPoints());
        
        return ApiResponse.success(finalResponse);
    }

    /**
     * 리뷰 작성 - JSON 부분과 이미지 부분으로 나뉜 멀티파트 요청
     */
    @PostMapping(value = "/with-images", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰가 성공적으로 작성되었습니다.")
    @Operation(
            summary = "리뷰 작성 (JSON + 이미지)", 
            description = "JSON 형식의 리뷰 데이터와 이미지 파일을 함께 전송하여 리뷰를 작성합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "리뷰 작성 성공",
                    content = @Content(schema = @Schema(implementation = ReviewCreateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (JSON 파싱 오류, 이미지 파일 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    public ApiResponse<ReviewCreateResponse> createReviewWithImages(
            @Parameter(description = "JSON 형식의 리뷰 정보", required = true) @Valid @RequestPart(value = "review") ReviewCreateRequest request,
            @Parameter(description = "리뷰 이미지 파일들 (선택사항)") @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        log.info("JSON+이미지 리뷰 작성 API 호출: productId={}, imageCount={}", 
                request.getProductId(), images != null ? images.size() : 0);
        
        ReviewCreateResponse response = reviewService.createReview(request, images);
        
        log.info("JSON+이미지 리뷰 작성 API 완료: reviewId={}, pointsEarned={}", 
                response.getReviewId(), response.getPointsEarned());
        
        return ApiResponse.success(response);
    }

    // ============= 리뷰 수정 API =============

    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰가 성공적으로 수정되었습니다.")
    @Operation(
            summary = "리뷰 수정", 
            description = "기존 리뷰의 내용, 평점, 이미지를 수정합니다. 리뷰 작성자만 수정 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "리뷰 수정 성공",
                    content = @Content(schema = @Schema(implementation = ReviewUpdateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효성 검증 실패, 이미지 파일 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "리뷰 수정 권한 없음 (다른 사용자의 리뷰)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "리뷰를 찾을 수 없음"
            )
    })
    public ApiResponse<ReviewUpdateResponse> updateReview(
            @Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId,
            @Parameter(description = "리뷰 수정 정보 (JSON)", required = true) @Valid @RequestPart("review") ReviewUpdateRequest request,
            @Parameter(description = "새로 추가할 이미지 파일들 (선택사항)") @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) {

        log.info("리뷰 수정 API 호출: reviewId={}", reviewId);

        // ReviewService의 updateReview 메서드는 3개의 파라미터를 받음
        ReviewUpdateResponse response = reviewService.updateReview(reviewId, request, newImages);

        log.info("리뷰 수정 API 완료: reviewId={}", reviewId);

        return ApiResponse.success(response);
    }

    @PatchMapping("/{reviewId}/rating")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰 평점이 성공적으로 수정되었습니다.")
    @Operation(
            summary = "리뷰 평점만 수정", 
            description = "리뷰의 평점(1-5점)만 빠르게 수정합니다. 리뷰 작성자만 수정 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "평점 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 평점 값 (1-5점 범위 외)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "리뷰 수정 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "리뷰를 찾을 수 없음"
            )
    })
    public ApiResponse<Void> updateReviewRating(
            @Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId,
            @Parameter(description = "수정할 평점 정보", required = true) @RequestBody @Valid UpdateRatingRequest request) {

        log.info("리뷰 평점 수정 API 호출: reviewId={}, 새로운 평점={}", reviewId, request.getRating());

        // ReviewService의 updateReviewRating 메서드는 void를 반환
        reviewService.updateReviewRating(reviewId, request.getRating());

        log.info("리뷰 평점 수정 API 완료: reviewId={}", reviewId);

        return ApiResponse.success(null);
    }

    @PatchMapping("/{reviewId}/content")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "리뷰 내용이 성공적으로 수정되었습니다.")
    @Operation(
            summary = "리뷰 내용만 수정", 
            description = "리뷰의 내용만 빠르게 수정합니다. 리뷰 작성자만 수정 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내용 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 내용 (길이 제한 초과 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "리뷰 수정 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "리뷰를 찾을 수 없음"
            )
    })
    public ApiResponse<Void> updateReviewContent(
            @Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId,
            @Parameter(description = "수정할 내용 정보", required = true) @RequestBody @Valid UpdateContentRequest request) {

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
    @Operation(
            summary = "리뷰 삭제", 
            description = "리뷰와 연관된 모든 이미지를 함께 삭제합니다. 리뷰 작성자만 삭제 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "리뷰 삭제 성공",
                    content = @Content(schema = @Schema(implementation = ReviewDeleteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "리뷰 삭제 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "리뷰를 찾을 수 없음"
            )
    })
    public ApiResponse<ReviewDeleteResponse> deleteReview(
            @Parameter(description = "삭제할 리뷰 ID", required = true) @PathVariable Long reviewId) {

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
    @Operation(
            summary = "리뷰 이미지 일괄 삭제", 
            description = "리뷰에서 선택한 특정 이미지들만 삭제합니다. 리뷰 내용은 유지됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이미지 삭제 성공",
                    content = @Content(schema = @Schema(implementation = ReviewImageDeleteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미지 ID 목록 없음)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "리뷰 수정 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "리뷰 또는 이미지를 찾을 수 없음"
            )
    })
    public ApiResponse<ReviewImageDeleteResponse> deleteReviewImages(
            @Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId,
            @Parameter(description = "삭제할 이미지 ID 목록", required = true) @RequestBody @Valid DeleteImagesRequest request) {

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
    @Operation(
            summary = "리뷰 이미지 전체 삭제", 
            description = "리뷰에 첫부된 모든 이미지를 삭제합니다. 리뷰 텍스트와 평점은 유지됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이미지 전체 삭제 성공",
                    content = @Content(schema = @Schema(implementation = ReviewImageDeleteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "리뷰 수정 권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "리뷰를 찾을 수 없음"
            )
    })
    public ApiResponse<ReviewImageDeleteResponse> deleteAllReviewImages(
            @Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId) {

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
    @Operation(
            summary = "내가 삭제한 리뷰 목록 조회", 
            description = "현재 로그인한 사용자가 삭제한 리뷰들을 조회합니다. 삭제된 리뷰는 다른 사용자에게 보이지 않습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제된 리뷰 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    public ApiResponse<List<ReviewResponse>> getMyDeletedReviews(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

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
    @Operation(
            summary = "내가 삭제한 리뷰 개수 조회", 
            description = "현재 로그인한 사용자가 삭제한 리뷰의 총 개수를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제된 리뷰 개수 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserDeletedReviewCountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    public ApiResponse<UserDeletedReviewCountResponse> getMyDeletedReviewCount(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

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

    // ============= 헬퍼 메서드 =============

    /**
     * 안전하게 현재 포인트 조회 (트랜잭션과 분리)
     */
    private Integer getCurrentPointsSafely(User user) {
        try {
            if (user == null) {
                log.warn("User가 null입니다");
                return null;
            }
            
            // User 엔티티를 직접 전달하는 방식으로 변경
            return pointService.getPointBalance(user).getCurrentPoints();
        } catch (Exception e) {
            log.error("포인트 조회 실패: userId={}, error={}", 
                    user != null ? user.getId() : "null", e.getMessage(), e);
            return null; // 실패시 null 반환
        }
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