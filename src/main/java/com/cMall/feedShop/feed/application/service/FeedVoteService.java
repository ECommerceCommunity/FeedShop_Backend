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
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.application.service.EventStatusService;
import com.cMall.feedShop.common.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedVoteService {

    private final FeedVoteRepository feedVoteRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final UserLevelService userLevelService;
    private final PointService pointService;
    private final EventStatusService eventStatusService;

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

        // 4. 이벤트가 진행중인지 확인
        Event event = feed.getEvent();
        EventStatus eventStatus = eventStatusService.calculateEventStatus(event, TimeUtil.nowDate());
        if (eventStatus != EventStatus.ONGOING) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, 
                String.format("이벤트가 종료되어 투표할 수 없습니다. 현재 상태: %s", eventStatus));
        }

        // 4. 같은 이벤트에서 이미 다른 피드에 투표했는지 확인
        if (feedVoteRepository.existsByEventIdAndUserId(feed.getEvent().getId(), userId)) {
            log.info("이미 해당 이벤트에 투표함 - 이벤트ID: {}, 사용자ID: {}", feed.getEvent().getId(), userId);
            return FeedVoteResponseDto.success(false, feed.getParticipantVoteCount());
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

        return FeedVoteResponseDto.success(true, feed.getParticipantVoteCount());
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

    /**
     * 투표 수 동기화 (Feed 엔티티의 participantVoteCount와 실제 투표 수 동기화)
     */
    @Transactional
    public void syncVoteCount(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));
        
        long actualVoteCount = feedVoteRepository.countByFeed_Id(feedId);
        long currentCount = feed.getParticipantVoteCount();
        
        if (actualVoteCount != currentCount) {
            log.info("투표 수 동기화 - feedId: {}, 현재: {}, 실제: {}", feedId, currentCount, actualVoteCount);
            
            // 차이값만큼 조정
            long difference = actualVoteCount - currentCount;
            if (difference > 0) {
                for (int i = 0; i < difference; i++) {
                    feed.incrementVoteCount();
                }
            } else {
                for (int i = 0; i < Math.abs(difference); i++) {
                    feed.decrementVoteCount();
                }
            }
        }
    }

    /**
     * 모든 피드의 투표 수 동기화
     */
    @Transactional
    public void syncAllVoteCounts() {
        // Pageable을 사용하여 모든 피드를 조회
        Pageable pageable = PageRequest.of(0, 1000); // 한 번에 1000개씩 처리
        Page<Feed> feedPage = feedRepository.findAll(pageable);
        List<Feed> feeds = feedPage.getContent();
        
        int syncedCount = 0;
        
        for (Feed feed : feeds) {
            try {
                syncVoteCount(feed.getId());
                syncedCount++;
            } catch (Exception e) {
                log.error("피드 투표 수 동기화 실패 - feedId: {}", feed.getId(), e);
            }
        }
        
        log.info("전체 피드 투표 수 동기화 완료 - {}개 피드 처리됨", syncedCount);
    }

    /**
     * 여러 피드에 대한 사용자의 투표 상태 일괄 조회
     * 성능 개선을 위한 일괄 조회 메서드
     * 
     * @param feedIds 피드 ID 목록
     * @param userId 사용자 ID
     * @return 투표한 피드 ID 집합
     */
    public Set<Long> getVotedFeedIdsByFeedIdsAndUserId(List<Long> feedIds, Long userId) {
        if (userId == null || feedIds == null || feedIds.isEmpty()) {
            return new HashSet<>();
        }
        
        try {
            List<Long> votedFeedIds = feedVoteRepository.findVotedFeedIdsByFeedIdsAndUserId(feedIds, userId);
            return new HashSet<>(votedFeedIds);
        } catch (Exception e) {
            log.error("일괄 투표 상태 조회 중 오류 발생 - userId: {}, feedIds: {}", userId, feedIds, e);
            // 오류 발생 시 빈 집합 반환 (성능 개선 실패 시 기존 방식으로 fallback)
            return new HashSet<>();
        }
    }
}
