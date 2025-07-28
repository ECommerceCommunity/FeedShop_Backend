package com.cMall.feedShop.product.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductListResponse {
    private Long productId;
    private String name;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Long storeId;
    private String storeName;
    private Integer wishNumber;
    private String mainImageUrl;

    public static ProductListResponse of(
            Long productId,
            String name,
            BigDecimal price,
            BigDecimal discountPrice,
            Long storeId,
            String storeName,
            Integer wishNumber,
            String mainImageUrl) {
        return ProductListResponse.builder()
                .productId(productId)
                .name(name)
                .price(price)
                .discountPrice(discountPrice)
                .storeId(storeId)
                .storeName(storeName)
                .wishNumber(wishNumber != null ? wishNumber : 0)
                .mainImageUrl(mainImageUrl)
                .build();
    }
}
