package com.cMall.feedShop.ai.application.dto.response;

import com.cMall.feedShop.product.application.dto.response.ProductListResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductRecommendationResponse {
    private List<ProductListResponse> products;
    private String prompt;
    private int count;
}
