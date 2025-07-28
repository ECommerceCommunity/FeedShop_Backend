package com.cMall.feedShop.review.domain.enums;

public enum Stability {
    VERY_UNSTABLE("매우 불안정"),
    UNSTABLE("불안정"),
    NORMAL("보통"),
    STABLE("안정적"),
    VERY_STABLE("매우 안정적");

    private final String description;

    Stability(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}