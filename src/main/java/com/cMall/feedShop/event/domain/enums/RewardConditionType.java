package com.cMall.feedShop.event.domain.enums;

/**
 * 이벤트 보상 조건 타입
 */
public enum RewardConditionType {
    RANK("등수"),
    PARTICIPATION("참여자"),
    VOTERS("투표자수 TOP"),
    VIEWS("조회수 TOP"),
    LIKES("좋아요 TOP"),
    RANDOM("랜덤 추첨");

    private final String description;

    RewardConditionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 문자열로부터 RewardConditionType 찾기
     */
    public static RewardConditionType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        // 숫자인 경우 RANK로 처리
        try {
            Integer.parseInt(value);
            return RANK;
        } catch (NumberFormatException e) {
            // 문자열 조건 처리
            switch (value.toLowerCase()) {
                case "participation":
                    return PARTICIPATION;
                case "voters":
                    return VOTERS;
                case "views":
                    return VIEWS;
                case "likes":
                    return LIKES;
                case "random":
                    return RANDOM;
                default:
                    return null;
            }
        }
    }

    /**
     * 등수인지 확인
     */
    public boolean isRank() {
        return this == RANK;
    }

    /**
     * 특별 조건인지 확인
     */
    public boolean isSpecialCondition() {
        return this != RANK;
    }
} 