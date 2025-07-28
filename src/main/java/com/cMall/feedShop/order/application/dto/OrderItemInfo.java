package com.cMall.feedShop.order.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class OrderItemInfo {
    private Long orderItemId;
    private Long optionId;
    private Integer quantity;
    private BigDecimal price;
}
