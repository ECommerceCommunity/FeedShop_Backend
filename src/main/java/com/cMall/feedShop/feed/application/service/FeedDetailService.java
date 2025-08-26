package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.application.service.FeedLikeService;
import com.cMall.feedShop.feed.application.service.FeedVoteService;
import com.cMall.feedShop.feed.application.service.FeedServiceUtils;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.application.service.FeedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final FeedLikeService feedLikeService;
    private final FeedVoteService feedVoteService; // Added FeedVoteService
    private final FeedServiceUtils feedServiceUtils;
    
    /**
     * 피드 상세 조회
     * 
     * @param feedId 피드 ID
     * @param userDetails 사용자 정보 (선택적)
     * @return 피드 상세 정보
     * @throws FeedNotFoundException 피드를 찾을 수 없는 경우
     */
    public FeedDetailResponseDto getFeedDetail(Long feedId, UserDetails userDetails) {
        log.info("피드 상세 조회 요청 - feedId: {}, userDetails: {}", feedId, userDetails != null ? "있음" : "없음");
        
        // 피드 조회 (삭제되지 않은 피드만)
        Feed feed = feedRepository.findDetailById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));
        
        // 피드 조회 가능 여부 확인
        if (!feed.isViewable()) {
            log.warn("삭제된 피드 조회 시도 - feedId: {}", feedId);
            throw new FeedNotFoundException(feedId, "삭제된 피드입니다.");
        }
        
        // DTO 변환
        FeedDetailResponseDto dto = feedMapper.toFeedDetailResponseDto(feed);
        
        // 사용자별 좋아요 상태 설정
        boolean isLiked = userDetails != null ? 
                feedLikeService.isLikedByUser(feedId, feedServiceUtils.getUserIdFromUserDetails(userDetails)) : false;
        boolean isVoted = userDetails != null ? 
                feedVoteService.hasVoted(feedId, feedServiceUtils.getUserIdFromUserDetails(userDetails)) : false;
        dto = dto.toBuilder().isLiked(isLiked).isVoted(isVoted).build();
        
        log.info("사용자별 상호작용 상태 설정 - feedId: {}, isLiked: {}, isVoted: {}", feedId, isLiked, isVoted);
        
        log.info("피드 상세 조회 완료 - feedId: {}, 제목: {}, isLiked: {}", feedId, feed.getTitle(), dto.getIsLiked());
        
        return dto;
    }
}
