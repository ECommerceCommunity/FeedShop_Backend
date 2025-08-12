package com.cMall.feedShop.product.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductSearchRequest;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.application.service.ProductReadService;
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

    /**
     * 상품 목록 조회 API
     * @param q
     * @param categoryId
     * @param minPrice
     * @param maxPrice
     * @param storeId
     * @param page
     * @param size
     * @param sort
     * @return
     */
    @GetMapping
    @ApiResponseFormat(message = "상품 목록을 성공적으로 조회했습니다.")
    public ApiResponse<ProductPageResponse> getProductList(
            @RequestParam(value = "q", required = false) String q,
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

        // 2. 요청 객체를 생성한다.
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(q)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .storeId(storeId)
                .build();

        // 3. 정렬 타입 변환
        ProductSortType productSortType = ProductSortType.fromCode(sort);

        // 4. 통합 서비스 호출
        ProductPageResponse data = productReadService.getProductList(request, page, size, productSortType);

        return ApiResponse.success(data);
    }
}
