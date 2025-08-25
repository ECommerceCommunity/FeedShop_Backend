package com.cMall.feedShop.event.application.service.strategy;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventResult;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.user.domain.model.User;

import java.util.List;

/**
 * 이벤트 처리 전략 인터페이스
 * 
 * <p>이벤트 유형별로 다른 로직을 처리하기 위한 전략 패턴의 핵심 인터페이스입니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
public interface EventStrategy {
    
    /**
     * 이 전략이 처리할 이벤트 타입을 반환합니다.
     * 
     * @return 이벤트 타입
     */
    EventType getEventType();
    
    /**
     * 이벤트 결과를 계산합니다.
     * 
     * @param event 이벤트
     * @param participants 참여자 목록
     * @return 계산된 이벤트 결과
     */
    EventResult calculateResult(Event event, List<Feed> participants);
    
    /**
     * 이벤트 참여 가능 여부를 확인합니다.
     * 
     * @param event 이벤트
     * @param user 사용자
     * @return 참여 가능 여부
     */
    boolean canParticipate(Event event, User user);
    
    /**
     * 이벤트 참여를 검증합니다.
     * 
     * @param event 이벤트
     * @param feed 참여 피드
     * @throws IllegalArgumentException 참여 조건을 만족하지 않는 경우
     */
    void validateParticipation(Event event, Feed feed);
    
    /**
     * 이벤트 참여자를 생성합니다.
     * 
     * @param event 이벤트
     * @param user 사용자
     * @param feed 참여 피드
     * @return 생성된 참여자 정보
     */
    EventParticipantInfo createParticipant(Event event, User user, Feed feed);
    
    /**
     * 이벤트 참여자 정보
     */
    class EventParticipantInfo {
        private final Long userId;
        private final Long feedId;
        private final String status;
        private final String metadata;
        
        public EventParticipantInfo(Long userId, Long feedId, String status, String metadata) {
            this.userId = userId;
            this.feedId = feedId;
            this.status = status;
            this.metadata = metadata;
        }
        
        // Getters
        public Long getUserId() { return userId; }
        public Long getFeedId() { return feedId; }
        public String getStatus() { return status; }
        public String getMetadata() { return metadata; }
    }
}
