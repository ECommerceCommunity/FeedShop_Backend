package com.cMall.feedShop.user.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequestDto {
    private String recipientName;
    private String recipientPhone;
    private String zipCode;
    private String addressLine1;
    private String addressLine2;
    private boolean isDefault;
}
