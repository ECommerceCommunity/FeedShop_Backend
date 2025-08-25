package com.cMall.feedShop.cart.application.dto.response;

import com.cMall.feedShop.cart.domain.model.WishList;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class WishListAddResponse {
    private Long wishlistId;
    private Long productId;
    private LocalDateTime createdAt;

    public WishListAddResponse(Long wishlistId, Long productId, LocalDateTime createdAt) {
        this.wishlistId = wishlistId;
        this.productId = productId;
        this.createdAt = createdAt;
    }

    public static WishListAddResponse from(WishList wishlist) {
        return new WishListAddResponse(
                wishlist.getWishlistId(),
                wishlist.getProduct().getProductId(),
                wishlist.getCreatedAt()
        );
    }
}
