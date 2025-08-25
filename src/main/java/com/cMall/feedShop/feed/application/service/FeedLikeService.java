package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.response.LikeToggleResponseDto;
import com.cMall.feedShop.feed.application.dto.response.LikeUserResponseDto;
import com.cMall.feedShop.feed.application.dto.response.MyLikedFeedsResponseDto;
import com.cMall.feedShop.feed.application.dto.response.MyLikedFeedItemDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.entity.FeedLike;
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
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedLikeService {

    private final FeedLikeRepository feedLikeRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final FeedRewardEventHandler feedRewardEventHandler;

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
        
        // 좋아요 추가 시 리워드 이벤트 생성 (마일스톤 체크)
        if (liked) {
            try {
                feedRewardEventHandler.createFeedLikesMilestoneEvent(user, feed, likeCount);
            } catch (Exception e) {
                log.warn("좋아요 마일스톤 리워드 이벤트 생성 중 오류 발생 - userId: {}, feedId: {}, likeCount: {}", 
                        userId, feedId, likeCount, e);
                // 리워드 이벤트 생성 실패가 좋아요 처리에 영향을 주지 않도록 예외를 던지지 않음
            }
        }
        
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
        List<FeedLike> feedLikes = feedLikeRepository.findByFeed_Id(feedId);
        
        // 페이징 처리 (수동으로 구현)
        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getPageSize(), feedLikes.size());
        List<FeedLike> pagedFeedLikes = feedLikes.subList(start, end);
        
        // DTO 변환
        List<LikeUserResponseDto> likeUsers = pagedFeedLikes.stream()
                .map(this::toLikeUserResponseDto)
                .collect(Collectors.toList());
        
        log.info("좋아요 사용자 목록 조회 완료 - feedId: {}, 총 {}명", feedId, feedLikes.size());
        
        // PaginatedResponse 구성
        return PaginatedResponse.<LikeUserResponseDto>builder()
                .content(likeUsers)
                .page(page)
                .size(size)
                .totalElements((long) feedLikes.size())
                .totalPages((int) Math.ceil((double) feedLikes.size() / size))
                .hasNext(end < feedLikes.size())
                .hasPrevious(page > 0)
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
        
        // 임시로 빈 리스트 반환 (실제 구현에서는 Repository에서 조회)
        List<Long> likedFeedIds = List.of();
        
        log.info("사용자별 좋아요 피드 목록 조회 완료 - userId: {}, 좋아요한 피드 수: {}", user.getId(), likedFeedIds.size());
        
        return likedFeedIds;
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
        Page<FeedLike> feedLikes = feedLikeRepository.findByUser_Id(userId, pageRequest);
        
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
     * 피드의 첫 번째 이미지 URL 조회
     */
    private String getFirstImageUrl(Feed feed) {
        // TODO: Feed 엔티티에 images 관계가 있다면 첫 번째 이미지 URL 반환
        // 현재는 null 반환
        return null;
    }
    
    /**
     * 사용자가 특정 피드에 좋아요했는지 확인
     */
    public boolean isLikedByUser(Long feedId, Long userId) {
        if (userId == null) {
            return false;
        }
        return feedLikeRepository.existsByFeed_IdAndUser_Id(feedId, userId);
    }

    /**
     * 여러 피드에 대한 사용자의 좋아요 상태 일괄 조회
     * 성능 개선을 위한 일괄 조회 메서드
     * 
     * @param feedIds 피드 ID 목록
     * @param userId 사용자 ID
     * @return 좋아요한 피드 ID 집합
     */
    public Set<Long> getLikedFeedIdsByFeedIdsAndUserId(List<Long> feedIds, Long userId) {
        if (userId == null || feedIds == null || feedIds.isEmpty()) {
            return new HashSet<>();
        }
        
        try {
            List<Long> likedFeedIds = feedLikeRepository.findLikedFeedIdsByFeedIdsAndUserId(feedIds, userId);
            return new HashSet<>(likedFeedIds);
        } catch (Exception e) {
            log.error("일괄 좋아요 상태 조회 중 오류 발생 - userId: {}, feedIds: {}", userId, feedIds, e);
            // 오류 발생 시 빈 집합 반환 (성능 개선 실패 시 기존 방식으로 fallback)
            return new HashSet<>();
        }
    }
}
