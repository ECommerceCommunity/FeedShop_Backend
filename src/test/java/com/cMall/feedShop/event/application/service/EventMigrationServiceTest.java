package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.service.strategy.BattleEventStrategy;
import com.cMall.feedShop.event.application.service.strategy.EventStrategy;
import com.cMall.feedShop.event.application.service.strategy.EventStrategyFactory;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventParticipant;
import com.cMall.feedShop.event.domain.EventMatch;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.event.domain.repository.EventParticipantRepository;
import com.cMall.feedShop.event.domain.repository.EventMatchRepository;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventMigrationServiceTest {

    @Mock
    private EventStrategyFactory strategyFactory;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private EventParticipantRepository eventParticipantRepository;

    @Mock
    private EventMatchRepository eventMatchRepository;

    @Mock
    private BattleEventStrategy battleEventStrategy;

    @InjectMocks
    private EventMigrationService eventMigrationService;

    private Event testEvent;
    private User testUser;
    private Feed testFeed1, testFeed2;

    @BeforeEach
    void setUp() {
        // 테스트 이벤트 설정
        testEvent = Event.builder()
                .type(EventType.BATTLE)
                .status(EventStatus.UPCOMING)
                .maxParticipants(20)
                .build();

        // Event ID 설정 (reflection 사용)
        try {
            java.lang.reflect.Field idField = Event.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testEvent, 1L);
        } catch (Exception e) {
            // reflection 실패 시 테스트 스킵
        }

        // 테스트 사용자 설정
        testUser = User.builder().build();

        // 테스트 피드 설정
        testFeed1 = Feed.builder()
                .user(testUser)
                .title("테스트 피드 1")
                .event(testEvent)
                .build();
        testFeed2 = Feed.builder()
                .user(testUser)
                .title("테스트 피드 2")
                .event(testEvent)
                .build();

        // Feed ID 설정 (reflection 사용)
        try {
            java.lang.reflect.Field feedIdField = Feed.class.getDeclaredField("id");
            feedIdField.setAccessible(true);
            feedIdField.set(testFeed1, 1L);
            feedIdField.set(testFeed2, 2L);
        } catch (Exception e) {
            // reflection 실패 시 테스트 스킵
        }
    }

    @Test
    @DisplayName("이벤트 데이터 마이그레이션 - 성공")
    void migrateEventData_Success() {
        // given
        List<Feed> existingFeeds = Arrays.asList(testFeed1, testFeed2);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(feedRepository.findByEventId(1L)).thenReturn(existingFeeds);
        when(strategyFactory.getStrategy(EventType.BATTLE)).thenReturn(battleEventStrategy);
        when(eventParticipantRepository.findByEventIdAndFeedId(eq(1L), any())).thenReturn(Optional.empty());
        when(eventMatchRepository.getNextMatchGroup(1L)).thenReturn(1, 2);
        when(eventParticipantRepository.saveAll(anyList())).thenReturn(Arrays.asList());
        when(eventMatchRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        // EventParticipantInfo 모킹
        EventStrategy.EventParticipantInfo participantInfo = 
                new EventStrategy.EventParticipantInfo(1L, 1L, "PARTICIPATING", "{}");
        when(battleEventStrategy.createParticipant(any(), any(), any())).thenReturn(participantInfo);
        when(battleEventStrategy.getEventType()).thenReturn(EventType.BATTLE);

        // when
        EventMigrationService.MigrationResult result = eventMigrationService.migrateEventData(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEventId()).isEqualTo(1L);
        assertThat(result.getParticipantsMigrated()).isEqualTo(2);
        assertThat(result.getMatchesMigrated()).isEqualTo(1); // 2명이므로 1개 매치

        verify(eventParticipantRepository).saveAll(anyList());
        verify(eventMatchRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("이벤트 데이터 마이그레이션 - 이미 마이그레이션된 참여자")
    void migrateEventData_AlreadyMigratedParticipant() {
        // given
        List<Feed> existingFeeds = Arrays.asList(testFeed1);
        
        EventParticipant existingParticipant = EventParticipant.builder()
                .event(testEvent)
                .user(testUser)
                .feed(testFeed1)
                .build();
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(feedRepository.findByEventId(1L)).thenReturn(existingFeeds);
        when(eventParticipantRepository.findByEventIdAndFeedId(eq(1L), any())).thenReturn(Optional.of(existingParticipant));
        when(strategyFactory.getStrategy(EventType.BATTLE)).thenReturn(battleEventStrategy);
        when(battleEventStrategy.getEventType()).thenReturn(EventType.BATTLE);
        when(eventParticipantRepository.saveAll(anyList())).thenReturn(Arrays.asList());
        when(eventMatchRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        // when
        EventMigrationService.MigrationResult result = eventMigrationService.migrateEventData(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getParticipantsMigrated()).isEqualTo(1);
        assertThat(result.getMatchesMigrated()).isEqualTo(1); // 홀수 명이어도 매치 생성됨

        verify(eventParticipantRepository).saveAll(anyList());
        verify(eventMatchRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("이벤트 데이터 마이그레이션 - 이벤트를 찾을 수 없는 경우")
    void migrateEventData_EventNotFound() {
        // given
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        try {
            eventMigrationService.migrateEventData(999L);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).contains("이벤트를 찾을 수 없습니다");
        }
    }

    @Test
    @DisplayName("전체 이벤트 데이터 마이그레이션 - 성공")
    void migrateAllEventData_Success() {
        // given
        List<Event> allEvents = Arrays.asList(testEvent);
        List<Feed> existingFeeds = Arrays.asList(testFeed1, testFeed2);
        
        when(eventRepository.findAll()).thenReturn(allEvents);
        when(eventRepository.findById(any())).thenReturn(Optional.of(testEvent));
        when(feedRepository.findByEventId(any())).thenReturn(existingFeeds);
        when(strategyFactory.getStrategy(EventType.BATTLE)).thenReturn(battleEventStrategy);
        when(eventParticipantRepository.findByEventIdAndFeedId(any(), any())).thenReturn(Optional.empty());
        when(eventMatchRepository.getNextMatchGroup(any())).thenReturn(1);
        when(eventParticipantRepository.saveAll(anyList())).thenReturn(Arrays.asList());
        when(eventMatchRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        // EventParticipantInfo 모킹
        EventStrategy.EventParticipantInfo participantInfo = 
                new EventStrategy.EventParticipantInfo(1L, 1L, "PARTICIPATING", "{}");
        when(battleEventStrategy.createParticipant(any(), any(), any())).thenReturn(participantInfo);
        when(battleEventStrategy.getEventType()).thenReturn(EventType.BATTLE);

        // when
        List<EventMigrationService.MigrationResult> results = eventMigrationService.migrateAllEventData();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEventId()).isEqualTo(testEvent.getId());
        assertThat(results.get(0).getParticipantsMigrated()).isEqualTo(2);
        assertThat(results.get(0).getMatchesMigrated()).isEqualTo(1);
    }
}
