package com.cMall.feedShop.cart.application.dto.response;

import com.cMall.feedShop.cart.application.dto.response.info.CartItemInfo;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CartItemListResponse {

    private List<CartItemInfo> items; // 장바구니 아이템 목록
    private BigDecimal totalOriginalPrice; // 총 원가
    private BigDecimal totalDiscountPrice; // 총 할인 가격 (실제 결제 금액)
    private BigDecimal totalSavings; // 총 절약 금액
    private Integer totalItemCount; // 총 아이템 개수

    public static CartItemListResponse of(List<CartItemInfo> items,
                                          BigDecimal totalOriginalPrice,
                                          BigDecimal totalDiscountPrice,
                                          BigDecimal totalSavings) {
        return CartItemListResponse.builder()
                .items(items)
                .totalOriginalPrice(totalOriginalPrice)
                .totalDiscountPrice(totalDiscountPrice)
                .totalSavings(totalSavings)
                .totalItemCount(items.size())
                .build();
    }

    public static CartItemListResponse empty() {
        return CartItemListResponse.builder()
                .items(List.of())
                .totalOriginalPrice(BigDecimal.ZERO)
                .totalDiscountPrice(BigDecimal.ZERO)
                .totalSavings(BigDecimal.ZERO)
                .totalItemCount(0)
                .build();
    }
}
