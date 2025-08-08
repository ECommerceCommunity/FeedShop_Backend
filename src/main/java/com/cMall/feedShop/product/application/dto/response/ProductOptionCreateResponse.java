package com.cMall.feedShop.product.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductOptionCreateResponse {

    // 새로 만들어진 옵션 ID
    private Long optionId;

    public static ProductOptionCreateResponse of(Long optionId) {
        return ProductOptionCreateResponse.builder()
                .optionId(optionId)
                .build();
    }
}
