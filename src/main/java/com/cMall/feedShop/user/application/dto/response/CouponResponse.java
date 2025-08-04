package com.cMall.feedShop.user.application.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponResponse {
    private String couponName;
    private Double discountValue;
    private boolean isFreeShipping;
    private String couponStatus; // Enum을 문자열로 변환하여 보낼 수 있습니다.
    private LocalDateTime expiresAt;
}