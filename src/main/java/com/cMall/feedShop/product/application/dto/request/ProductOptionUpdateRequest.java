package com.cMall.feedShop.product.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductOptionUpdateRequest {

    // (선택)
    private String gender;

    // (선택)
    private String size;

    // (선택)
    private String color;

    // 재고 수량 (필수)
    @NotNull(message = "재고는 필수입니다.")
    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    private Integer stock;
}
