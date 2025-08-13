package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewResponse;
import com.cMall.feedShop.review.application.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 리뷰 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "관리자 리뷰 관리 API", description = "관리자용 리뷰 통계 및 관리 API (관리자 권한 필요)")
public class ReviewAdminController {

    private final ReviewService reviewService;

    // ============= 커스텀 보안 어노테이션 =============

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN')")
    public @interface AdminOnly {}

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public @interface AdminOrSeller {}

    // ============= 리뷰 통계 조회 API =============

    @GetMapping("/stats/product/{productId}")
    @AdminOrSeller
    @ApiResponseFormat(message = "상품 리뷰 통계를 조회했습니다.")
    @Operation(summary = "상품별 리뷰 통계 조회",
            description = "특정 상품의 활성/삭제/전체 리뷰 개수와 평균 평점, 삭제율을 조회합니다.")
    public ApiResponse<ReviewService.ReviewStatsResponse> getProductReviewStats(
            @PathVariable @Parameter(description = "상품 ID") Long productId) {

        log.info("상품 리뷰 통계 조회 API 호출: productId={}", productId);

        ReviewService.ReviewStatsResponse stats = reviewService.getProductReviewStats(productId);

        log.info("상품 리뷰 통계 조회 완료: productId={}, active={}, deleted={}, total={}",
                productId, stats.getActiveReviewCount(), stats.getDeletedReviewCount(), stats.getTotalReviewCount());

        return ApiResponse.success(stats);
    }

    @GetMapping("/stats/period")
    @AdminOnly
    @ApiResponseFormat(message = "기간별 삭제된 리뷰 통계를 조회했습니다.")
    @Operation(summary = "기간별 삭제된 리뷰 통계",
            description = "지정된 기간 내에 삭제된 리뷰들의 통계를 조회합니다.")
    public ApiResponse<ReviewService.PeriodReviewStatsResponse> getDeletedReviewStatsByPeriod(
            @RequestParam @Parameter(description = "시작 날짜 (yyyy-MM-dd'T'HH:mm:ss)")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 날짜 (yyyy-MM-dd'T'HH:mm:ss)")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("기간별 삭제된 리뷰 통계 조회 API 호출: {} ~ {}", startDate, endDate);

        // 날짜 유효성 검증 (GlobalExceptionHandler에서 IllegalArgumentException 처리)
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        ReviewService.PeriodReviewStatsResponse stats = reviewService.getDeletedReviewStatsBetween(startDate, endDate);

        log.info("기간별 삭제된 리뷰 통계 조회 완료: 기간={} ~ {}, 총 삭제={}, 사용자={}, 상품={}",
                startDate, endDate, stats.getTotalDeletedCount(), stats.getUniqueUserCount(), stats.getUniqueProductCount());

        return ApiResponse.success(stats);
    }

    @GetMapping("/stats/recent")
    @AdminOnly
    @ApiResponseFormat(message = "최근 30일간 삭제된 리뷰 통계를 조회했습니다.")
    @Operation(summary = "최근 30일간 삭제된 리뷰 통계",
            description = "최근 30일간 삭제된 리뷰들의 통계를 조회합니다.")
    public ApiResponse<ReviewService.PeriodReviewStatsResponse> getRecentDeletedReviewStats() {

        log.info("최근 30일간 삭제된 리뷰 통계 조회 API 호출");

        ReviewService.PeriodReviewStatsResponse stats = reviewService.getRecentDeletedReviewStats();

        log.info("최근 30일간 삭제된 리뷰 통계 조회 완료: 총 삭제={}, 사용자={}, 상품={}",
                stats.getTotalDeletedCount(), stats.getUniqueUserCount(), stats.getUniqueProductCount());

        return ApiResponse.success(stats);
    }

    // ============= 삭제된 리뷰 조회 API =============

    @GetMapping("/deleted/user/{userId}")
    @AdminOnly
    @ApiResponseFormat(message = "사용자의 삭제된 리뷰 목록을 조회했습니다.")
    @Operation(summary = "사용자별 삭제된 리뷰 목록 조회",
            description = "특정 사용자가 삭제한 리뷰들을 조회합니다.")
    public ApiResponse<List<ReviewResponse>> getUserDeletedReviews(
            @PathVariable @Parameter(description = "사용자 ID") Long userId) {

        log.info("사용자별 삭제된 리뷰 조회 API 호출: userId={}", userId);

        List<ReviewResponse> deletedReviews = reviewService.getUserDeletedReviews(userId);

        log.info("사용자별 삭제된 리뷰 조회 완료: userId={}, 개수={}", userId, deletedReviews.size());

        return ApiResponse.success(deletedReviews);
    }

    @GetMapping("/deleted/period")
    @AdminOnly
    @ApiResponseFormat(message = "기간별 삭제된 리뷰 목록을 조회했습니다.")
    @Operation(summary = "기간별 삭제된 리뷰 목록 조회",
            description = "지정된 기간 내에 삭제된 모든 리뷰들을 조회합니다.")
    public ApiResponse<List<ReviewResponse>> getDeletedReviewsByPeriod(
            @RequestParam @Parameter(description = "시작 날짜 (yyyy-MM-dd'T'HH:mm:ss)")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @Parameter(description = "종료 날짜 (yyyy-MM-dd'T'HH:mm:ss)")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("기간별 삭제된 리뷰 목록 조회 API 호출: {} ~ {}", startDate, endDate);

        // 날짜 유효성 검증 (GlobalExceptionHandler에서 IllegalArgumentException 처리)
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        List<ReviewResponse> deletedReviews = reviewService.getDeletedReviewsBetween(startDate, endDate);

        log.info("기간별 삭제된 리뷰 목록 조회 완료: 기간={} ~ {}, 개수={}",
                startDate, endDate, deletedReviews.size());

        return ApiResponse.success(deletedReviews);
    }

    @GetMapping("/deleted/count/user/{userId}")
    @AdminOnly
    @ApiResponseFormat(message = "사용자의 삭제된 리뷰 개수를 조회했습니다.")
    @Operation(summary = "사용자별 삭제된 리뷰 개수 조회",
            description = "특정 사용자가 삭제한 리뷰 개수를 조회합니다.")
    public ApiResponse<UserDeletedReviewCountResponse> getUserDeletedReviewCount(
            @PathVariable @Parameter(description = "사용자 ID") Long userId) {

        log.info("사용자별 삭제된 리뷰 개수 조회 API 호출: userId={}", userId);

        Long deletedCount = reviewService.getUserDeletedReviewCount(userId);

        UserDeletedReviewCountResponse response = new UserDeletedReviewCountResponse(userId, deletedCount);

        log.info("사용자별 삭제된 리뷰 개수 조회 완료: userId={}, 개수={}", userId, deletedCount);

        return ApiResponse.success(response);
    }

    // ============= 응답 DTO (Record 사용) =============

    /**
     * 사용자별 삭제된 리뷰 개수 응답 DTO
     */
    public record UserDeletedReviewCountResponse(
            Long userId,
            Long deletedReviewCount
    ) {}
}