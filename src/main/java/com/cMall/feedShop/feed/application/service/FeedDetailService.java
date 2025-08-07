package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 피드 상세 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedDetailService {
    
    private final FeedRepository feedRepository;
    private final FeedMapper feedMapper;
    
    /**
     * 피드 상세 조회
     * 
     * @param feedId 피드 ID
     * @return 피드 상세 정보
     * @throws FeedNotFoundException 피드를 찾을 수 없는 경우
     */
    public FeedDetailResponseDto getFeedDetail(Long feedId) {
        log.info("피드 상세 조회 요청 - feedId: {}", feedId);
        
        // 피드 조회 (삭제되지 않은 피드만)
        Feed feed = feedRepository.findDetailById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));
        
        // 피드 조회 가능 여부 확인
        if (!feed.isViewable()) {
            log.warn("삭제된 피드 조회 시도 - feedId: {}", feedId);
            throw new FeedNotFoundException(feedId, "삭제된 피드입니다.");
        }
        
        log.info("피드 상세 조회 완료 - feedId: {}, 제목: {}", feedId, feed.getTitle());
        
        // DTO 변환 및 반환
        return feedMapper.toDetailResponseDto(feed);
    }
}
