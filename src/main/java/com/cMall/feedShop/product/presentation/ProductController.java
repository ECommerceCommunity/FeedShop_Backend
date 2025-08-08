package com.cMall.feedShop.product.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductFilterRequest;
import com.cMall.feedShop.product.application.dto.response.CategoryResponse;
import com.cMall.feedShop.product.application.dto.response.ProductDetailResponse;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.application.service.CategoryService;
import com.cMall.feedShop.product.application.service.ProductFilterService;
import com.cMall.feedShop.product.application.service.ProductReadService;
import com.cMall.feedShop.product.application.validator.PriceValidator;
import com.cMall.feedShop.product.domain.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
//@Tag(name = "상품 조회", description = "상품 목록과 상세 정보를 조회하는 API 입니다.")
public class ProductController {
    private final ProductReadService productReadService;
    private final CategoryService categoryService;
    private final ProductFilterService productFilterService;

    /**
     * 상품 목록 조회 API
     * /api/products?page=0&size=20
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 상품 목록 응답
     */
    @GetMapping
    @ApiResponseFormat(message = "상품 목록을 성공적으로 조회했습니다.")
    public ApiResponse<ProductPageResponse> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ProductPageResponse data = productReadService.getProductList(page, size);
        return ApiResponse.success(data);
    }

    /**
     * 상품 목록 필터링 조회 API
     * /api/products/filter?categoryId=1&minPrice=1000&maxPrice=5000&storeId=2&page=0&size=20
     * @param categoryId 카테고리 아이디 (선택적)
     * @param minPrice 최소 가격 (선택적)
     * @param maxPrice 최대 가격 (선택적)
     * @param storeId 매장 아이디 (선택적)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 필터링된 상품 목록 응답
     */
    @GetMapping("/filter")
    @ApiResponseFormat(message = "상품 목록을 필터링하여 성공적으로 조회했습니다.")
    public ApiResponse<ProductPageResponse> filterProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // 1. 가격 범위 유효성을 검증한다.
        if (!PriceValidator.isValidPriceRange(minPrice, maxPrice)) {
            throw new ProductException(ErrorCode.INVALID_PRODUCT_FILTER_PRICE_RANGE);
        }

        // 2. 필터링 요청 객체를 생성한다.
        ProductFilterRequest request = ProductFilterRequest.builder()
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .storeId(storeId)
                .build();

        // 3. 필터링된 상품 목록을 조회한다.
        ProductPageResponse data = productFilterService.filterProductList(request, page, size);
        return ApiResponse.success(data);
    }

    /**
     * 상품 상세 조회 API
     * /api/products/{productId}
     * @param productId 상품 아이디
     * @return 상품 상세 조회 응답
     */
    @GetMapping("/{productId}")
    @ApiResponseFormat(message = "상품 상세 정보를 성공적으로 조회했습니다.")
    public ApiResponse<ProductDetailResponse> getProductDetail(@PathVariable Long productId) {
        ProductDetailResponse data = productReadService.getProductDetail(productId);
        return ApiResponse.success(data);
    }

    /**
     * 카테고리 목록 조회 API
     * /api/products/categories
     * @return 카테고리 목록 응답
     */
    @GetMapping("/categories")
    @ApiResponseFormat(message = "카테고리 목록을 성공적으로 조회했습니다.")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> data = categoryService.getAllCategories();
        return ApiResponse.success(data);
    }
}
