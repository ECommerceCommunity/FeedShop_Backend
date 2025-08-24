package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.response.FeedVoteResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.entity.FeedVote;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.domain.repository.FeedVoteRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.application.service.UserLevelService;
import com.cMall.feedShop.user.domain.model.ActivityType;
import com.cMall.feedShop.user.application.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedVoteService {

    private final FeedVoteRepository feedVoteRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final UserLevelService userLevelService;
    private final PointService pointService;

    /**
     * 피드 투표
     * - 이벤트 참여 피드에만 투표 가능
     * - 투표 시 자동으로 리워드 지급 (포인트 100점 + 뱃지 점수 2점)
     */
    @Transactional
    public FeedVoteResponseDto voteFeed(Long feedId, Long userId) {
        log.info("피드 투표 요청 - feedId: {}, userId: {}", feedId, userId);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 2. 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));

        if (feed.isDeleted()) {
            throw new FeedNotFoundException(feedId);
        }

        // 3. 이벤트 참여 피드인지 확인
        if (!feed.isEventFeed()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이벤트 참여 피드에만 투표할 수 있습니다.");
        }

        // 4. 이미 투표했는지 확인
        if (feedVoteRepository.existsByFeed_IdAndVoter_Id(feedId, userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "이미 투표한 피드입니다.");
        }

        // 5. 투표 생성
        FeedVote vote = FeedVote.builder()
                .feed(feed)
                .voter(user)
                .event(feed.getEvent())
                .build();

        FeedVote savedVote = feedVoteRepository.save(vote);

        // 6. 피드 투표 수 증가
        feed.incrementVoteCount();

        log.info("피드 투표 완료 - feedId: {}, userId: {}, voteId: {}", feedId, userId, savedVote.getId());

        // 7. 투표 리워드 지급 (포인트 100점 + 뱃지 점수 2점)
        try {
            // 포인트 100점 지급
            pointService.earnPoints(user, 100, "피드 투표 리워드", feedId);
            
            // 뱃지 점수 2점 추가 (VOTE_PARTICIPATION 활동 기록)
            userLevelService.recordActivity(userId, ActivityType.VOTE_PARTICIPATION, 
                "피드 투표 참여", feedId, "FEED");
            
            log.info("피드 투표 리워드 지급 완료 - userId: {}, feedId: {}", userId, feedId);
        } catch (Exception e) {
            log.error("피드 투표 리워드 지급 실패 - userId: {}, feedId: {}", userId, feedId, e);
            // 리워드 지급 실패가 투표에 영향을 주지 않도록 예외를 던지지 않음
        }

        return FeedVoteResponseDto.builder()
                .voteId(savedVote.getId())
                .feedId(feedId)
                .userId(userId)
                .eventId(feed.getEvent().getId())
                .votedAt(savedVote.getCreatedAt())
                .build();
    }

    /**
     * 피드 투표 취소
     */
    @Transactional
    public void cancelVote(Long feedId, Long userId) {
        log.info("피드 투표 취소 요청 - feedId: {}, userId: {}", feedId, userId);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 2. 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));

        if (feed.isDeleted()) {
            throw new FeedNotFoundException(feedId);
        }

        // 3. 투표 존재 확인
        FeedVote vote = feedVoteRepository.findByFeed_IdAndVoter_Id(feedId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "투표 내역을 찾을 수 없습니다."));

        // 4. 투표 삭제
        feedVoteRepository.delete(vote);

        // 5. 피드 투표 수 감소
        feed.decrementVoteCount();

        log.info("피드 투표 취소 완료 - feedId: {}, userId: {}", feedId, userId);
    }

    /**
     * 사용자가 특정 피드에 투표했는지 확인
     */
    public boolean hasVoted(Long feedId, Long userId) {
        if (userId == null) {
            return false;
        }
        return feedVoteRepository.existsByFeed_IdAndVoter_Id(feedId, userId);
    }

    /**
     * 특정 피드의 투표 개수 조회
     */
    public long getVoteCount(Long feedId) {
        return feedVoteRepository.countByFeed_Id(feedId);
    }

    /**
     * 특정 이벤트의 투표 개수 조회
     */
    public long getEventVoteCount(Long eventId) {
        return feedVoteRepository.countByEvent_Id(eventId);
    }
}
