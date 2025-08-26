package com.cMall.feedShop.product.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.product.application.dto.response.CategoryResponse;
import com.cMall.feedShop.product.application.dto.response.ProductDetailResponse;
import com.cMall.feedShop.product.application.service.CategoryService;
import com.cMall.feedShop.product.application.service.ProductReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "상품 (공개)", description = "공개 상품 조회 관련 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductReadService productReadService;
    private final CategoryService categoryService;

    @Operation(
            summary = "상품 상세 조회",
            description = "특정 상품의 상세 정보를 조회합니다. 상품 기본 정보, 옵션, 이미지, 할인 정보 등을 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품을 찾을 수 없음"
            )
    })
    @GetMapping("/{productId}")
    @ApiResponseFormat(message = "상품 상세 정보를 성공적으로 조회했습니다.")
    public ApiResponse<ProductDetailResponse> getProductDetail(
            @Parameter(description = "상품 ID", required = true, example = "1")
            @PathVariable Long productId) {
        ProductDetailResponse data = productReadService.getProductDetail(productId);
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "카테고리 목록 조회",
            description = "상품 분류에 사용되는 모든 카테고리 목록을 조회합니다. 카테고리별 상품 필터링 시 사용합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "카테고리 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))
            )
    })
    @GetMapping("/categories")
    @ApiResponseFormat(message = "카테고리 목록을 성공적으로 조회했습니다.")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> data = categoryService.getAllCategories();
        return ApiResponse.success(data);
    }
}
