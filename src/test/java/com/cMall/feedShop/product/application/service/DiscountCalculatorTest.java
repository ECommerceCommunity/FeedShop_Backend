package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.util.DiscountCalculator;

import com.cMall.feedShop.product.domain.enums.DiscountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DiscountCalculator 테스트")
class DiscountCalculatorTest {

    private DiscountCalculator discountCalculator;

    @BeforeEach
    void setUp() {
        discountCalculator = new DiscountCalculator();
    }

    @Test
    @DisplayName("할인 없음 - 원가 그대로 반환")
    void calculateDiscountPrice_None() {
        // given
        BigDecimal originalPrice = new BigDecimal("50000");

        // when
        BigDecimal result = discountCalculator.calculateDiscountPrice(
                originalPrice, DiscountType.NONE, new BigDecimal("10"));

        // then
        assertThat(result).isEqualTo(originalPrice);
    }

    @Test
    @DisplayName("고정 할인 - 할인 금액 차감")
    void calculateDiscountPrice_FixedDiscount() {
        // given
        BigDecimal originalPrice = new BigDecimal("50000");
        BigDecimal discountValue = new BigDecimal("5000");

        // when
        BigDecimal result = discountCalculator.calculateDiscountPrice(
                originalPrice, DiscountType.FIXED_DISCOUNT, discountValue);

        // then
        assertThat(result).isEqualTo(new BigDecimal("45000"));
    }

    @Test
    @DisplayName("고정 할인 - 할인 금액이 원가보다 큰 경우 0 반환")
    void calculateDiscountPrice_FixedDiscount_ExceedsOriginalPrice() {
        // given
        BigDecimal originalPrice = new BigDecimal("30000");
        BigDecimal discountValue = new BigDecimal("50000");

        // when
        BigDecimal result = discountCalculator.calculateDiscountPrice(
                originalPrice, DiscountType.FIXED_DISCOUNT, discountValue);

        // then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("비율 할인 - 퍼센트 할인 적용")
    void calculateDiscountPrice_RateDiscount() {
        // given
        BigDecimal originalPrice = new BigDecimal("50000");
        BigDecimal discountValue = new BigDecimal("20"); // 20%

        // when
        BigDecimal result = discountCalculator.calculateDiscountPrice(
                originalPrice, DiscountType.RATE_DISCOUNT, discountValue);

        // then
        assertThat(result).isEqualTo(new BigDecimal("40000"));
    }

    @Test
    @DisplayName("비율 할인 - 소수점 반올림 처리")
    void calculateDiscountPrice_RateDiscount_Rounding() {
        // given
        BigDecimal originalPrice = new BigDecimal("33333");
        BigDecimal discountValue = new BigDecimal("15"); // 15%

        // when
        BigDecimal result = discountCalculator.calculateDiscountPrice(
                originalPrice, DiscountType.RATE_DISCOUNT, discountValue);

        // then
        assertThat(result).isEqualTo(new BigDecimal("28333")); // 반올림 적용
    }

    @Test
    @DisplayName("null 값 처리 - 원가 null")
    void calculateDiscountPrice_NullOriginalPrice() {
        // when
        BigDecimal result = discountCalculator.calculateDiscountPrice(
                null, DiscountType.RATE_DISCOUNT, new BigDecimal("10"));

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("null 값 처리 - 할인 타입 null")
    void calculateDiscountPrice_NullDiscountType() {
        // given
        BigDecimal originalPrice = new BigDecimal("50000");

        // when
        BigDecimal result = discountCalculator.calculateDiscountPrice(
                originalPrice, null, new BigDecimal("10"));

        // then
        assertThat(result).isEqualTo(originalPrice);
    }

    @Test
    @DisplayName("null 값 처리 - 할인 값 null")
    void calculateDiscountPrice_NullDiscountValue() {
        // given
        BigDecimal originalPrice = new BigDecimal("50000");

        // when
        BigDecimal result = discountCalculator.calculateDiscountPrice(
                originalPrice, DiscountType.RATE_DISCOUNT, null);

        // then
        assertThat(result).isEqualTo(originalPrice);
    }

    @Test
    @DisplayName("0 이하 할인 값 처리")
    void calculateDiscountPrice_ZeroOrNegativeDiscountValue() {
        // given
        BigDecimal originalPrice = new BigDecimal("50000");

        // when
        BigDecimal result1 = discountCalculator.calculateDiscountPrice(
                originalPrice, DiscountType.RATE_DISCOUNT, BigDecimal.ZERO);
        BigDecimal result2 = discountCalculator.calculateDiscountPrice(
                originalPrice, DiscountType.RATE_DISCOUNT, new BigDecimal("-5"));

        // then
        assertThat(result1).isEqualTo(originalPrice);
        assertThat(result2).isEqualTo(originalPrice);
    }
}