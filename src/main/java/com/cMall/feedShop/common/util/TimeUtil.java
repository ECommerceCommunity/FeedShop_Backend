package com.cMall.feedShop.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 한국 시간대를 일관되게 사용하기 위한 유틸리티 클래스
 * 
 * @author FeedShop Team
 * @since 1.0
 */
public class TimeUtil {
    
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    
    /**
     * 현재 한국 시간의 날짜를 반환합니다.
     * 
     * @return 한국 시간 기준 현재 날짜
     */
    public static LocalDate nowDate() {
        return ZonedDateTime.now(KOREA_ZONE).toLocalDate();
    }
    
    /**
     * 현재 한국 시간을 반환합니다.
     * 
     * @return 한국 시간 기준 현재 시간
     */
    public static LocalDateTime nowDateTime() {
        return ZonedDateTime.now(KOREA_ZONE).toLocalDateTime();
    }
    
    /**
     * 특정 날짜가 한국 시간 기준으로 유효한지 확인합니다.
     * 
     * @param date 확인할 날짜
     * @return 유효한 날짜이면 true
     */
    public static boolean isValidDate(LocalDate date) {
        if (date == null) return false;
        LocalDate today = nowDate();
        return !date.isBefore(today.minusDays(1)); // 어제까지는 허용
    }
} 