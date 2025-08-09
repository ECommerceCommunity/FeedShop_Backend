package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.response.LikeToggleResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedLike;
import com.cMall.feedShop.feed.domain.repository.FeedLikeRepository;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
