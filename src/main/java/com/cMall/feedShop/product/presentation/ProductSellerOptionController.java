package com.cMall.feedShop.product.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.product.application.dto.response.info.ProductOptionInfo;
import com.cMall.feedShop.product.application.service.ProductOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class ProductSellerOptionController {
    private final ProductOptionService productOptionService;

    /**
     * 상품 옵션 정보를 조회하는 API
     * GET /api/seller/products/{productId}/options
     *
     * @param productId 조회할 상품의 ID
     * @return 상품 옵션 정보 리스트
     */
    @GetMapping("/products/{productId}/options")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "상품 옵션 정보를 조회했습니다.")
    public ApiResponse<List<ProductOptionInfo>> getProductOptions(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 상품 ID로 상품 옵션 정보를 조회
        List<ProductOptionInfo> options = productOptionService.getProductOptions(productId, userDetails);

        // 조회된 옵션 정보를 ApiResponse로 감싸서 반환
        return ApiResponse.success(options);
    }
}
