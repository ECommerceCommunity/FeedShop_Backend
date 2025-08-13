package com.cMall.feedShop.order.application.constants;

import java.math.BigDecimal;

public class OrderConstants {
    // 구매 금액의 {POINT_USAGE_RATE}% 까지만 포인트 사용 가능
    public static final BigDecimal POINT_USAGE_RATE = BigDecimal.valueOf(0.1);

    // {POINT_REWARD_THRESHOLD}원 단위로 {POINT_REWARD_AMOUNT} 포인트를 적립
    public static final BigDecimal POINT_REWARD_THRESHOLD = BigDecimal.valueOf(10000);
    public static final BigDecimal POINT_REWARD_AMOUNT = BigDecimal.valueOf(50);

    // 포인트 사용 단위
    public static final int POINT_UNIT = 100;

    // 최대 주문 수량
    public static final int MAX_ORDER_QUANTITY = 50;
}
