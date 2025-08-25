package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.MyFeedListResponseDto;
import com.cMall.feedShop.feed.application.dto.response.MyFeedCountResponse;
import com.cMall.feedShop.feed.application.exception.FeedAccessDeniedException;
import com.cMall.feedShop.feed.application.service.FeedLikeService;
import com.cMall.feedShop.feed.application.service.FeedVoteService;
import com.cMall.feedShop.feed.application.service.FeedServiceUtils;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.application.service.FeedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 내 피드 조회 서비스
 * 현재 로그인한 사용자의 피드 목록 조회 등의 비즈니스 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyFeedReadService {
    
    private final FeedRepository feedRepository;
    private final FeedMapper feedMapper;
    private final FeedLikeService feedLikeService;
    private final FeedVoteService feedVoteService;
    private final FeedServiceUtils feedServiceUtils;
    
    /**
     * 내 피드 목록 조회 (페이징, 정렬) - 성능 개선 버전
     * 
     * @param userDetails 현재 로그인한 사용자 정보
     * @param pageable 페이징 및 정렬 정보
     * @return 내 피드 목록 페이지
     */
    public Page<MyFeedListResponseDto> getMyFeeds(UserDetails userDetails, Pageable pageable) {
        if (userDetails == null) {
            throw new IllegalArgumentException("사용자 정보가 필요합니다.");
        }
        
        Long userId = feedServiceUtils.getUserIdFromUserDetails(userDetails);
        log.info("내 피드 목록 조회 - userId: {}, page: {}, size: {}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Feed> feedPage = feedRepository.findByUserId(userId, pageable);
        
        // Feed 엔티티를 DTO로 변환
        Page<MyFeedListResponseDto> responsePage = feedPage.map(feedMapper::toMyFeedListResponseDto);

        // 사용자별 좋아요/투표 상태 일괄 조회 (성능 개선)
        final Set<Long> likedFeedIds = new HashSet<>();
        final Set<Long> votedFeedIds = new HashSet<>();
        
        List<Long> feedIds = responsePage.getContent().stream()
                .map(MyFeedListResponseDto::getFeedId)
                .collect(Collectors.toList());
        
        // 일괄 조회로 성능 향상 (N+1 문제 해결)
        likedFeedIds.addAll(feedLikeService.getLikedFeedIdsByFeedIdsAndUserId(feedIds, userId));
        votedFeedIds.addAll(feedVoteService.getVotedFeedIdsByFeedIdsAndUserId(feedIds, userId));
        
        log.info("일괄 상태 조회 완료 - userId: {}, 피드 수: {}, 좋아요: {}개, 투표: {}개", 
                userId, feedIds.size(), likedFeedIds.size(), votedFeedIds.size());

        // 상태 설정 (Set.contains() 사용으로 O(1) 성능)
        responsePage = responsePage.map(dto -> {
            boolean isLiked = likedFeedIds.contains(dto.getFeedId());
            boolean isVoted = votedFeedIds.contains(dto.getFeedId());
            return MyFeedListResponseDto.builder()
                    .feedId(dto.getFeedId())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .feedType(dto.getFeedType())
                    .instagramId(dto.getInstagramId())
                    .createdAt(dto.getCreatedAt())
                    .likeCount(dto.getLikeCount())
                    .commentCount(dto.getCommentCount())
                    .participantVoteCount(dto.getParticipantVoteCount())
                    .userNickname(dto.getUserNickname())
                    .userProfileImg(dto.getUserProfileImg())
                    .userLevel(dto.getUserLevel())
                    .orderItemId(dto.getOrderItemId())
                    .productName(dto.getProductName())
                    .productSize(dto.getProductSize())
                    .eventId(dto.getEventId())
                    .eventTitle(dto.getEventTitle())
                    .hashtags(dto.getHashtags())
                    .imageUrls(dto.getImageUrls())
                    .isLiked(isLiked)
                    .isVoted(isVoted)
                    .build();
        });
        
        log.info("내 피드 목록 조회 완료 - userId: {}, 총 {}개, 현재 페이지 {}개", 
                userId, responsePage.getTotalElements(), responsePage.getNumberOfElements());
        
        return responsePage;
    }
} 