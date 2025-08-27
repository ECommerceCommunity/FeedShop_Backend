package com.cMall.feedShop.common.ai;

public final class PromptTemplates {
    public static final String PRODUCT_RECOMMENDATION_TEMPLATE = """
            신발 쇼핑몰에서 다음 조건을 고려하여 상품 %d개를 추천해주세요:
            === 사용자 요청 ===
            "%s"
            %s
            === 추천 기준 ===
            - 사용자의 발 사이즈에 맞는 상품 우선 추천
            - 발 너비와 발등 높이를 고려한 편안한 핏
            응답은 다음 JSON 형식으로만 작성해주세요:
            {"status":"OK", "productIds": [1, 2, 3, 4, 5], "message": ""}
            """;
}
