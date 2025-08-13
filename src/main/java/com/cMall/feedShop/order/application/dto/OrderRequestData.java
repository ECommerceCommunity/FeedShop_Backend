package com.cMall.feedShop.order.application.dto;

import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import com.cMall.feedShop.order.application.dto.request.DirectOrderCreateRequest;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderRequestData {
    private String deliveryAddress;
    private String deliveryDetailAddress;
    private String postalCode;
    private String recipientName;
    private String recipientPhone;
    private String deliveryMessage;

    private Integer usedPoints;
    private String paymentMethod;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvc;

    private BigDecimal deliveryFee;

    public static OrderRequestData from(OrderCreateRequest request) {
        return OrderRequestData.builder()
                // 배송 정보
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryDetailAddress(request.getDeliveryDetailAddress())
                .postalCode(request.getPostalCode())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .deliveryMessage(request.getDeliveryMessage())

                // 결제 정보
                .usedPoints(request.getUsedPoints())
                .paymentMethod(request.getPaymentMethod())
                .cardNumber(request.getCardNumber())
                .cardExpiry(request.getCardExpiry())
                .cardCvc(request.getCardCvc())

                // 배송비 (장바구니 주문은 요청에 포함)
                .deliveryFee(request.getDeliveryFee())
                .build();
    }

    public static OrderRequestData from(DirectOrderCreateRequest request) {
        return OrderRequestData.builder()
                // 배송 정보
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryDetailAddress(request.getDeliveryDetailAddress())
                .postalCode(request.getPostalCode())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .deliveryMessage(request.getDeliveryMessage())

                // 결제 정보
                .usedPoints(request.getUsedPoints())
                .paymentMethod(request.getPaymentMethod())
                .cardNumber(request.getCardNumber())
                .cardExpiry(request.getCardExpiry())
                .cardCvc(request.getCardCvc())

                // 배송비 (직접 주문은 서버에서 계산)
                .deliveryFee(request.getDeliveryFee())
                .build();
    }
}
