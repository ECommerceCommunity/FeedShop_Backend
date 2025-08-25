package com.cMall.feedShop.review.domain.enums;

public enum Stability {
    VERY_UNSTABLE(1, "매우 불안정"),
    UNSTABLE(2, "불안정"),
    NORMAL(3, "보통"),
    STABLE(4, "안정적"),
    VERY_STABLE(5, "매우 안정적");

    private final int value;
    private final String description;

    Stability(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static Stability fromValue(Integer value) {
        if (value == null) {
            return NORMAL; // 기본값
        }
        for (Stability stability : values()) {
            if (stability.value == value) {
                return stability;
            }
        }
        // 잘못된 값이면 기본값 반환 (예외 대신)
        return NORMAL;
    }
}