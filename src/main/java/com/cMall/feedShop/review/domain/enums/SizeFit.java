package com.cMall.feedShop.review.domain.enums;

public enum SizeFit {
    VERY_SMALL(1, "매우 작음"),
    SMALL(2, "작음"),
    NORMAL(3, "적당함"),
    BIG(4, "큼"),
    VERY_BIG(5, "매우 큼");

    private final int value;
    private final String description;

    SizeFit(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static SizeFit fromValue(Integer value) {
        if (value == null) {
            return NORMAL; // 기본값
        }
        for (SizeFit sizeFit : values()) {
            if (sizeFit.value == value) {
                return sizeFit;
            }
        }
        // 잘못된 값이면 기본값 반환 (예외 대신)
        return NORMAL;
    }
}
