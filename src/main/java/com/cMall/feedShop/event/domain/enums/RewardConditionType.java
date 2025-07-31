package com.cMall.feedShop.event.domain.enums;

/**
 * 이벤트 보상 조건 타입
 * 
 * <p>이벤트에서 보상을 지급하기 위한 조건을 정의합니다.</p>
 * 
 * <ul>
 *   <li><strong>RANK</strong>: 등수 기반 보상 (1등, 2등, 3등 등)</li>
 *   <li><strong>PARTICIPATION</strong>: 참여자 전원에게 지급</li>
 *   <li><strong>VOTERS</strong>: 투표자수 TOP에게 지급</li>
 *   <li><strong>VIEWS</strong>: 조회수 TOP에게 지급</li>
 *   <li><strong>LIKES</strong>: 좋아요 TOP에게 지급</li>
 *   <li><strong>RANDOM</strong>: 랜덤 추첨으로 지급</li>
 * </ul>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
public enum RewardConditionType {
    /** 등수 기반 보상 */
    RANK("등수"),
    /** 참여자 전원 보상 */
    PARTICIPATION("참여자"),
    /** 투표자수 TOP 보상 */
    VOTERS("투표자수 TOP"),
    /** 조회수 TOP 보상 */
    VIEWS("조회수 TOP"),
    /** 좋아요 TOP 보상 */
    LIKES("좋아요 TOP"),
    /** 랜덤 추첨 보상 */
    RANDOM("랜덤 추첨");

    private final String description;

    RewardConditionType(String description) {
        this.description = description;
    }

    /**
     * 조건 타입의 한글 설명을 반환합니다.
     * 
     * @return 조건 타입 설명
     */
    public String getDescription() {
        return description;
    }

    /**
     * 문자열로부터 RewardConditionType을 찾습니다.
     * 
     * <p>숫자 문자열인 경우 RANK로 처리하고, 그 외의 경우는 대소문자를 구분하지 않고 매칭합니다.</p>
     * 
     * @param value 조건값 문자열
     * @return 매칭되는 RewardConditionType, 매칭되지 않으면 null
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
     * 등수 조건인지 확인합니다.
     * 
     * @return 등수 조건이면 true, 그렇지 않으면 false
     */
    public boolean isRank() {
        return this == RANK;
    }

    /**
     * 특별 조건인지 확인합니다.
     * 
     * @return 등수가 아닌 특별 조건이면 true, 그렇지 않으면 false
     */
    public boolean isSpecialCondition() {
        return this != RANK;
    }
} 