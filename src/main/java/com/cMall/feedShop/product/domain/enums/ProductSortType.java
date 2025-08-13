package com.cMall.feedShop.product.domain.enums;

public enum ProductSortType {
    LATEST("latest", "최신순"),
    POPULAR("popular", "인기순");

    private final String code;
    private final String description;

    ProductSortType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ProductSortType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return LATEST;
        }

        String trimmedCode = code.trim();
        for (ProductSortType type : values()) {
            if (type.code.equalsIgnoreCase(trimmedCode)) {
                return type;
            }
        }

        return LATEST; // 일치하는 타입이 없을 경우 기본값 반환
    }
}
