package com.cMall.feedShop.user.domain.model;

public enum BadgeType {
    // 첫 구매 관련
    FIRST_PURCHASE("첫 구매", "첫 구매를 완료했습니다", "/images/badges/first_purchase.png", 20),
    
    // 구매 횟수 관련
    PURCHASE_5("5회 구매", "5회 구매를 달성했습니다", "/images/badges/purchase_5.png", 25),
    PURCHASE_10("10회 구매", "10회 구매를 달성했습니다", "/images/badges/purchase_10.png", 30),
    PURCHASE_20("20회 구매", "20회 구매를 달성했습니다", "/images/badges/purchase_20.png", 40),
    PURCHASE_50("50회 구매", "50회 구매를 달성했습니다", "/images/badges/purchase_50.png", 60),
    PURCHASE_100("100회 구매", "100회 구매를 달성했습니다", "/images/badges/purchase_100.png", 100),
    
    // 구매 금액 관련
    AMOUNT_100K("10만원 구매", "총 구매금액 10만원을 달성했습니다", "/images/badges/amount_100k.png", 30),
    AMOUNT_500K("50만원 구매", "총 구매금액 50만원을 달성했습니다", "/images/badges/amount_500k.png", 50),
    AMOUNT_1M("100만원 구매", "총 구매금액 100만원을 달성했습니다", "/images/badges/amount_1m.png", 80),
    AMOUNT_5M("500만원 구매", "총 구매금액 500만원을 달성했습니다", "/images/badges/amount_5m.png", 150),
    
    // 리뷰 관련
    FIRST_REVIEW("첫 리뷰", "첫 리뷰를 작성했습니다", "/images/badges/first_review.png", 15),
    REVIEW_10("10개 리뷰", "10개의 리뷰를 작성했습니다", "/images/badges/review_10.png", 30),
    REVIEW_50("50개 리뷰", "50개의 리뷰를 작성했습니다", "/images/badges/review_50.png", 60),
    REVIEW_100("100개 리뷰", "100개의 리뷰를 작성했습니다", "/images/badges/review_100.png", 120),
    
    // 특별 뱃지 (레벨 관련)
    VIP("VIP 회원", "레벨 5 달성! VIP 회원이 되었습니다", "/images/badges/vip.png", 100),
    LOYAL_CUSTOMER("레전드", "레벨 10 달성! 최고의 단골 고객입니다", "/images/badges/loyal_customer.png", 200),
    
    // 이벤트 뱃지
    EARLY_ADOPTER("얼리 어답터", "서비스 초기 가입자입니다", "/images/badges/early_adopter.png", 50),
    ANNIVERSARY_2024("2024년 기념", "2024년 기념 이벤트 참여자입니다", "/images/badges/anniversary_2024.png", 30),
    
    // 소셜 관련
    SNS_CONNECTOR("소셜 커넥터", "SNS 계정을 연동했습니다", "/images/badges/sns_connector.png", 25),
    INFLUENCER("인플루언서", "인플루언서로 인증되었습니다", "/images/badges/influencer.png", 300);
    
    private final String name;
    private final String description;
    private final String imageUrl;
    private final int bonusPoints;
    
    BadgeType(String name, String description, String imageUrl, int bonusPoints) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.bonusPoints = bonusPoints;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public int getBonusPoints() {
        return bonusPoints;
    }
}
