package com.cMall.feedShop.cart.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WishListRequest {

    @NotNull(message = "상품 ID는 필수입니다.")
    @Positive(message = "상품 ID는 양수여야 합니다.")
    private Long productId;

    public WishListRequest(Long productId) {
        this.productId = productId;
    }
}
