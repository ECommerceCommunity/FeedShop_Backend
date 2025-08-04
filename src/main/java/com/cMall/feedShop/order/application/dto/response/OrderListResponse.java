package com.cMall.feedShop.order.application.dto.response;

import com.cMall.feedShop.order.application.dto.response.info.OrderItemInfo;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderListResponse {
    private Long orderId;
    private OrderStatus status;
    private LocalDateTime orderedAt;
    private String currency;
    private BigDecimal deliveryFee;
    private BigDecimal totalDiscountPrice;
    private BigDecimal totalPrice;
    private BigDecimal finalPrice;
    private Integer usedPoints;
    private Integer earnedPoints;
    private List<OrderItemInfo> items;

    public static OrderListResponse from(Order order) {
        // 총 할인 금액을 계산해서 넣어줌(총 가격 - (최종 결제금액 + 배송비))
        // 최종 결제금액은 배송비가 더 해진 값이기 때문에
        // 배송비를 같이 빼줘야 정확한 총 할인 금액이 나온다.
        BigDecimal totalDiscountedPrice = order.getTotalPrice()
                .subtract(order.getFinalPrice())
                .add(order.getDeliveryFee());

        return OrderListResponse.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .orderedAt(order.getCreatedAt())
                .currency(order.getCurrency())
                .deliveryFee(order.getDeliveryFee())
                .totalDiscountPrice(totalDiscountedPrice)
                .totalPrice(order.getTotalPrice())
                .finalPrice(order.getFinalPrice())
                .usedPoints(order.getUsedPoints())
                .earnedPoints(order.getEarnedPoints())
                .items(order.getOrderItems().stream()
                        .map(OrderItemInfo::from)
                        .toList())
                .build();
    }
}
