package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.response.LikeToggleResponseDto;
import com.cMall.feedShop.feed.application.dto.response.LikeUserResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedLike;
import com.cMall.feedShop.feed.domain.repository.FeedLikeRepository;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.cMall.feedShop.feed.application.dto.response.MyLikedFeedsResponseDto;
import com.cMall.feedShop.feed.application.dto.response.MyLikedFeedItemDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedLikeService {

    private final FeedLikeRepository feedLikeRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    /**
     * 좋아요 토글
     * - 없으면 생성(liked=true), 있으면 삭제(liked=false)
     * - Feed.likeCount 증감
     */
    @Transactional
    public LikeToggleResponseDto toggleLike(Long feedId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));
        if (feed.isDeleted()) {
            throw new FeedNotFoundException(feedId);
        }

        boolean exists = feedLikeRepository.existsByFeed_IdAndUser_Id(feedId, user.getId());
        boolean liked;
        if (exists) {
            // 취소
            feedLikeRepository.deleteByFeed_IdAndUser_Id(feedId, user.getId());
            feed.decrementLikeCount();
            liked = false;
        } else {
            // 추가
            feedLikeRepository.save(FeedLike.builder()
                    .feed(feed)
                    .user(user)
                    .build());
            feed.incrementLikeCount();
            liked = true;
        }

        int likeCount = feed.getLikeCount() != null ? feed.getLikeCount() : 0;
        return LikeToggleResponseDto.builder()
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }

    /**
     * 좋아요 사용자 목록 조회
     * - 피드 존재/삭제 여부 검증
     * - 좋아요 누른 시간 기준 내림차순 정렬
     * - 페이징 지원
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<LikeUserResponseDto> getLikedUsers(Long feedId, int page, int size) {
        log.info("좋아요 사용자 목록 조회 요청 - feedId: {}, page: {}, size: {}", feedId, page, size);
        
        // 피드 존재 및 삭제 여부 검증
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));
        if (feed.isDeleted()) {
            log.warn("삭제된 피드의 좋아요 사용자 목록 조회 시도 - feedId: {}", feedId);
            throw new FeedNotFoundException(feedId);
        }
        
        // 페이징 및 정렬 설정
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 좋아요 사용자 목록 조회 (User 정보 포함)
        Page<FeedLike> feedLikes = feedLikeRepository.findByFeedIdWithUser(feedId, pageRequest);
        
        // DTO 변환
        List<LikeUserResponseDto> likeUsers = feedLikes.getContent().stream()
                .map(this::toLikeUserResponseDto)
                .collect(Collectors.toList());
        
        log.info("좋아요 사용자 목록 조회 완료 - feedId: {}, 총 {}명", feedId, feedLikes.getTotalElements());
        
        // PaginatedResponse 구성
        return PaginatedResponse.<LikeUserResponseDto>builder()
                .content(likeUsers)
                .page(page)
                .size(size)
                .totalElements(feedLikes.getTotalElements())
                .totalPages(feedLikes.getTotalPages())
                .hasNext(feedLikes.hasNext())
                .hasPrevious(feedLikes.hasPrevious())
                .build();
    }

    /**
     * 사용자가 좋아요한 피드 ID 목록 조회
     * - 로그인한 사용자가 좋아요를 누른 모든 피드 ID 반환
     * - 프론트엔드에서 좋아요 상태 복원용
     */
    @Transactional(readOnly = true)
    public List<Long> getMyLikedFeedIds(Long userId) {
        User user = userRepository.findById(userId)
                .orElse(null);
        
        if (user == null) {
            log.warn("사용자를 찾을 수 없어서 좋아요 피드 목록을 조회할 수 없습니다. - userId: {}", userId);
            return List.of();
        }
        
        log.info("사용자별 좋아요 피드 목록 조회 - userId: {}", user.getId());
        
        List<Long> likedFeedIds = feedLikeRepository.findFeedIdsByUserId(user.getId());
        
        log.info("사용자별 좋아요 피드 목록 조회 완료 - userId: {}, 좋아요한 피드 수: {}", user.getId(), likedFeedIds.size());
        
        return likedFeedIds;
    }
    
    /**
     * FeedLike 엔티티를 LikeUserResponseDto로 변환
     */
    private LikeUserResponseDto toLikeUserResponseDto(FeedLike feedLike) {
        User user = feedLike.getUser();
        return LikeUserResponseDto.builder()
                .userId(user.getId())
                .nickname(getUserNickname(user))
                .profileImageUrl(getUserProfileImageUrl(user))
                .level(getUserLevel(user))
                .likedAt(feedLike.getCreatedAt())
                .build();
    }
    
    /**
     * 사용자 닉네임 조회
     */
    private String getUserNickname(User user) {
        if (user.getUserProfile() != null) {
            return user.getUserProfile().getNickname();
        }
        return null;
    }
    
    /**
     * 사용자 프로필 이미지 URL 조회
     */
    private String getUserProfileImageUrl(User user) {
        // TODO: 추후 UserProfile에 profileImageUrl 필드 추가 시 구현
        return null;
    }
    
    /**
     * 사용자 레벨 조회
     */
    private Integer getUserLevel(User user) {
        // TODO: 추후 UserProfile에 level 필드 추가 시 구현
        return null;
    }
    
    /**
     * 사용자별 좋아요 상태 확인
     * - 공통으로 사용되는 좋아요 상태 확인 로직
     * - 다른 서비스에서 호출하여 사용
     */
    public boolean isLikedByUser(Long feedId, Long userId) {
        try {
            return feedLikeRepository.existsByFeed_IdAndUser_Id(feedId, userId);
        } catch (Exception e) {
            log.warn("사용자별 좋아요 상태 확인 실패 - feedId: {}, userId: {}, error: {}", feedId, userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * 사용자가 좋아요한 피드 목록 조회 (페이징)
     * - 로그인한 사용자가 좋아요를 누른 피드들을 페이징으로 조회
     * - 좋아요를 누른 시간순으로 정렬 (최신순)
     * - 피드 상세 정보와 함께 좋아요 시간 제공
     */
    @Transactional(readOnly = true)
    public MyLikedFeedsResponseDto getMyLikedFeeds(Long userId, int page, int size) {
        log.info("사용자별 좋아요 피드 목록 조회 요청 - userId: {}, page: {}, size: {}", userId, page, size);
        
        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
        
        // 페이징 및 정렬 설정
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 사용자가 좋아요한 피드 목록 조회
        Page<FeedLike> feedLikes = feedLikeRepository.findByUserIdWithFeed(userId, pageRequest);
        
        // DTO 변환
        List<MyLikedFeedItemDto> likedFeeds = feedLikes.getContent().stream()
                .map(this::toMyLikedFeedItemDto)
                .collect(Collectors.toList());
        
        log.info("사용자별 좋아요 피드 목록 조회 완료 - userId: {}, 총 {}개", userId, feedLikes.getTotalElements());
        
        // MyLikedFeedsResponseDto 구성
        return MyLikedFeedsResponseDto.builder()
                .content(likedFeeds)
                .page(page)
                .size(size)
                .totalElements(feedLikes.getTotalElements())
                .totalPages(feedLikes.getTotalPages())
                .first(feedLikes.isFirst())
                .last(feedLikes.isLast())
                .hasNext(feedLikes.hasNext())
                .hasPrevious(feedLikes.hasPrevious())
                .build();
    }
    
    /**
     * FeedLike 엔티티를 MyLikedFeedItemDto로 변환
     */
    private MyLikedFeedItemDto toMyLikedFeedItemDto(FeedLike feedLike) {
        Feed feed = feedLike.getFeed();
        User author = feed.getUser();
        
        return MyLikedFeedItemDto.builder()
                .feedId(feed.getId())
                .title(feed.getTitle())
                .content(feed.getContent())
                .feedType(feed.getFeedType().name())
                .imageUrl(getFirstImageUrl(feed))
                .likedAt(feedLike.getCreatedAt())
                .likeCount(feed.getLikeCount() != null ? feed.getLikeCount() : 0)
                .commentCount(feed.getCommentCount() != null ? feed.getCommentCount() : 0)
                .authorNickname(getUserNickname(author))
                .authorProfileImage(getUserProfileImageUrl(author))
                .build();
    }
    
    /**
     * 피드의 첫 번째 이미지 URL 조회
     */
    private String getFirstImageUrl(Feed feed) {
        // TODO: Feed 엔티티에 images 관계가 있다면 첫 번째 이미지 URL 반환
        // 현재는 null 반환
        return null;
    }
}
