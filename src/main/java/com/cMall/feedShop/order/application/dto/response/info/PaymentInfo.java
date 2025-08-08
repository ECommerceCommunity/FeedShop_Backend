package com.cMall.feedShop.order.application.dto.response.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfo {
    private String paymentMethod;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvc;
}
