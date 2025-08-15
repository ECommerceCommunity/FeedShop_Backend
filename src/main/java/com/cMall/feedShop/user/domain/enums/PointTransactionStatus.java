package com.cMall.feedShop.user.domain.enums;


public enum PointTransactionStatus {
    ACTIVE,    // 적립 후 유효한 상태
    USED,      // 사용된 상태
    EXPIRED,   // 만료된 상태
    CANCELLED  // 취소된 상태
}
