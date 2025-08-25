package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.service.strategy.EventStrategy;
import com.cMall.feedShop.event.application.service.strategy.EventStrategyFactory;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이벤트 참여 서비스
 * 
 * <p>전략 패턴을 활용하여 이벤트 참여 로직을 처리합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventParticipationService {

    private final EventStrategyFactory strategyFactory;
    private final EventRepository eventRepository;

    /**
     * 이벤트 참여 가능 여부를 확인합니다.
     * 
     * @param eventId 이벤트 ID
     * @param user 사용자
     * @return 참여 가능 여부
     */
    @Transactional(readOnly = true)
    public boolean canParticipate(Long eventId, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다: " + eventId));
        
        EventStrategy strategy = strategyFactory.getStrategy(event.getType());
        return strategy.canParticipate(event, user);
    }

    /**
     * 이벤트 참여를 검증합니다.
     * 
     * @param eventId 이벤트 ID
     * @param feed 참여 피드
     * @throws IllegalArgumentException 참여 조건을 만족하지 않는 경우
     */
    @Transactional(readOnly = true)
    public void validateParticipation(Long eventId, Feed feed) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다: " + eventId));
        
        EventStrategy strategy = strategyFactory.getStrategy(event.getType());
        strategy.validateParticipation(event, feed);
    }

    /**
     * 이벤트 참여자를 생성합니다.
     * 
     * @param eventId 이벤트 ID
     * @param user 사용자
     * @param feed 참여 피드
     * @return 생성된 참여자 정보
     */
    public EventStrategy.EventParticipantInfo createParticipant(Long eventId, User user, Feed feed) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다: " + eventId));
        
        EventStrategy strategy = strategyFactory.getStrategy(event.getType());
        
        log.info("이벤트 참여자 생성 - eventId: {}, userId: {}, feedId: {}", 
                eventId, user.getId(), feed.getId());
        
        return strategy.createParticipant(event, user, feed);
    }

    /**
     * 이벤트 참여 가능 여부를 확인하고 참여자를 생성합니다.
     * 
     * @param eventId 이벤트 ID
     * @param user 사용자
     * @param feed 참여 피드
     * @return 생성된 참여자 정보
     * @throws IllegalArgumentException 참여 조건을 만족하지 않는 경우
     */
    public EventStrategy.EventParticipantInfo participate(Long eventId, User user, Feed feed) {
        // 1. 참여 가능 여부 확인
        if (!canParticipate(eventId, user)) {
            throw new IllegalArgumentException("이벤트에 참여할 수 없습니다.");
        }
        
        // 2. 참여 검증
        validateParticipation(eventId, feed);
        
        // 3. 참여자 생성
        return createParticipant(eventId, user, feed);
    }
}
