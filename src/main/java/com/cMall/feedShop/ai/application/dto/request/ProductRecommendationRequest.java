package com.cMall.feedShop.ai.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductRecommendationRequest {

    @NotBlank(message = "요청 내용을 입력해주세요.")
    @Size(max = 500, message = "추천 요청은 500자 이내로 입력해주세요.")
    private String prompt;

    @Min(value = 1, message = "최소 1개 이상")
    @Max(value = 20, message = "최대 20개 이하")
    private int limit = 5;

    public ProductRecommendationRequest(String prompt, int limit) {
        this.prompt = prompt;
        this.limit = limit;
    }
}
