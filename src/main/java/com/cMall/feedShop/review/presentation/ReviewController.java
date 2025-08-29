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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰 조회 API", description = "리뷰 목록 및 상세 조회 API (로그인 불필요)")
public class ReviewController {

    private final ReviewService reviewService;
    private final Review3ElementStatisticsService statisticsService;
  
    @GetMapping("/products/{productId}")
    @ApiResponseFormat(message = "상품 리뷰 목록을 성공적으로 조회했습니다.")
    @Operation(
            summary = "상품별 리뷰 목록 조회", 
            description = "특정 상품의 리뷰 목록을 페이지네이션으로 조회합니다. 로그인이 필요하지 않습니다. 최신순/인기순 정렬을 지원합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "상품 리뷰 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReviewListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "잘못된 요청 파라미터 (페이지 번호, 크기, 정렬 방식 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "해당 상품을 찾을 수 없음"
            )
    })
    public com.cMall.feedShop.common.dto.ApiResponse<ReviewListResponse> getProductReviews(
            @Parameter(description = "상품 ID", required = true, example = "1") @PathVariable Long productId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (1-100)", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 방식 (latest: 최신순, points: 인기순)", example = "latest") @RequestParam(defaultValue = "latest") String sort) {
        ReviewListResponse response = reviewService.getProductReviews(productId, page, size, sort);
        return com.cMall.feedShop.common.dto.ApiResponse.success(response);
    }

    @GetMapping("/products/{productId}/filter")
    @ApiResponseFormat(message = "필터링된 상품 리뷰 목록을 성공적으로 조회했습니다.")
    @Operation(
            summary = "상품별 리뷰 목록 필터링 조회", 
            description = "특정 상품의 리뷰를 평점, 착용감, 쿠션감, 안정성으로 필터링하여 조회합니다. 모든 필터는 선택사항이며 여러 조건을 동시에 적용할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "필터링된 리뷰 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReviewListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "잘못된 요청 파라미터 (필터 값, 페이지 설정 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "해당 상품을 찾을 수 없음"
            )
    })
    public com.cMall.feedShop.common.dto.ApiResponse<ReviewListResponse> getProductReviewsWithFilters(
            @Parameter(description = "상품 ID", required = true, example = "1") @PathVariable Long productId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (1-100)", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 방식 (latest: 최신순, points: 인기순)", example = "latest") @RequestParam(defaultValue = "latest") String sort,
            @Parameter(description = "평점 필터 (1-5점)", example = "5") @RequestParam(required = false) Integer rating,
            @Parameter(description = "착용감 필터 (1: 작음, 2: 적당, 3: 큼)", example = "2") @RequestParam(required = false) String sizeFit,
            @Parameter(description = "쿠션감 필터 (1: 딱딱, 2: 보통, 3: 부드러움)", example = "2") @RequestParam(required = false) String cushion,
            @Parameter(description = "안정성 필터 (1: 불안정, 2: 보통, 3: 안정적)", example = "3") @RequestParam(required = false) String stability) {
        ReviewListResponse response = reviewService.getProductReviewsWithFilters(productId, page, size, sort, rating, sizeFit, cushion, stability);
        return com.cMall.feedShop.common.dto.ApiResponse.success(response);
    }

    @GetMapping("/{reviewId}")
    @ApiResponseFormat(message = "리뷰 상세 정보를 성공적으로 조회했습니다.")
    @Operation(
            summary = "리뷰 상세 조회", 
            description = "특정 리뷰의 상세 정보(내용, 평점, 이미지, 3요소 평가 등)를 조회합니다. 로그인이 필요하지 않습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "리뷰 상세 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", 
                    description = "삭제되었거나 비공개 처리된 리뷰"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "해당 리뷰를 찾을 수 없음"
            )
    })
    public com.cMall.feedShop.common.dto.ApiResponse<ReviewResponse> getReview(
            @Parameter(description = "리뷰 ID", required = true, example = "123") @PathVariable Long reviewId) {
        ReviewResponse response = reviewService.getReview(reviewId);
        return com.cMall.feedShop.common.dto.ApiResponse.success(response);

    }

    @GetMapping("/products/{productId}/statistics")
    @ApiResponseFormat(message = "상품 3요소 평가 통계를 성공적으로 조회했습니다.")
    @Operation(
            summary = "상품별 3요소 평가 통계 조회",
            description = "특정 상품에 대한 모든 리뷰의 쿠션감(Cushion), 착용감(SizeFit), 안정성(Stability) 평가 통계를 조회합니다. 각 요소별 평균값과 분포를 제공합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "3요소 평가 통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = Review3ElementStatisticsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "잘못된 상품 ID (음수 값 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "해당 상품을 찾을 수 없음"
            )
    })
    public com.cMall.feedShop.common.dto.ApiResponse<Review3ElementStatisticsResponse> getProductStatistics(
            @Parameter(description = "상품 ID", required = true, example = "1") @PathVariable Long productId) {
        Review3ElementStatisticsResponse response = statisticsService.getProductStatistics(productId);
        return com.cMall.feedShop.common.dto.ApiResponse.success(response);
    }

    @GetMapping("/images/{year}/{month}/{day}/{filename:.+}")
    @Operation(
            summary = "리뷰 이미지 서빙", 
            description = "업로드된 리뷰 이미지 파일을 서빙합니다. 이미지는 연/월/일 구조로 저장되며, CORS 헤더가 설정되어 다른 도메인에서도 접근 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "이미지 서빙 성공",
                    content = @Content(schema = @Schema(type = "string", format = "binary"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "잘못된 URL 형식"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "이미지 파일을 찾을 수 없음"
            )
    })
    public ResponseEntity<Resource> serveReviewImage(
            @Parameter(description = "연도 (4자리)", required = true, example = "2025") @PathVariable String year,
            @Parameter(description = "월 (2자리)", required = true, example = "08") @PathVariable String month, 
            @Parameter(description = "일 (2자리)", required = true, example = "14") @PathVariable String day,
            @Parameter(description = "이미지 파일명 (확장자 포함)", required = true, example = "abc123.jpg") @PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads/reviews")
                    .resolve(year)
                    .resolve(month)
                    .resolve(day)
                    .resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                String contentType = getContentType(filename);
                return ResponseEntity.ok()
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }

}