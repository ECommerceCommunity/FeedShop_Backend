package com.cMall.feedShop.event.application.service.strategy;

import com.cMall.feedShop.event.domain.enums.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 이벤트 전략 팩토리
 * 
 * <p>이벤트 타입에 따라 적절한 전략을 제공합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Component
public class EventStrategyFactory {

    private final Map<EventType, EventStrategy> strategies;

    /**
     * 생성자에서 모든 EventStrategy 구현체를 주입받아 Map으로 관리
     */
    public EventStrategyFactory(List<EventStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        EventStrategy::getEventType,
                        strategy -> strategy
                ));
        
        log.info("이벤트 전략 팩토리 초기화 완료 - 등록된 전략: {}", 
                strategies.keySet());
    }

    /**
     * 이벤트 타입에 따른 전략을 반환합니다.
     * 
     * @param eventType 이벤트 타입
     * @return 해당 이벤트 타입의 전략
     * @throws IllegalArgumentException 지원하지 않는 이벤트 타입인 경우
     */
    public EventStrategy getStrategy(EventType eventType) {
        EventStrategy strategy = strategies.get(eventType);
        
        if (strategy == null) {
            log.error("지원하지 않는 이벤트 타입: {}", eventType);
            throw new IllegalArgumentException("지원하지 않는 이벤트 타입입니다: " + eventType);
        }
        
        log.debug("이벤트 타입 {}에 대한 전략 반환: {}", eventType, strategy.getClass().getSimpleName());
        return strategy;
    }

    /**
     * 등록된 모든 전략의 이벤트 타입을 반환합니다.
     * 
     * @return 지원하는 이벤트 타입 목록
     */
    public List<EventType> getSupportedEventTypes() {
        return List.copyOf(strategies.keySet());
    }

    /**
     * 특정 이벤트 타입이 지원되는지 확인합니다.
     * 
     * @param eventType 확인할 이벤트 타입
     * @return 지원 여부
     */
    public boolean isSupported(EventType eventType) {
        return strategies.containsKey(eventType);
    }
}
