package com.cMall.feedShop.order.application.dto.response.info;

import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemInfo {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private OptionDetails optionDetails;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal totalPrice;
    private BigDecimal finalPrice;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionDetails {
        private Gender gender;
        private Size size;
        private Color color;
    }

    public static OrderItemInfo from(OrderItem orderItem) {
        return OrderItemInfo.builder()
                .orderItemId(orderItem.getOrderItemId())
                .productId(orderItem.getProductOption().getProduct().getProductId())
                .productName(orderItem.getProductOption().getProduct().getName())
                .optionDetails(OptionDetails.builder()
                        .gender(orderItem.getProductOption().getGender())
                        .size(orderItem.getProductOption().getSize())
                        .color(orderItem.getProductOption().getColor())
                        .build())
                .imageUrl(orderItem.getProductImage().getUrl())
                .quantity(orderItem.getQuantity())
                .totalPrice(orderItem.getTotalPrice())
                .finalPrice(orderItem.getFinalPrice())
                .build();
    }
}
