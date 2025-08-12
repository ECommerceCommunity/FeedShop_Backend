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
public class ProductSearchRequest {
    private String keyword; // 검색 키워드
    private Long categoryId; // 카테고리 ID
    private BigDecimal minPrice; // 최소 가격
    private BigDecimal maxPrice; // 최대 가격
    private Long storeId; // 매장 ID
}
