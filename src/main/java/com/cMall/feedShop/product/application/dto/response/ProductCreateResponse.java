package com.cMall.feedShop.product.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductCreateResponse {
    private Long productId;

    public static ProductCreateResponse of(Long productId) {
        return ProductCreateResponse.builder()
                .productId(productId)
                .build();
    }
}
