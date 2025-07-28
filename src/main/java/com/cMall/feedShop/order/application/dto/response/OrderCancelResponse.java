package com.cMall.feedShop.order.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCancelResponse {
    private Long orderId;
    private String status;
}
