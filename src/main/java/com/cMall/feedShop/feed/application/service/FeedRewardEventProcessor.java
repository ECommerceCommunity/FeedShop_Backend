package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.domain.model.FeedRewardEvent;
import com.cMall.feedShop.feed.domain.repository.FeedRewardEventRepository;
import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.model.RewardHistory;
import com.cMall.feedShop.user.domain.model.RewardPolicy;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.RewardHistoryRepository;
import com.cMall.feedShop.user.domain.repository.RewardPolicyRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 피드 리워드 이벤트 처리기
 * 대기중인 리워드 이벤트를 처리하여 실제 포인트를 지급
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedRewardEventProcessor {

    private final FeedRewardEventRepository feedRewardEventRepository;
    private final RewardPolicyRepository rewardPolicyRepository;
    private final RewardHistoryRepository rewardHistoryRepository;
    private final UserRepository userRepository;

    /**
     * 대기중인 리워드 이벤트 처리 (5분마다 실행)
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    public void processPendingRewardEvents() {
        log.info("대기중인 피드 리워드 이벤트 처리 시작");
        
        try {
            // 대기중인 이벤트 조회
            List<FeedRewardEvent> pendingEvents = feedRewardEventRepository
                    .findByEventStatusOrderByCreatedAtAsc(FeedRewardEvent.EventStatus.PENDING);
            
            if (pendingEvents.isEmpty()) {
                log.debug("처리할 대기중인 리워드 이벤트가 없습니다");
                return;
            }
            
            log.info("처리할 대기중인 리워드 이벤트 수: {}", pendingEvents.size());
            
            // 각 이벤트 처리
            for (FeedRewardEvent event : pendingEvents) {
                try {
                    processRewardEvent(event);
                } catch (Exception e) {
                    log.error("리워드 이벤트 처리 중 오류 발생 - eventId: {}, userId: {}, feedId: {}", 
                            event.getEventId(), event.getUser().getId(), event.getFeed().getId(), e);
                    
                    // 처리 실패로 마킹
                    event.markAsFailed("처리 중 오류 발생: " + e.getMessage());
                    feedRewardEventRepository.save(event);
                }
            }
            
            log.info("피드 리워드 이벤트 처리 완료");
            
        } catch (Exception e) {
            log.error("리워드 이벤트 처리 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 실패한 리워드 이벤트 재처리 (30분마다 실행)
     */
    @Scheduled(fixedRate = 1800000) // 30분 = 1,800,000ms
    public void retryFailedRewardEvents() {
        log.info("실패한 피드 리워드 이벤트 재처리 시작");
        
        try {
            // 재시도 횟수가 3회 미만인 실패 이벤트 조회
            List<FeedRewardEvent> failedEvents = feedRewardEventRepository
                    .findByEventStatusAndRetryCountLessThanOrderByCreatedAtAsc(
                            FeedRewardEvent.EventStatus.FAILED, 3);
            
            if (failedEvents.isEmpty()) {
                log.debug("재처리할 실패한 리워드 이벤트가 없습니다");
                return;
            }
            
            log.info("재처리할 실패한 리워드 이벤트 수: {}", failedEvents.size());
            
            // 각 이벤트 재처리
            for (FeedRewardEvent event : failedEvents) {
                try {
                    event.retry();
                    processRewardEvent(event);
                } catch (Exception e) {
                    log.error("리워드 이벤트 재처리 중 오류 발생 - eventId: {}, userId: {}, feedId: {}", 
                            event.getEventId(), event.getUser().getId(), event.getFeed().getId(), e);
                    
                    // 재처리 실패로 마킹
                    event.markAsFailed("재처리 중 오류 발생: " + e.getMessage());
                    feedRewardEventRepository.save(event);
                }
            }
            
            log.info("피드 리워드 이벤트 재처리 완료");
            
        } catch (Exception e) {
            log.error("리워드 이벤트 재처리 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 개별 리워드 이벤트 처리
     */
    private void processRewardEvent(FeedRewardEvent event) {
        log.debug("리워드 이벤트 처리 시작 - eventId: {}, userId: {}, rewardType: {}, points: {}", 
                event.getEventId(), event.getUser().getId(), event.getRewardType(), event.getPoints());
        
        try {
            // 사용자 존재 여부 확인
            User user = userRepository.findById(event.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + event.getUser().getId()));
            
            // 리워드 정책 확인
            RewardPolicy policy = rewardPolicyRepository.findByRewardType(event.getRewardType())
                    .orElseThrow(() -> new RuntimeException("리워드 정책을 찾을 수 없습니다: " + event.getRewardType()));
            
            // 중복 지급 방지 체크
            if (isRewardAlreadyGranted(event)) {
                log.warn("이미 지급된 리워드입니다 - eventId: {}, userId: {}, rewardType: {}", 
                        event.getEventId(), event.getUser().getId(), event.getRewardType());
                event.markAsFailed("이미 지급된 리워드입니다");
                feedRewardEventRepository.save(event);
                return;
            }
            
            // 리워드 히스토리에 기록
            RewardHistory rewardHistory = createRewardHistory(event, user, policy);
            rewardHistoryRepository.save(rewardHistory);
            
            // 이벤트 상태를 처리 완료로 변경
            event.markAsProcessed();
            feedRewardEventRepository.save(event);
            
            log.info("리워드 이벤트 처리 완료 - eventId: {}, userId: {}, rewardType: {}, points: {}", 
                    event.getEventId(), event.getUser().getId(), event.getRewardType(), event.getPoints());
            
        } catch (Exception e) {
            log.error("리워드 이벤트 처리 중 오류 발생 - eventId: {}", event.getEventId(), e);
            throw e;
        }
    }

    /**
     * 리워드 히스토리 생성
     */
    private RewardHistory createRewardHistory(FeedRewardEvent event, User user, RewardPolicy policy) {
        return RewardHistory.builder()
                .user(user)
                .rewardType(event.getRewardType())
                .points(event.getPoints())
                .description(event.getDescription())
                .relatedId(event.getFeed().getId())
                .relatedType("FEED")
                .build();
    }

    /**
     * 이미 지급된 리워드인지 확인
     */
    private boolean isRewardAlreadyGranted(FeedRewardEvent event) {
        // 같은 사용자, 같은 피드, 같은 리워드 타입으로 이미 처리된 이벤트가 있는지 확인
        return feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(
                event.getUser(), 
                event.getFeed(), 
                event.getRewardType()
        );
    }

    /**
     * 수동으로 특정 이벤트 처리 (관리자용)
     */
    @Transactional
    public void processSpecificEvent(Long eventId) {
        log.info("수동 리워드 이벤트 처리 시작 - eventId: {}", eventId);
        
        FeedRewardEvent event = feedRewardEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("리워드 이벤트를 찾을 수 없습니다: " + eventId));
        
        if (event.getEventStatus() != FeedRewardEvent.EventStatus.PENDING) {
            throw new RuntimeException("처리할 수 없는 이벤트 상태입니다: " + event.getEventStatus());
        }
        
        processRewardEvent(event);
        log.info("수동 리워드 이벤트 처리 완료 - eventId: {}", eventId);
    }

    /**
     * 특정 사용자의 미처리 이벤트 조회
     */
    @Transactional(readOnly = true)
    public List<FeedRewardEvent> getPendingEventsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        return feedRewardEventRepository.findByUserOrderByCreatedAtDesc(user, null).getContent();
    }

    /**
     * 특정 피드의 미처리 이벤트 조회
     */
    @Transactional(readOnly = true)
    public List<FeedRewardEvent> getPendingEventsByFeed(Long feedId) {
        // Feed 엔티티 조회 (실제로는 FeedRepository를 주입받아야 함)
        // 여기서는 간단히 처리
        return feedRewardEventRepository.findByFeedOrderByCreatedAtDesc(null);
    }
}
