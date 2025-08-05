package com.cMall.feedShop.order.application.dto.response;

import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.Order;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderStatusUpdateResponse {

    private Long orderId;
    private OrderStatus status;

    public static OrderStatusUpdateResponse from(Order order) {
        return OrderStatusUpdateResponse.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .build();
    }
}
