package com.cMall.feedShop.order.application.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class OrderCalculation {
    // 할인 적용되기 전 금액
    private BigDecimal totalAmount;

    // 최종 금액 (할인 적용된)
    private BigDecimal finalAmount;

    // 사용할 포인트
    private Integer actualUsedPoints;

    // 적립될 포인트
    private Integer earnedPoints;
}
