package com.cMall.feedShop.product.application.dto.request;

import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
    private List<Color> colors; // 색상 필터
    private List<Size> sizes; // 사이즈 필터
    private List<Gender> genders; // 성별 필터
    private Boolean inStockOnly; // 재고 여부 필터
    private Boolean discountedOnly; // 할인 여부 필터
}
