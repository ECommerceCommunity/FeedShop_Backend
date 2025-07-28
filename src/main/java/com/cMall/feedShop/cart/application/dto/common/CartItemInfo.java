package com.cMall.feedShop.cart.application.dto.common;

import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CartItemInfo {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private BigDecimal discountPrice;
    private Long optionId;
    private OptionDetails optionDetails;
    private Long imageId;
    private String imageUrl;
    private Integer quantity;
    private Boolean selected;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class OptionDetails {
        private String gender;
        private String size;
        private String color;
        private Integer stock;
    }

    public static CartItemInfo from(CartItem cartItem, Product product, ProductOption option, ProductImage image, BigDecimal discountPrice) {
        return CartItemInfo.builder()
                .cartItemId(cartItem.getCartItemId())
                .productId(product.getProductId())
                .productName(product.getName())
                .productPrice(product.getPrice())
                .discountPrice(discountPrice)
                .optionId(option.getOptionId())
                .optionDetails(OptionDetails.builder()
                        .gender(option.getGender().name())
                        .size(option.getSize().getValue())
                        .color(option.getColor().name())
                        .stock(option.getStock())
                        .build())
                .imageId(image.getImageId())
                .imageUrl(image.getUrl())
                .quantity(cartItem.getQuantity())
                .selected(cartItem.getSelected())
                .createdAt(cartItem.getCreatedAt())
                .build();
    }
}
