package com.cMall.feedShop.review.domain.enums;

public enum ReportReason {
    ABUSIVE_LANGUAGE("욕설 및 비방"),
    SPAM("스팸 및 도배"),
    INAPPROPRIATE_CONTENT("부적절한 내용"),
    FALSE_INFORMATION("허위 정보"),
    ADVERTISING("광고성 내용"),
    COPYRIGHT_VIOLATION("저작권 침해"),
    OTHER("기타");

    private final String description;

    ReportReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}