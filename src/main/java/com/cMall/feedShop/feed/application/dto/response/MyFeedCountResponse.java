package com.cMall.feedShop.feed.application.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 마이피드 개수 응답 DTO
 * 사용자의 피드 타입별 개수 정보를 제공
 */
@Getter
@Builder
public class MyFeedCountResponse {
    
    /**
     * 전체 피드 개수
     */
    private final long totalCount;
    
    /**
     * 일상 피드 개수
     */
    private final long dailyCount;
    
    /**
     * 이벤트 피드 개수
     */
    private final long eventCount;
    
    /**
     * 랭킹 피드 개수
     */
    private final long rankingCount;
}
