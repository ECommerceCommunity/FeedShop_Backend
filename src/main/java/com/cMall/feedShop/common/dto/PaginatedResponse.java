package com.cMall.feedShop.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 페이징된 응답을 위한 제네릭 DTO
 * 
 * @param <T> 응답 데이터 타입
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
    
    /**
     * 응답 데이터 리스트
     */
    private List<T> content;
    
    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    private int page;
    
    /**
     * 페이지 크기
     */
    private int size;
    
    /**
     * 전체 데이터 개수
     */
    private long totalElements;
    
    /**
     * 전체 페이지 개수
     */
    private int totalPages;
    
    /**
     * 다음 페이지 존재 여부
     */
    private boolean hasNext;
    
    /**
     * 이전 페이지 존재 여부
     */
    private boolean hasPrevious;
    
    /**
     * 첫 번째 페이지 여부
     */
    public boolean isFirst() {
        return page == 0;
    }
    
    /**
     * 마지막 페이지 여부
     */
    public boolean isLast() {
        return !hasNext;
    }
    
    /**
     * 빈 페이지 여부
     */
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
    
    /**
     * 현재 페이지의 데이터 개수
     */
    public int getNumberOfElements() {
        return content != null ? content.size() : 0;
    }
} 