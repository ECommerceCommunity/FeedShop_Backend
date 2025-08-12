package com.cMall.feedShop.user.domain.enums;

public enum PointTransactionType {
    EARN("적립"),
    USE("사용"),
    EXPIRE("만료"),
    CANCEL("취소");

    private final String description;

    PointTransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

