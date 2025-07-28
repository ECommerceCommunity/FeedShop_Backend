package com.cMall.feedShop.review.domain.enums;

public enum SizeFit {
    VERY_SMALL("매우 작음"),
    SMALL("작음"),
    NORMAL("보통"),
    BIG("큼"),
    VERY_BIG("매우 큼");

    private final String description;

    SizeFit(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
