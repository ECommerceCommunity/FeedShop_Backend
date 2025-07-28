package com.cMall.feedShop.product.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.product.application.dto.response.CategoryResponse;
import com.cMall.feedShop.product.application.dto.response.ProductDetailResponse;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.application.service.CategoryService;
import com.cMall.feedShop.product.application.service.ProductReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
//@Tag(name = "상품 조회", description = "상품 목록과 상세 정보를 조회하는 API 입니다.")
public class ProductController {
    private final ProductReadService productReadService;
    private final CategoryService categoryService;

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
