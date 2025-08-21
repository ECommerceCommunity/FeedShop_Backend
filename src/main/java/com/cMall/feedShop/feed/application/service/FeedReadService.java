package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.FeedListResponseDto;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    private final FeedLikeService feedLikeService;
    private final FeedServiceUtils feedServiceUtils;
    
    /**
     * 피드 목록 조회 (필터링, 페이징, 정렬)
     * 
     * @param feedType 피드 타입 (DAILY, EVENT, RANKING, null=전체)
     * @param pageable 페이징 및 정렬 정보
     * @param userDetails 사용자 정보 (선택적)
     * @return 피드 목록 페이지
     */
    public Page<FeedListResponseDto> getFeeds(FeedType feedType, Pageable pageable, UserDetails userDetails) {
        log.info("피드 목록 조회 - feedType: {}, page: {}, size: {}, userDetails: {}", 
                feedType, pageable.getPageNumber(), pageable.getPageSize(), userDetails != null ? "있음" : "없음");
        
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

        // 사용자별 좋아요 상태 설정
        responsePage = responsePage.map(dto -> {
            boolean isLiked = userDetails != null ? 
                    feedLikeService.isLikedByUser(dto.getFeedId(), feedServiceUtils.getUserIdFromUserDetails(userDetails)) : false;
            return FeedListResponseDto.builder()
                    .feedId(dto.getFeedId())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .feedType(dto.getFeedType())
                    .instagramId(dto.getInstagramId())
                    .createdAt(dto.getCreatedAt())
                    .likeCount(dto.getLikeCount())
                    .commentCount(dto.getCommentCount())
                    .participantVoteCount(dto.getParticipantVoteCount())
                    .userId(dto.getUserId())
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
                    .isVoted(dto.getIsVoted())
                    .build();
        });
        
        log.info("피드 목록 조회 완료 - 총 {}개, 현재 페이지 {}개", 
                responsePage.getTotalElements(), responsePage.getNumberOfElements());
        
        return responsePage;
    }
    
    /**
     * 피드 타입별 조회 (페이징)
     * 
     * @param feedType 피드 타입
     * @param pageable 페이징 및 정렬 정보
     * @param userDetails 사용자 정보 (선택적)
     * @return 피드 목록 페이지
     */
    public Page<FeedListResponseDto> getFeedsByType(FeedType feedType, Pageable pageable, UserDetails userDetails) {
        log.info("피드 타입별 조회 - feedType: {}, page: {}, size: {}, userDetails: {}", 
                feedType, pageable.getPageNumber(), pageable.getPageSize(), userDetails != null ? "있음" : "없음");
        
        Page<Feed> feedPage = feedRepository.findByFeedType(feedType.name(), pageable);
        Page<FeedListResponseDto> responsePage = feedPage.map(feedMapper::toFeedListResponseDto);
        
        // 사용자별 좋아요 상태 설정
        responsePage = responsePage.map(dto -> {
            boolean isLiked = userDetails != null ? 
                    feedLikeService.isLikedByUser(dto.getFeedId(), feedServiceUtils.getUserIdFromUserDetails(userDetails)) : false;
            return FeedListResponseDto.builder()
                    .feedId(dto.getFeedId())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .feedType(dto.getFeedType())
                    .instagramId(dto.getInstagramId())
                    .createdAt(dto.getCreatedAt())
                    .likeCount(dto.getLikeCount())
                    .commentCount(dto.getCommentCount())
                    .participantVoteCount(dto.getParticipantVoteCount())
                    .userId(dto.getUserId())
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
                    .isVoted(dto.getIsVoted())
                    .build();
        });
        
        log.info("피드 타입별 조회 완료 - feedType: {}, 총 {}개, 현재 페이지 {}개", 
                feedType, responsePage.getTotalElements(), responsePage.getNumberOfElements());
        
        return responsePage;
    }

    /**
     * 특정 사용자의 피드 목록 조회
     * 
     * @param userId 사용자 ID
     * @param pageable 페이징 및 정렬 정보
     * @param userDetails 로그인한 사용자 정보 (선택적)
     * @return 특정 사용자의 피드 목록 페이지
     */
    public Page<FeedListResponseDto> getUserFeeds(Long userId, Pageable pageable, UserDetails userDetails) {
        log.info("사용자 피드 목록 조회 - userId: {}, page: {}, size: {}, userDetails: {}", 
                userId, pageable.getPageNumber(), pageable.getPageSize(), userDetails != null ? "있음" : "없음");
        
        Page<Feed> feedPage = feedRepository.findByUserId(userId, pageable);
        Page<FeedListResponseDto> responsePage = feedPage.map(feedMapper::toFeedListResponseDto);
        
        // 사용자별 좋아요 상태 설정
        responsePage = responsePage.map(dto -> {
            boolean isLiked = userDetails != null ? 
                    feedLikeService.isLikedByUser(dto.getFeedId(), feedServiceUtils.getUserIdFromUserDetails(userDetails)) : false;
            return FeedListResponseDto.builder()
                    .feedId(dto.getFeedId())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .feedType(dto.getFeedType())
                    .instagramId(dto.getInstagramId())
                    .createdAt(dto.getCreatedAt())
                    .likeCount(dto.getLikeCount())
                    .commentCount(dto.getCommentCount())
                    .participantVoteCount(dto.getParticipantVoteCount())
                    .userId(dto.getUserId())
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
                    .isVoted(dto.getIsVoted())
                    .build();
        });
        
        log.info("사용자 피드 목록 조회 완료 - userId: {}, 총 {}개, 현재 페이지 {}개", 
                userId, responsePage.getTotalElements(), responsePage.getNumberOfElements());
        
        return responsePage;
    }

    /**
     * 특정 사용자의 특정 타입 피드 목록 조회
     * 
     * @param userId 사용자 ID
     * @param feedType 피드 타입
     * @param pageable 페이징 및 정렬 정보
     * @param userDetails 로그인한 사용자 정보 (선택적)
     * @return 특정 사용자의 특정 타입 피드 목록 페이지
     */
    public Page<FeedListResponseDto> getUserFeedsByType(Long userId, FeedType feedType, Pageable pageable, UserDetails userDetails) {
        log.info("사용자 피드 타입별 조회 - userId: {}, feedType: {}, page: {}, size: {}, userDetails: {}", 
                userId, feedType, pageable.getPageNumber(), pageable.getPageSize(), userDetails != null ? "있음" : "없음");
        
        Page<Feed> feedPage = feedRepository.findByUserIdAndFeedType(userId, feedType.name(), pageable);
        Page<FeedListResponseDto> responsePage = feedPage.map(feedMapper::toFeedListResponseDto);
        
        // 사용자별 좋아요 상태 설정
        responsePage = responsePage.map(dto -> {
            boolean isLiked = userDetails != null ? 
                    feedLikeService.isLikedByUser(dto.getFeedId(), feedServiceUtils.getUserIdFromUserDetails(userDetails)) : false;
            return FeedListResponseDto.builder()
                    .feedId(dto.getFeedId())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .feedType(dto.getFeedType())
                    .instagramId(dto.getInstagramId())
                    .createdAt(dto.getCreatedAt())
                    .likeCount(dto.getLikeCount())
                    .commentCount(dto.getCommentCount())
                    .participantVoteCount(dto.getParticipantVoteCount())
                    .userId(dto.getUserId())
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
                    .isVoted(dto.getIsVoted())
                    .build();
        });
        
        log.info("사용자 피드 타입별 조회 완료 - userId: {}, feedType: {}, 총 {}개, 현재 페이지 {}개", 
                userId, feedType, responsePage.getTotalElements(), responsePage.getNumberOfElements());
        
        return responsePage;
    }
    
} 