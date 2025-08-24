package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.MyFeedListResponseDto;
import com.cMall.feedShop.feed.application.dto.response.MyFeedCountResponse;
import com.cMall.feedShop.feed.application.exception.FeedAccessDeniedException;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.enums.FeedType;
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

import java.util.List;
import java.util.Optional;

/**
 * 마이피드 조회 서비스
 * 로그인한 사용자의 피드만 조회하는 비즈니스 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyFeedReadService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final FeedMapper feedMapper;
    private final FeedLikeService feedLikeService;
    private final FeedServiceUtils feedServiceUtils;

    /**
     * 사용자의 마이피드 목록 조회 (페이징)
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 및 정렬 정보
     * @param userDetails 사용자 정보 (선택적)
     * @return 마이피드 목록 페이지
     */
    public Page<MyFeedListResponseDto> getMyFeeds(Long userId, Pageable pageable, UserDetails userDetails) {
        log.info("마이피드 목록 조회 - userId: {}, page: {}, size: {}, userDetails: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize(), userDetails != null ? "있음" : "없음");

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedAccessDeniedException(userId, "존재하지 않는 사용자입니다."));

        // 사용자의 피드 조회
        Page<Feed> feedPage = feedRepository.findByUserId(userId, pageable);

        // Feed 엔티티를 DTO로 변환
        Page<MyFeedListResponseDto> responsePage = feedPage.map(feedMapper::toMyFeedListResponseDto);

        // 사용자별 좋아요 상태 설정
        responsePage = responsePage.map(dto -> {
            boolean isLiked = userDetails != null ? 
                    feedLikeService.isLikedByUser(dto.getFeedId(), feedServiceUtils.getUserIdFromUserDetails(userDetails)) : false;
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

        log.info("마이피드 목록 조회 완료 - userId: {}, 총 {}개, 현재 페이지 {}개",
                userId, responsePage.getTotalElements(), responsePage.getNumberOfElements());

        return responsePage;
    }

    /**
     * 사용자의 특정 타입 마이피드 목록 조회 (페이징)
     *
     * @param userId 사용자 ID
     * @param feedType 피드 타입
     * @param pageable 페이징 및 정렬 정보
     * @param userDetails 사용자 정보 (선택적)
     * @return 마이피드 목록 페이지
     */
    public Page<MyFeedListResponseDto> getMyFeedsByType(Long userId, FeedType feedType, Pageable pageable, UserDetails userDetails) {
        log.info("마이피드 타입별 조회 - userId: {}, feedType: {}, page: {}, size: {}, userDetails: {}",
                userId, feedType, pageable.getPageNumber(), pageable.getPageSize(), userDetails != null ? "있음" : "없음");

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedAccessDeniedException(userId, "존재하지 않는 사용자입니다."));

        // 사용자의 특정 타입 피드 조회
        Page<Feed> feedPage = feedRepository.findByUserIdAndFeedType(userId, feedType.name(), pageable);

        // Feed 엔티티를 DTO로 변환
        Page<MyFeedListResponseDto> responsePage = feedPage.map(feedMapper::toMyFeedListResponseDto);

        // 사용자별 좋아요 상태 설정
        responsePage = responsePage.map(dto -> {
            boolean isLiked = userDetails != null ? 
                    feedLikeService.isLikedByUser(dto.getFeedId(), feedServiceUtils.getUserIdFromUserDetails(userDetails)) : false;
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

        log.info("마이피드 타입별 조회 완료 - userId: {}, feedType: {}, 총 {}개, 현재 페이지 {}개",
                userId, feedType, responsePage.getTotalElements(), responsePage.getNumberOfElements());

        return responsePage;
    }

    /**
     * 사용자의 마이피드 개수 조회
     *
     * @param userId 사용자 ID
     * @return 마이피드 개수
     */
    public long getMyFeedCount(Long userId) {
        log.info("마이피드 개수 조회 - userId: {}", userId);

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedAccessDeniedException(userId, "존재하지 않는 사용자입니다."));

        long count = feedRepository.countByUserId(userId);
        log.info("마이피드 개수 조회 완료 - userId: {}, 개수: {}", userId, count);

        return count;
    }

    /**
     * 사용자의 특정 타입 마이피드 개수 조회
     *
     * @param userId 사용자 ID
     * @param feedType 피드 타입
     * @return 마이피드 개수
     */
    public long getMyFeedCountByType(Long userId, FeedType feedType) {
        log.info("마이피드 타입별 개수 조회 - userId: {}, feedType: {}", userId, feedType);

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedAccessDeniedException(userId, "존재하지 않는 사용자입니다."));

        long count = feedRepository.countByUserIdAndFeedType(userId, feedType.name());
        log.info("마이피드 타입별 개수 조회 완료 - userId: {}, feedType: {}, 개수: {}", userId, feedType, count);

        return count;
    }

    /**
     * 사용자의 모든 피드 타입별 개수 조회
     *
     * @param userId 사용자 ID
     * @return 모든 피드 타입별 개수
     */
    public MyFeedCountResponse getMyFeedCounts(Long userId) {
        log.info("마이피드 전체 개수 조회 - userId: {}", userId);

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedAccessDeniedException(userId, "존재하지 않는 사용자입니다."));

        // 전체 개수
        long totalCount = feedRepository.countByUserId(userId);
        
        // 타입별 개수
        long dailyCount = feedRepository.countByUserIdAndFeedType(userId, FeedType.DAILY.name());
        long eventCount = feedRepository.countByUserIdAndFeedType(userId, FeedType.EVENT.name());
        long rankingCount = feedRepository.countByUserIdAndFeedType(userId, FeedType.RANKING.name());

        MyFeedCountResponse response = MyFeedCountResponse.builder()
                .totalCount(totalCount)
                .dailyCount(dailyCount)
                .eventCount(eventCount)
                .rankingCount(rankingCount)
                .build();

        log.info("마이피드 전체 개수 조회 완료 - userId: {}, total: {}, daily: {}, event: {}, ranking: {}", 
                userId, totalCount, dailyCount, eventCount, rankingCount);

        return response;
    }
    
} 