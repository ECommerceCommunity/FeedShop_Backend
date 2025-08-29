package com.cMall.feedShop.review.domain.enums;

public enum Cushion {
    VERY_SOFT(1, "매우 부드러움"),
    SOFT(2, "부드러움"),
    MEDIUM(3, "보통"),
    FIRM(4, "단단함"),
    VERY_FIRM(5, "매우 단단함");

    private final int value;
    private final String description;

    Cushion(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static Cushion fromValue(Integer value) {
        if (value == null) {
            return MEDIUM; // 기본값
        }
        for (Cushion cushion : values()) {
            if (cushion.value == value) {
                return cushion;
            }
        }
        // 잘못된 값이면 기본값 반환 (예외 대신)
        return MEDIUM;
    }
}