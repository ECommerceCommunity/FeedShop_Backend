package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.application.dto.request.FeedSearchRequest;
import com.cMall.feedShop.feed.domain.entity.Feed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 피드 검색을 위한 QueryDSL Repository 인터페이스
 */
public interface FeedQueryRepository {
    
    /**
     * 검색 조건에 따른 피드 개수 조회
     * 
     * @param request 검색 요청 조건
     * @return 검색 조건에 맞는 피드 개수
     */
    long countWithSearchConditions(FeedSearchRequest request);
    
    /**
     * 검색 조건에 따른 피드 목록 조회
     * 
     * @param request 검색 요청 조건
     * @param pageable 페이징 정보
     * @return 검색 조건에 맞는 피드 목록 (페이징)
     */
    Page<Feed> findWithSearchConditions(FeedSearchRequest request, Pageable pageable);
}
