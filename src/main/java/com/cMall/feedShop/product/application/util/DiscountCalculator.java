package com.cMall.feedShop.product.application.util;

import com.cMall.feedShop.product.domain.enums.DiscountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DiscountCalculator {

    /**
     * 할인가를 계산한다.
     * originalPrice : 원가 (ex. 50000)
     * discountType : 할인 타입 (ex. NONE, FIXED_DISCOUNT, RATE_DISCOUNT)
     * discountValue : 할인 값
     * (ex. discountType이 NONE일 경우 해당 없음.
     * FIXED_DISCOUNT일 경우 3000 => 50000 - 3000 = 47000 (실제 결제 금액),
     * RATE_DISCOUNT일 경우 30(%) => 50000 * (1 - 30/100) = 35000 (실제 결제 금액))
     */

    public BigDecimal calculateDiscountPrice(BigDecimal originalPrice, DiscountType discountType, BigDecimal discountValue) {

        // null, discountType==NONE 예외처리
        if (originalPrice == null || discountType == null || discountType == DiscountType.NONE) {
            return originalPrice;
        }

        // null, discountValue==0 예외처리
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            return originalPrice;
        }

        switch (discountType) {
            case FIXED_DISCOUNT:
                // 원가(originalPrice) - 할인 금액(discountValue)
                BigDecimal fixedResult = originalPrice.subtract(discountValue);
                return fixedResult.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : fixedResult;
            case RATE_DISCOUNT:
                // 원가 * (1 - discountValue/100)

                // discountRate = discountValue/100
                // 소수점 4자리까지(scale = 4) 계산하고 반올림(HALF_UP)
                // ex) 15 -> 0.1500
                BigDecimal discountRate = discountValue.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

                // rateResult = originalPrice * (1 - discountRate)
                BigDecimal rateResult = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate));

                // 소수점 첫째 자리에서(scale = 0) 반올림.
                // ex) 8500.50 -> 8501
                return rateResult.setScale(0, RoundingMode.HALF_UP);
            default:
                return originalPrice;
        }
    }
}
