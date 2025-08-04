package com.cMall.feedShop.order.application.dto.response;

import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.Order;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderCreateResponse {
    private Long orderId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private BigDecimal deliveryFee;
    private Integer usedPoints;
    private Integer earnedPoints;
    private String paymentMethod;
    private LocalDateTime orderedAt;

    public static OrderCreateResponse from(Order order) {
        return OrderCreateResponse.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .deliveryFee(order.getDeliveryFee())
                .usedPoints(order.getUsedPoints())
                .earnedPoints(order.getEarnedPoints())
                .paymentMethod(order.getPaymentMethod())
                .orderedAt(order.getCreatedAt())
                .build();
    }
}
