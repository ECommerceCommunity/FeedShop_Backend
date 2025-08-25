package com.cMall.feedShop.feed.application.dto.request;

import com.cMall.feedShop.feed.domain.FeedType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 피드 검색 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedSearchRequest {
    
    // 기본 검색
    private String keyword;                    // 검색 키워드 (제목, 내용, 해시태그)
    private Long authorId;                     // 작성자 ID
    private FeedType feedType;                 // 피드 타입 (DAILY, EVENT, RANKING)
    
    // 날짜 범위
    private LocalDateTime startDate;           // 시작 날짜
    private LocalDateTime endDate;             // 종료 날짜
    
    // 상품 정보
    private String productName;                // 상품명
    private Long productId;                    // 상품 ID
    
    // 이벤트 정보
    private Long eventId;                      // 이벤트 ID
    private String eventTitle;                 // 이벤트 제목
    
    // 해시태그
    private List<String> hashtags;             // 해시태그 목록
    
    // 페이징 및 정렬
    private Integer page;                      // 페이지 번호
    private Integer size;                      // 페이지 크기
    private String sort;                       // 정렬 기준
    
    /**
     * 키워드가 있는지 확인
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    /**
     * 작성자가 있는지 확인
     */
    public boolean hasAuthor() {
        return authorId != null;
    }
    
    /**
     * 피드 타입이 있는지 확인
     */
    public boolean hasFeedType() {
        return feedType != null;
    }
    
    /**
     * 날짜 범위가 있는지 확인
     */
    public boolean hasDateRange() {
        return startDate != null || endDate != null;
    }
    
    /**
     * 상품명이 있는지 확인
     */
    public boolean hasProductName() {
        return productName != null && !productName.trim().isEmpty();
    }
    
    /**
     * 상품 ID가 있는지 확인
     */
    public boolean hasProductId() {
        return productId != null;
    }
    
    /**
     * 이벤트 ID가 있는지 확인
     */
    public boolean hasEventId() {
        return eventId != null;
    }
    
    /**
     * 이벤트 제목이 있는지 확인
     */
    public boolean hasEventTitle() {
        return eventTitle != null && !eventTitle.trim().isEmpty();
    }
    
    /**
     * 해시태그가 있는지 확인
     */
    public boolean hasHashtags() {
        return hashtags != null && !hashtags.isEmpty();
    }
}
