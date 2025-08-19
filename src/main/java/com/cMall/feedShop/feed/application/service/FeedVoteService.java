package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.request.FeedVoteRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedVoteResponseDto;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedVote;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.domain.repository.FeedVoteRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedVoteService {

    private final FeedVoteRepository feedVoteRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    /**
     * 피드 투표
     */
    @Transactional
    public FeedVoteResponseDto voteFeed(Long feedId, Long userId) {
        // 피드 존재 확인
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이벤트 피드인지 확인
        if (feed.getFeedType() != com.cMall.feedShop.feed.domain.FeedType.EVENT) {
            throw new BusinessException(ErrorCode.FEED_ACCESS_DENIED);
        }

        // 이미 투표했는지 확인
        if (feedVoteRepository.existsByFeedIdAndUserId(feedId, userId)) {
            log.info("이미 투표한 피드 - 피드ID: {}, 사용자ID: {}", feedId, userId);
            int voteCount = (int) feedVoteRepository.countByFeedId(feedId);
            return FeedVoteResponseDto.success(false, voteCount);
        }

        // 투표 생성
                       FeedVote vote = FeedVote.builder()
                       .event(feed.getEvent())
                       .feed(feed)
                       .voter(user)
                       .build();

        feedVoteRepository.save(vote);
        
        // 투표 개수 조회
        int voteCount = (int) feedVoteRepository.countByFeedId(feedId);
        
        log.info("피드 투표 완료 - 피드ID: {}, 사용자ID: {}, 투표개수: {}", feedId, userId, voteCount);

        return FeedVoteResponseDto.success(true, voteCount);
    }

    /**
     * 피드의 투표 개수 조회
     */
    public int getVoteCount(Long feedId) {
        try {
            // 피드 존재 확인
            if (!feedRepository.findById(feedId).isPresent()) {
                throw new BusinessException(ErrorCode.FEED_NOT_FOUND);
            }

            return (int) feedVoteRepository.countByFeedId(feedId);
        } catch (Exception e) {
            log.error("투표 개수 조회 중 오류 발생 - 피드ID: {}", feedId, e);
            // 테이블이 존재하지 않는 경우 0 반환
            return 0;
        }
    }

    /**
     * 사용자가 특정 피드에 투표했는지 확인
     */
    public boolean hasVoted(Long feedId, Long userId) {
        try {
            // 피드 존재 확인
            if (!feedRepository.findById(feedId).isPresent()) {
                throw new BusinessException(ErrorCode.FEED_NOT_FOUND);
            }

            // 사용자 존재 확인
            if (!userRepository.findById(userId).isPresent()) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            return feedVoteRepository.existsByFeedIdAndUserId(feedId, userId);
        } catch (Exception e) {
            log.error("투표 여부 확인 중 오류 발생 - 피드ID: {}, 사용자ID: {}", feedId, userId, e);
            // 테이블이 존재하지 않는 경우 false 반환
            return false;
        }
    }
}
