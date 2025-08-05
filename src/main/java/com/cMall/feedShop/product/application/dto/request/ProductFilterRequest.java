package com.cMall.feedShop.product.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterRequest {

    // 카테고리 ID (선택)
    private Long categoryId;

    // 최소 가격 (선택)
    private BigDecimal minPrice;

    // 최대 가격 (선택)
    private BigDecimal maxPrice;

    // 스토어 ID (선택)
    private Long storeId;

}
