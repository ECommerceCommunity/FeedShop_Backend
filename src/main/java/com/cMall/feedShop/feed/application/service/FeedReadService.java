package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.FeedListResponseDto;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 피드 조회 서비스
 * 피드 목록 조회 등의 비즈니스 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedReadService {
    
    private final FeedRepository feedRepository;
    private final FeedMapper feedMapper;
    
    /**
     * 피드 목록 조회 (필터링, 페이징, 정렬)
     * 
     * @param feedType 피드 타입 (DAILY, EVENT, RANKING, null=전체)
     * @param pageable 페이징 및 정렬 정보
     * @return 피드 목록 페이지
     */
    public Page<FeedListResponseDto> getFeeds(FeedType feedType, Pageable pageable) {
        log.info("피드 목록 조회 - feedType: {}, page: {}, size: {}", 
                feedType, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Feed> feedPage;
        
        if (feedType != null) {
            // 특정 타입의 피드만 조회
            feedPage = feedRepository.findByFeedType(feedType.name(), pageable);
        } else {
            // 전체 피드 조회
            feedPage = feedRepository.findAll(pageable);
        }
        
        // Feed 엔티티를 DTO로 변환
        Page<FeedListResponseDto> responsePage = feedPage.map(feedMapper::toFeedListResponseDto);
        
        log.info("피드 목록 조회 완료 - 총 {}개, 현재 페이지 {}개", 
                responsePage.getTotalElements(), responsePage.getNumberOfElements());
        
        return responsePage;
    }
    
    /**
     * 피드 타입별 조회 (페이징)
     * 
     * @param feedType 피드 타입
     * @param pageable 페이징 및 정렬 정보
     * @return 피드 목록 페이지
     */
    public Page<FeedListResponseDto> getFeedsByType(FeedType feedType, Pageable pageable) {
        log.info("피드 타입별 조회 - feedType: {}, page: {}, size: {}", 
                feedType, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Feed> feedPage = feedRepository.findByFeedType(feedType.name(), pageable);
        Page<FeedListResponseDto> responsePage = feedPage.map(feedMapper::toFeedListResponseDto);
        
        log.info("피드 타입별 조회 완료 - feedType: {}, 총 {}개, 현재 페이지 {}개", 
                feedType, responsePage.getTotalElements(), responsePage.getNumberOfElements());
        
        return responsePage;
    }
} 