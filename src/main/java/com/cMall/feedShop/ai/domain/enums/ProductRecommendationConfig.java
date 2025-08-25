package com.cMall.feedShop.ai.domain.enums;

public enum ProductRecommendationConfig {
    MIN_PRODUCT_COUNT(1),
    MAX_PRODUCT_COUNT(20),
    DEFAULT_PRODUCT_COUNT(5),
    MAX_RETRY_COUNT(3),
    TIMEOUT_SECONDS(30);

    private final int value;

    ProductRecommendationConfig(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // 상품 추천 개수 유효성 검사
    public static int validateProductCount(int limit) {
        return Math.max(MIN_PRODUCT_COUNT.getValue(),
                Math.min(MAX_PRODUCT_COUNT.getValue(), limit));
    }
}
