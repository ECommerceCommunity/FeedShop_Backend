package com.cMall.feedShop.product.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductFilterRequest;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.application.service.ProductFilterService;
import com.cMall.feedShop.product.application.service.ProductReadService;
import com.cMall.feedShop.product.application.service.ProductSearchService;
import com.cMall.feedShop.product.application.validator.PriceValidator;
import com.cMall.feedShop.product.domain.enums.ProductSortType;
import com.cMall.feedShop.product.domain.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 상품 목록 조회 컨트롤러
 * - 상품 목록을 조회하는 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductListController {

    private final ProductReadService productReadService;
    private final ProductFilterService productFilterService;
    private final ProductSearchService productSearchService;

    /**
     * 상품 목록 조회 API
     * /api/products?page=0&size=20
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @param sort 정렬 기준 (기본값: latest)
     * @return 상품 목록 응답
     */
    @GetMapping
    @ApiResponseFormat(message = "상품 목록을 성공적으로 조회했습니다.")
    public ApiResponse<ProductPageResponse> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        ProductSortType productSortType = ProductSortType.fromCode(sort);
        ProductPageResponse data = productReadService.getProductList(page, size, productSortType);
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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
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

        ProductSortType productSortType = ProductSortType.fromCode(sort);

        // 3. 필터링된 상품 목록을 조회한다.
        ProductPageResponse data = productFilterService.filterProductList(request, page, size, productSortType);
        return ApiResponse.success(data);
    }

    /**
     * 상품 검색 API
     * /api/products/search?q=keyword&page=0&size=20
     * @param keyword 검색어 (선택적)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @return 검색된 상품 목록 응답
     */
    @GetMapping("/search")
    @ApiResponseFormat(message = "상품 검색 결과를 성공적으로 조회했습니다.")
    public ApiResponse<ProductPageResponse> searchProducts(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ProductPageResponse data = productSearchService.searchProductList(keyword, page, size);
        return ApiResponse.success(data);
    }

}
