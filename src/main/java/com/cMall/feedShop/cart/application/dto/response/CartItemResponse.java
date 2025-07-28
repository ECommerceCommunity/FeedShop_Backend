package com.cMall.feedShop.cart.application.dto.response;

import com.cMall.feedShop.cart.domain.model.CartItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CartItemResponse {
    private Long cartItemId;
    private Long cartId;
    private Long optionId;
    private Long imageId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartItemResponse from(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .cartId(cartItem.getCart() != null ? cartItem.getCart().getCartId() : null)
                .optionId(cartItem.getOptionId())
                .imageId(cartItem.getImageId())
                .quantity(cartItem.getQuantity())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}
