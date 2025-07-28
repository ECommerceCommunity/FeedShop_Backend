package com.cMall.feedShop.review.domain.enums;

public enum Cushion {
    VERY_SOFT("매우 부드러움"),
    SOFT("부드러움"),
    MEDIUM("보통"),
    FIRM("단단함"),
    VERY_FIRM("매우 단단함");

    private final String description;

    Cushion(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}