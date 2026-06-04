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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    // [Phase 2-B] REQUIRES_NEW 분리 서비스 — flush() Hibernate Session 오염 방지
    private final FeedVotePersistenceService feedVotePersistenceService;
    // [Phase 2-B] Redis INCR — 투표 수 원자적 연산 (데드락 없이 정합성 보장)
    private final StringRedisTemplate redisTemplate;

    private static final String VOTE_COUNT_KEY = "vote:count:";

    /**
     * 피드 투표
     * - 이벤트 참여 피드에만 투표 가능
     * - 투표 시 자동으로 리워드 지급 (포인트 100점 + 뱃지 점수 2점)
     */
    // [Phase 2-B] NOT_SUPPORTED: 트랜잭션 없이 실행
    // [BEFORE 1] @Transactional(noRollbackFor=...) → Hibernate Session 오염으로 무효
    // [BEFORE 2] @Transactional + REQUIRES_NEW(saveVote) → 내부 rollback이 외부 오염
    // [AFTER] NOT_SUPPORTED: 트랜잭션 없음 → saveVote 예외 catch해도 오염 없음
    //         각 하위 작업(saveVote, earnPoints, recordActivity)이 독립 트랜잭션 사용
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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

        // 4. 같은 이벤트에서 이미 다른 피드에 투표했는지 확인 (앱 레벨 1차 체크)
        if (feedVoteRepository.existsByEventIdAndUserId(feed.getEvent().getId(), userId)) {
            log.info("이미 해당 이벤트에 투표함 - 이벤트ID: {}, 사용자ID: {}", feed.getEvent().getId(), userId);
            return FeedVoteResponseDto.success(false, feed.getParticipantVoteCount());
        }

        // 5. 투표 생성
        // [Phase 2-B] DB 유니크 제약 (event_id, voter_id)으로 동시 요청 시 중복 방지
        // FeedVotePersistenceService(REQUIRES_NEW)에서 flush() 처리
        //   → DataIntegrityViolationException 발생 시 해당 트랜잭션만 rollback
        //   → 외부 트랜잭션(이 메서드) Hibernate Session 오염 없음
        FeedVote vote = FeedVote.builder()
                .feed(feed)
                .voter(user)
                .event(feed.getEvent())
                .build();

        // [Phase 2-B] 독립 트랜잭션(saveVote)으로 INSERT + flush
        // NOT_SUPPORTED 환경에서 DataIntegrityViolationException catch → 트랜잭션 오염 없음
        FeedVote savedVote;
        try {
            savedVote = feedVotePersistenceService.saveVote(vote);
        } catch (DataIntegrityViolationException e) {
            log.info("[Phase 2-B] DB 유니크 제약으로 동시 투표 중복 차단 - 이벤트ID: {}, 사용자ID: {}",
                    feed.getEvent().getId(), userId);
            return FeedVoteResponseDto.success(false, feed.getParticipantVoteCount());
        }

        // 6. 피드 투표 수 증가
        // [BEFORE 1] ORM 레벨 증가 → 동시 요청 시 충돌로 롤백 발생
        // feed.incrementVoteCount();
        // [BEFORE 2] 원자적 SQL UPDATE → feed_votes FK S-lock + feeds UPDATE X-lock 데드락 발생
        // feedRepository.incrementVoteCountAtomic(feedId);

        // [Phase 2-B] Redis INCR 원자적 연산 — lock 없이 투표 수 정합성 보장
        redisTemplate.opsForValue().increment(VOTE_COUNT_KEY + feedId);

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

        // Redis에서 최신 투표 수 반환
        return FeedVoteResponseDto.success(true, (int) getVoteCount(feedId));
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
     * [Phase 2-B] Redis 우선 조회 → Redis 없으면 DB 조회 (SET 없음 — 경쟁 조건 방지)
     */
    public long getVoteCount(Long feedId) {
        String redisKey = VOTE_COUNT_KEY + feedId;
        String cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        // Redis에 값 없으면 DB에서 직접 조회 (SET 하지 않음 → 동시 요청 시 잘못된 초기값 방지)
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
        // Pageable을 사용하여 활성 피드만 조회 (삭제된 피드는 제외)
        Pageable pageable = PageRequest.of(0, 1000); // 한 번에 1000개씩 처리
        Page<Feed> feedPage = feedRepository.findAllActive(pageable);
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
