package com.cMall.feedShop.event.domain.enums;

public enum EventType {
    BATTLE,   // 배틀 이벤트: 랜덤 매칭으로 2명씩 대결
    RANKING;  // 랭킹 이벤트: 투표 수 기준으로 순위 결정
    
    /**
     * 기존 MISSION 타입과의 호환성을 위한 메서드
     * 데이터베이스에 MISSION 값이 남아있을 경우 RANKING으로 매핑
     */
    public static EventType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return EventType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 기존 MISSION 타입을 RANKING으로 매핑
            if ("MISSION".equalsIgnoreCase(value)) {
                return RANKING;
            }
            // 기존 MULTIPLE 타입을 RANKING으로 매핑
            if ("MULTIPLE".equalsIgnoreCase(value)) {
                return RANKING;
            }
            throw e;
        }
    }
    
    /**
     * 이벤트 타입이 유효한지 확인
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        
        try {
            fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 