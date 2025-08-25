package com.cMall.feedShop.ai.application.dto.response;

import com.cMall.feedShop.common.ai.BaseAIResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor
public class ProductRecommendationAIResponse extends BaseAIResponse<List<Long>> {

    @JsonProperty("productIds")
    private List<Long> productIds;

    // 상품 ID 목록을 안전하게 반환
    public List<Long> getProductIds() {
        // productIds 필드가 있으면 우선 사용, 없으면 data 필드 사용
        if (productIds != null) {
            return productIds;
        }
        return getData() != null ? getData() : Collections.emptyList();
    }

}
