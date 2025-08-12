package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.domain.repository.FeedLikeRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
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
        boolean isLiked = feedLikeService.isLikedByUser(feedId, userDetails);
        dto = FeedDetailResponseDto.builder()
                .feedId(dto.getFeedId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .feedType(dto.getFeedType())
                .instagramId(dto.getInstagramId())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .likeCount(dto.getLikeCount())
                .commentCount(dto.getCommentCount())
                .participantVoteCount(dto.getParticipantVoteCount())
                .userId(dto.getUserId())
                .userNickname(dto.getUserNickname())
                .userProfileImg(dto.getUserProfileImg())
                .userLevel(dto.getUserLevel())
                .userGender(dto.getUserGender())
                .userHeight(dto.getUserHeight())
                .orderItemId(dto.getOrderItemId())
                .productName(dto.getProductName())
                .productSize(dto.getProductSize())
                .productImageUrl(dto.getProductImageUrl())
                .productId(dto.getProductId())
                .eventId(dto.getEventId())
                .eventTitle(dto.getEventTitle())
                .eventDescription(dto.getEventDescription())
                .eventStartDate(dto.getEventStartDate())
                .eventEndDate(dto.getEventEndDate())
                .hashtags(dto.getHashtags())
                .images(dto.getImages())
                .comments(dto.getComments())
                .isLiked(isLiked)
                .isVoted(dto.getIsVoted())
                .canVote(dto.getCanVote())
                .build();
        
        log.info("사용자별 좋아요 상태 설정 - feedId: {}, isLiked: {}", feedId, isLiked);
        
        log.info("피드 상세 조회 완료 - feedId: {}, 제목: {}, isLiked: {}", feedId, feed.getTitle(), dto.getIsLiked());
        
        return dto;
    }
}
