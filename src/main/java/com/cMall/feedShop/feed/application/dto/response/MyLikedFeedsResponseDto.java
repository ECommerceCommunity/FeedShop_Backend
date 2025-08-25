package com.cMall.feedShop.feed.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 좋아요한 피드 목록 응답 DTO
 * 페이징 정보와 함께 사용자가 좋아요를 누른 피드 목록을 제공
 */
@Getter
@Builder
public class MyLikedFeedsResponseDto {
    
    /**
     * 좋아요한 피드 목록
     */
    private final List<MyLikedFeedItemDto> content;
    
    /**
     * 현재 페이지 (0부터 시작)
     */
    private final int page;
    
    /**
     * 페이지 크기
     */
    private final int size;
    
    /**
     * 전체 피드 개수
     */
    private final long totalElements;
    
    /**
     * 전체 페이지 수
     */
    private final int totalPages;
    
    /**
     * 첫 번째 페이지 여부
     */
    private final boolean first;
    
    /**
     * 마지막 페이지 여부
     */
    private final boolean last;
    
    /**
     * 다음 페이지 존재 여부
     */
    private final boolean hasNext;
    
    /**
     * 이전 페이지 존재 여부
     */
    private final boolean hasPrevious;
}
