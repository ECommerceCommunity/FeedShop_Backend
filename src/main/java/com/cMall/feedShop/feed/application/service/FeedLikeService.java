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
    public LikeToggleResponseDto toggleLike(Long feedId, UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 없습니다.");
        }
        String loginId = userDetails.getUsername();
        User user = userRepository.findByLoginId(loginId)
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
}
