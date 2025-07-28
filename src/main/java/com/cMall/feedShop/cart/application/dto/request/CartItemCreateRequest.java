package com.cMall.feedShop.cart.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartItemCreateRequest {
    @NotNull(message = "상품 옵션 ID는 필수입니다.")
    private Long optionId;

    @NotNull(message = "이미지 ID는 필수입니다.")
    private Long imageId;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Integer quantity;
}
