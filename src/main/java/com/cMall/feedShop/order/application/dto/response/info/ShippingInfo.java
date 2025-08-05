package com.cMall.feedShop.order.application.dto.response.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingInfo {
    private String recipientName;
    private String recipientPhone;
    private String postalCode;
    private String deliveryAddress;
    private String deliveryDetailAddress;
    private String deliveryMessage;
}
