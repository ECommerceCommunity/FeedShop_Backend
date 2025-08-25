package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.service.strategy.EventStrategy;
import com.cMall.feedShop.event.application.service.strategy.EventStrategyFactory;
import com.cMall.feedShop.event.application.service.strategy.EventStrategy.EventParticipantInfo;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventParticipant;
import com.cMall.feedShop.event.domain.EventMatch;
import com.cMall.feedShop.event.domain.repository.EventParticipantRepository;
import com.cMall.feedShop.event.domain.repository.EventMatchRepository;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 이벤트 마이그레이션 서비스
 * 
 * <p>기존 시스템에서 새로운 시스템으로의 점진적 마이그레이션을 처리합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventMigrationService {

    private final EventStrategyFactory strategyFactory;
    private final EventRepository eventRepository;
    private final FeedRepository feedRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final EventMatchRepository eventMatchRepository;

    /**
     * 특정 이벤트의 기존 데이터를 새로운 시스템으로 마이그레이션
     * 
     * @param eventId 이벤트 ID
     * @return 마이그레이션 결과
     */
    public MigrationResult migrateEventData(Long eventId) {
        log.info("이벤트 데이터 마이그레이션 시작 - eventId: {}", eventId);
        
        // 1. 이벤트 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다: " + eventId));
        
        // 2. 기존 참여자 데이터 조회 (Feed 기반)
        List<Feed> existingFeeds = feedRepository.findByEventId(eventId);
        
        // 3. 새로운 참여자 엔터티 생성
        List<EventParticipant> migratedParticipants = migrateParticipants(event, existingFeeds);
        
        // 4. 이벤트 타입에 따른 매치 데이터 생성
        List<EventMatch> migratedMatches = migrateMatches(event, existingFeeds);
        
        // 5. 데이터 저장
        eventParticipantRepository.saveAll(migratedParticipants);
        eventMatchRepository.saveAll(migratedMatches);
        
        MigrationResult result = MigrationResult.builder()
                .eventId(eventId)
                .participantsMigrated(migratedParticipants.size())
                .matchesMigrated(migratedMatches.size())
                .build();
        
        log.info("이벤트 데이터 마이그레이션 완료 - eventId: {}, 참여자: {}, 매치: {}", 
                eventId, result.getParticipantsMigrated(), result.getMatchesMigrated());
        
        return result;
    }

    /**
     * 모든 이벤트 데이터 마이그레이션
     * 
     * @return 전체 마이그레이션 결과
     */
    public List<MigrationResult> migrateAllEventData() {
        log.info("전체 이벤트 데이터 마이그레이션 시작");
        
        List<Event> allEvents = eventRepository.findAll();
        return allEvents.stream()
                .map(event -> migrateEventData(event.getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 참여자 데이터 마이그레이션
     */
    private List<EventParticipant> migrateParticipants(Event event, List<Feed> feeds) {
        EventStrategy strategy = strategyFactory.getStrategy(event.getType());
        
        return feeds.stream()
                .map(feed -> {
                    // 기존 참여자 데이터가 있는지 확인
                    Optional<EventParticipant> existingParticipant = 
                            eventParticipantRepository.findByEventIdAndFeedId(event.getId(), feed.getId());
                    
                    if (existingParticipant.isPresent()) {
                        log.debug("이미 마이그레이션된 참여자 데이터 존재 - eventId: {}, feedId: {}", 
                                event.getId(), feed.getId());
                        return existingParticipant.get();
                    }
                    
                    // 새로운 참여자 생성
                    EventParticipantInfo participantInfo = strategy.createParticipant(event, feed.getUser(), feed);
                    
                    return EventParticipant.builder()
                            .event(event)
                            .user(feed.getUser())
                            .feed(feed)
                            .metadata(participantInfo.getMetadata())
                            .build();
                })
                .toList();
    }

    /**
     * 매치 데이터 마이그레이션
     */
    private List<EventMatch> migrateMatches(Event event, List<Feed> feeds) {
        EventStrategy strategy = strategyFactory.getStrategy(event.getType());
        
        // 배틀 이벤트인 경우에만 매치 데이터 생성
        if (strategy.getEventType().name().equals("BATTLE")) {
            return createBattleMatches(event, feeds);
        }
        
        // 랭킹 이벤트는 매치가 없으므로 빈 리스트 반환
        return List.of();
    }

    /**
     * 배틀 매치 생성
     */
    private List<EventMatch> createBattleMatches(Event event, List<Feed> feeds) {
        List<EventMatch> matches = new java.util.ArrayList<>();
        
        // 2명씩 매칭
        for (int i = 0; i < feeds.size() - 1; i += 2) {
            Feed participant1 = feeds.get(i);
            Feed participant2 = feeds.get(i + 1);
            
            Integer matchGroup = eventMatchRepository.getNextMatchGroup(event.getId());
            
            EventMatch match = EventMatch.builder()
                    .event(event)
                    .matchGroup(matchGroup)
                    .participant1(participant1)
                    .participant2(participant2)
                    .metadata(EventMatch.createBattleMetadata(1, "1라운드"))
                    .build();
            
            matches.add(match);
        }
        
        // 홀수 명일 경우 마지막 참여자는 자동 우승
        if (feeds.size() % 2 == 1) {
            Feed lastParticipant = feeds.get(feeds.size() - 1);
            Integer matchGroup = eventMatchRepository.getNextMatchGroup(event.getId());
            
            EventMatch match = EventMatch.builder()
                    .event(event)
                    .matchGroup(matchGroup)
                    .participant1(lastParticipant)
                    .participant2(null)
                    .metadata(EventMatch.createBattleMetadata(1, "자동 우승"))
                    .build();
            
            matches.add(match);
        }
        
        return matches;
    }

    /**
     * 마이그레이션 결과
     */
    @lombok.Builder
    @lombok.Getter
    public static class MigrationResult {
        private Long eventId;
        private Integer participantsMigrated;
        private Integer matchesMigrated;
    }
}
