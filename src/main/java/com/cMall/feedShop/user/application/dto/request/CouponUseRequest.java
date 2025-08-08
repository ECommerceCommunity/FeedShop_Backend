package com.cMall.feedShop.user.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CouponUseRequest {
    private String email;
    private String couponCode;
}