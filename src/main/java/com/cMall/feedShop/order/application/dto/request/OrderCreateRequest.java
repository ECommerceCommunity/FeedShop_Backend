package com.cMall.feedShop.order.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {
    private Long userId;
    private String deliveryAddress;
    private String paymentMethod;
    private BigDecimal totalPrice;
}
