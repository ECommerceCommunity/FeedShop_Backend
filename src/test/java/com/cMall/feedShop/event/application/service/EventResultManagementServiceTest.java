package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventResultCreateRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventResultResponseDto;
import com.cMall.feedShop.event.application.service.strategy.BattleEventStrategy;
import com.cMall.feedShop.event.application.service.strategy.EventStrategyFactory;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventResult;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.event.domain.repository.EventResultRepository;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.feed.domain.Feed;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventResultManagementServiceTest {

    @Mock
    private EventStrategyFactory strategyFactory;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventResultRepository eventResultRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private BattleEventStrategy battleEventStrategy;

    @InjectMocks
    private EventResultManagementService eventResultManagementService;

    private Event testEvent;
    private User testUser;
    private Feed testFeed;
    private EventResult testEventResult;

    @BeforeEach
    void setUp() {
        // 테스트 이벤트 설정
        testEvent = Event.builder()
                .type(EventType.BATTLE)
                .status(EventStatus.UPCOMING)
                .maxParticipants(20)
                .build();

        // 테스트 사용자 설정
        testUser = User.builder().build();

        // 테스트 피드 설정
        testFeed = Feed.builder()
                .user(testUser)
                .title("테스트 피드")
                .event(testEvent)
                .build();

        // 테스트 이벤트 결과 설정
        testEventResult = EventResult.builder()
                .event(testEvent)
                .resultType(EventResult.ResultType.BATTLE_WINNER)
                .totalParticipants(1)
                .totalVotes(10L)
                .build();
    }

    @Test
    @DisplayName("이벤트 결과 생성 - 성공")
    void createEventResult_Success() {
        // given
        EventResultCreateRequestDto requestDto = EventResultCreateRequestDto.builder()
                .eventId(1L)
                .forceRecalculate(false)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventResultRepository.existsByEventId(1L)).thenReturn(false);
        when(feedRepository.findByEventId(1L)).thenReturn(Arrays.asList(testFeed));
        when(strategyFactory.getStrategy(EventType.BATTLE)).thenReturn(battleEventStrategy);
        when(battleEventStrategy.calculateResult(any(), any())).thenReturn(testEventResult);
        when(eventResultRepository.save(any())).thenReturn(testEventResult);

        // when
        EventResultResponseDto result = eventResultManagementService.createEventResult(requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEventId()).isEqualTo(1L);
        verify(eventResultRepository).save(any(EventResult.class));
    }

    @Test
    @DisplayName("이벤트 결과 생성 - 이미 결과가 존재하는 경우")
    void createEventResult_AlreadyExists() {
        // given
        EventResultCreateRequestDto requestDto = EventResultCreateRequestDto.builder()
                .eventId(1L)
                .forceRecalculate(false)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventResultRepository.existsByEventId(1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> eventResultManagementService.createEventResult(requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 결과가 존재합니다");
    }

    @Test
    @DisplayName("이벤트 결과 생성 - 강제 재계산")
    void createEventResult_ForceRecalculate() {
        // given
        EventResultCreateRequestDto requestDto = EventResultCreateRequestDto.builder()
                .eventId(1L)
                .forceRecalculate(true)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(feedRepository.findByEventId(1L)).thenReturn(Arrays.asList(testFeed));
        when(strategyFactory.getStrategy(EventType.BATTLE)).thenReturn(battleEventStrategy);
        when(battleEventStrategy.calculateResult(any(), any())).thenReturn(testEventResult);
        when(eventResultRepository.save(any())).thenReturn(testEventResult);
        when(eventResultRepository.findByEventId(1L)).thenReturn(Optional.of(testEventResult));

        // when
        EventResultResponseDto result = eventResultManagementService.createEventResult(requestDto);

        // then
        assertThat(result).isNotNull();
        verify(eventResultRepository).delete(any(EventResult.class));
        verify(eventResultRepository).save(any(EventResult.class));
    }

    @Test
    @DisplayName("이벤트 결과 생성 - 이벤트를 찾을 수 없는 경우")
    void createEventResult_EventNotFound() {
        // given
        EventResultCreateRequestDto requestDto = EventResultCreateRequestDto.builder()
                .eventId(999L)
                .forceRecalculate(false)
                .build();

        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventResultManagementService.createEventResult(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이벤트를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("이벤트 결과 생성 - 참여자가 없는 경우")
    void createEventResult_NoParticipants() {
        // given
        EventResultCreateRequestDto requestDto = EventResultCreateRequestDto.builder()
                .eventId(1L)
                .forceRecalculate(false)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventResultRepository.existsByEventId(1L)).thenReturn(false);
        when(feedRepository.findByEventId(1L)).thenReturn(Arrays.asList());

        // when & then
        assertThatThrownBy(() -> eventResultManagementService.createEventResult(requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이벤트에 참여자가 없습니다");
    }

    @Test
    @DisplayName("이벤트 결과 조회 - 성공")
    void getEventResult_Success() {
        // given
        when(eventResultRepository.findByEventId(1L)).thenReturn(Optional.of(testEventResult));

        // when
        EventResultResponseDto result = eventResultManagementService.getEventResult(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEventId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이벤트 결과 조회 - 결과를 찾을 수 없는 경우")
    void getEventResult_NotFound() {
        // given
        when(eventResultRepository.findByEventId(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventResultManagementService.getEventResult(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이벤트 결과를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("이벤트 결과 존재 여부 확인")
    void hasEventResult() {
        // given
        when(eventResultRepository.existsByEventId(1L)).thenReturn(true);

        // when
        boolean exists = eventResultManagementService.hasEventResult(1L);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이벤트 결과 삭제 - 성공")
    void deleteEventResult_Success() {
        // given
        when(eventResultRepository.findByEventId(1L)).thenReturn(Optional.of(testEventResult));

        // when
        eventResultManagementService.deleteEventResult(1L);

        // then
        verify(eventResultRepository).delete(testEventResult);
    }

    @Test
    @DisplayName("이벤트 결과 삭제 - 결과를 찾을 수 없는 경우")
    void deleteEventResult_NotFound() {
        // given
        when(eventResultRepository.findByEventId(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventResultManagementService.deleteEventResult(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이벤트 결과를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("이벤트 결과 재계산 - 성공")
    void recalculateEventResult_Success() {
        // given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(feedRepository.findByEventId(1L)).thenReturn(Arrays.asList(testFeed));
        when(strategyFactory.getStrategy(EventType.BATTLE)).thenReturn(battleEventStrategy);
        when(battleEventStrategy.calculateResult(any(), any())).thenReturn(testEventResult);
        when(eventResultRepository.save(any())).thenReturn(testEventResult);
        when(eventResultRepository.findByEventId(1L)).thenReturn(Optional.of(testEventResult));

        // when
        EventResultResponseDto result = eventResultManagementService.recalculateEventResult(1L);

        // then
        assertThat(result).isNotNull();
        verify(eventResultRepository).delete(any(EventResult.class));
        verify(eventResultRepository).save(any(EventResult.class));
    }
}
