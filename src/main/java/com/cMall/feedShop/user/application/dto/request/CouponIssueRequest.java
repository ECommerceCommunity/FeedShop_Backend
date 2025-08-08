package com.cMall.feedShop.user.application.dto.request;

import com.cMall.feedShop.user.domain.enums.DiscountType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CouponIssueRequest {
    private String email;
    private String couponCode;
    private String couponName;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private boolean isFreeShipping;
    private LocalDateTime expiresAt;
}