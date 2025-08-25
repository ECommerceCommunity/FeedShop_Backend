package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventResultCreateRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventResultResponseDto;
import com.cMall.feedShop.event.application.service.strategy.EventStrategy;
import com.cMall.feedShop.event.application.service.strategy.EventStrategyFactory;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventResult;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.event.domain.repository.EventResultRepository;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 이벤트 결과 관리 서비스
 * 
 * <p>새로운 아키텍처를 기반으로 하는 이벤트 결과 생성, 조회 등의 비즈니스 로직을 처리합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventResultManagementService {

    private final EventStrategyFactory strategyFactory;
    private final EventRepository eventRepository;
    private final EventResultRepository eventResultRepository;
    private final FeedRepository feedRepository;

    /**
     * 이벤트 결과 생성
     * 
     * @param requestDto 이벤트 결과 생성 요청
     * @return 생성된 이벤트 결과
     */
    public EventResultResponseDto createEventResult(EventResultCreateRequestDto requestDto) {
        log.info("이벤트 결과 생성 시작 - eventId: {}", requestDto.getEventId());
        
        // 1. 이벤트 조회
        Event event = eventRepository.findById(requestDto.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다: " + requestDto.getEventId()));
        
        // 2. 기존 결과 확인
        if (!requestDto.getForceRecalculate() && eventResultRepository.existsByEventId(event.getId())) {
            throw new IllegalStateException("이미 결과가 존재합니다. 강제 재계산을 원하면 forceRecalculate=true로 설정하세요.");
        }
        
        // 3. 이벤트 참여자 조회
        List<Feed> participants = feedRepository.findByEventId(event.getId());
        if (participants.isEmpty()) {
            throw new IllegalStateException("이벤트에 참여자가 없습니다.");
        }
        
        // 4. 전략 패턴을 사용하여 결과 계산
        EventStrategy strategy = strategyFactory.getStrategy(event.getType());
        EventResult eventResult = strategy.calculateResult(event, participants);
        
        // 5. 기존 결과가 있으면 삭제
        if (requestDto.getForceRecalculate()) {
            eventResultRepository.findByEventId(event.getId())
                    .ifPresent(eventResultRepository::delete);
        }
        
        // 6. 결과 저장
        EventResult savedResult = eventResultRepository.save(eventResult);
        
        log.info("이벤트 결과 생성 완료 - eventId: {}, resultId: {}", event.getId(), savedResult.getId());
        
        return EventResultResponseDto.from(savedResult);
    }

    /**
     * 이벤트 결과 조회
     * 
     * @param eventId 이벤트 ID
     * @return 이벤트 결과
     */
    @Transactional(readOnly = true)
    public EventResultResponseDto getEventResult(Long eventId) {
        log.info("이벤트 결과 조회 - eventId: {}", eventId);
        
        EventResult eventResult = eventResultRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 결과를 찾을 수 없습니다: " + eventId));
        
        return EventResultResponseDto.from(eventResult);
    }

    /**
     * 이벤트 결과 존재 여부 확인
     * 
     * @param eventId 이벤트 ID
     * @return 결과 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean hasEventResult(Long eventId) {
        return eventResultRepository.existsByEventId(eventId);
    }

    /**
     * 이벤트 결과 삭제
     * 
     * @param eventId 이벤트 ID
     */
    public void deleteEventResult(Long eventId) {
        log.info("이벤트 결과 삭제 - eventId: {}", eventId);
        
        EventResult eventResult = eventResultRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트 결과를 찾을 수 없습니다: " + eventId));
        
        eventResultRepository.delete(eventResult);
        
        log.info("이벤트 결과 삭제 완료 - eventId: {}", eventId);
    }

    /**
     * 이벤트 결과 재계산
     * 
     * @param eventId 이벤트 ID
     * @return 재계산된 이벤트 결과
     */
    public EventResultResponseDto recalculateEventResult(Long eventId) {
        log.info("이벤트 결과 재계산 시작 - eventId: {}", eventId);
        
        EventResultCreateRequestDto requestDto = EventResultCreateRequestDto.builder()
                .eventId(eventId)
                .forceRecalculate(true)
                .build();
        
        return createEventResult(requestDto);
    }
}
