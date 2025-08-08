package com.cMall.feedShop.order.application.dto.response;

import com.cMall.feedShop.order.application.dto.response.info.OrderItemDetailInfo;
import com.cMall.feedShop.order.application.dto.response.info.PaymentInfo;
import com.cMall.feedShop.order.application.dto.response.info.ShippingInfo;
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
public class OrderDetailResponse {
    private Long orderId;
    private OrderStatus status;
    private LocalDateTime orderedAt;
    private Integer usedPoints;
    private Integer earnedPoints;
    private String currency;
    private BigDecimal deliveryFee;
    private BigDecimal totalDiscountPrice;
    private BigDecimal totalPrice;
    private BigDecimal finalPrice;
    private ShippingInfo shippingInfo;
    private PaymentInfo paymentInfo;
    private List<OrderItemDetailInfo> items;

    public static OrderDetailResponse from(Order order) {
        // 포인트 할인 전 상품 가격 총합을 계산한다.
        // (OrderItem의 totalPrice{원가} * quantity)
        BigDecimal originalTotalPrice = order.getOrderItems().stream()
                .map(item -> item.getTotalPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 1. 배송비를 제외한 최종 결제 상품 금액을 먼저 계산한다.
        BigDecimal finalItemPrice = order.getFinalPrice().subtract(order.getDeliveryFee());

        // 2. 할인 전 상품 총 금액에서 최종 상품 금액을 빼서 총 할인 금액을 구한다.
        BigDecimal totalDiscountPrice = originalTotalPrice.subtract(finalItemPrice);

        return OrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .orderedAt(order.getCreatedAt())
                .usedPoints(order.getUsedPoints())
                .earnedPoints(order.getEarnedPoints())
                .currency(order.getCurrency())
                .deliveryFee(order.getDeliveryFee())
                .totalDiscountPrice(totalDiscountPrice)
                .totalPrice(originalTotalPrice)
                .finalPrice(order.getFinalPrice())
                .shippingInfo(ShippingInfo.builder()
                        .recipientName(order.getRecipientName())
                        .recipientPhone(order.getRecipientPhone())
                        .postalCode(order.getPostalCode())
                        .deliveryAddress(order.getDeliveryAddress())
                        .deliveryDetailAddress(order.getDeliveryDetailAddress())
                        .deliveryMessage(order.getDeliveryMessage())
                        .build())
                .paymentInfo(PaymentInfo.builder()
                        .paymentMethod(order.getPaymentMethod())
                        .cardNumber(maskCardNumber(order.getCardNumber()))
                        .cardExpiry(order.getCardExpiry())
                        .cardCvc(order.getCardCvc())
                        .build())
                .items(order.getOrderItems().stream()
                        .map(orderItem -> OrderItemDetailInfo.builder()
                                .orderItemId(orderItem.getOrderItemId())
                                .productId(orderItem.getProductOption().getProduct().getProductId())
                                .productName(orderItem.getProductOption().getProduct().getName())
                                .optionId(orderItem.getProductOption().getOptionId())
                                .optionDetails(OrderItemDetailInfo.OptionDetails.builder()
                                        .gender(orderItem.getProductOption().getGender())
                                        .size(orderItem.getProductOption().getSize())
                                        .color(orderItem.getProductOption().getColor())
                                        .build())
                                .imageId(orderItem.getProductImage().getImageId())
                                .imageUrl(orderItem.getProductImage().getUrl())
                                .quantity(orderItem.getQuantity())
                                .totalPrice(orderItem.getTotalPrice())
                                .finalPrice(orderItem.getFinalPrice())
                                .orderedAt(orderItem.getCreatedAt())
                                .build())
                        .toList())
                .build();
    }

    // 카드 번호는 마스킹 처리를 한다.
    private static String maskCardNumber(String cardNumber) {
        // 이미 유효성 검증에서 검증하기 때문에 null 이나 4 이하로 올 가능성은 낮음.
        // 하지만 혹시 모르는 상황을 위한 방어 코드 처리.
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }

        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
