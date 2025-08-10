package com.cMall.feedShop.user.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponResponse {
    private String couponName;
    private BigDecimal discountValue;
    private boolean isFreeShipping;
    private String couponStatus;
    private LocalDateTime expiresAt;
}