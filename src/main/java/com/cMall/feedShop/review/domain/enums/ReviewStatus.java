package com.cMall.feedShop.review.domain.enums;

public enum ReviewStatus {
    ACTIVE("활성"),
    HIDDEN("숨김"),
    DELETED("삭제됨");

    private final String description;

    ReviewStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}