package com.cMall.feedShop.user.domain.model;

public enum UserLevel {
    LEVEL_1(0, 0, "새싹", "🌱", "새로운 시작"),
    LEVEL_2(1, 100, "성장", "🌿", "포인트 1,000 지급"),
    LEVEL_3(2, 300, "발전", "🌳", "경험치 2배 이벤트 참여권"),
    LEVEL_4(3, 600, "도전", "🏅", "할인 쿠폰 제공"),
    LEVEL_5(4, 1000, "전문가", "👑", "이벤트 우선 참여권 + 인기 상품 우선 구매권"),
    LEVEL_6(5, 1500, "마스터", "💎", "VIP 혜택 + 전용 상품 접근권"),
    LEVEL_7(6, 2200, "레전드", "⭐", "개인 맞춤 서비스 + 특별 할인"),
    LEVEL_8(7, 3000, "챔피언", "🔥", "최고급 혜택 + 신상품 우선 체험"),
    LEVEL_9(8, 4000, "슈퍼스타", "✨", "인플루언서 프로그램 참여권"),
    LEVEL_10(9, 5500, "갓", "🚀", "모든 혜택 + 브랜드 앰버서더 자격");
    
    private final int levelNumber;
    private final int requiredPoints;
    private final String name;
    private final String emoji;
    private final String rewardDescription;
    
    UserLevel(int levelNumber, int requiredPoints, String name, String emoji, String rewardDescription) {
        this.levelNumber = levelNumber;
        this.requiredPoints = requiredPoints;
        this.name = name;
        this.emoji = emoji;
        this.rewardDescription = rewardDescription;
    }
    
    public int getLevelNumber() {
        return levelNumber;
    }
    
    public int getRequiredPoints() {
        return requiredPoints;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getRewardDescription() {
        return rewardDescription;
    }
    
    public String getDisplayName() {
        return String.format("Lv.%d %s %s", levelNumber + 1, emoji, name);
    }
    
    /**
     * 점수에 따른 레벨 계산
     */
    public static UserLevel fromPoints(int totalPoints) {
        for (int i = UserLevel.values().length - 1; i >= 0; i--) {
            UserLevel level = UserLevel.values()[i];
            if (totalPoints >= level.getRequiredPoints()) {
                return level;
            }
        }
        return LEVEL_1;
    }
    
    /**
     * 다음 레벨까지 필요한 점수
     */
    public int getPointsToNextLevel(int currentPoints) {
        UserLevel[] levels = UserLevel.values();
        if (this.ordinal() < levels.length - 1) {
            UserLevel nextLevel = levels[this.ordinal() + 1];
            return nextLevel.getRequiredPoints() - currentPoints;
        }
        return 0; // 최고 레벨
    }
}
