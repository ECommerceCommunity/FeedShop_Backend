package com.cMall.feedShop.cart.application.dto.response.info;

import com.cMall.feedShop.product.domain.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class WishlistInfo {
    private Long wishlistId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal productPrice;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LocalDateTime createdAt;
}
