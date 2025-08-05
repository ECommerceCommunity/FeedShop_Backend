package com.cMall.feedShop.product.application.validator;

import java.math.BigDecimal;

/**
 * 가격 유효성 검증을 담당하는 클래스
 * 가격 범위가 올바른지 확인한다.
 */
public class PriceValidator {

    /**
     * 가격 범위가 올바른지 검증한다.
     *
     * @param minPrice 최소 가격 (null 허용)
     * @param maxPrice 최대 가격 (null 허용)
     * @return 가격 범위가 올바르면 true, 아니면 false
     */
    public static boolean isValidPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        // 둘 중 하나라도 null인 경우는 유효한 가격 범위로 간주한다.
        if (minPrice == null || maxPrice == null) {
            return true;
        }

        // 최소 가격이 최대 가격보다 작거나 같으면 유효하다.
        return minPrice.compareTo(maxPrice) <= 0;
    }
}