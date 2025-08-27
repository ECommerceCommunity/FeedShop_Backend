package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventStatusService 테스트")
class EventStatusServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventStatusService eventStatusService;

    private Event upcomingEvent;
    private Event ongoingEvent;
    private Event endedEvent;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.of(2025, 8, 15);
        
        // 예정된 이벤트 (시작일이 미래)
        EventDetail upcomingDetail = EventDetail.builder()
                .eventStartDate(LocalDate.of(2025, 8, 20))
                .eventEndDate(LocalDate.of(2025, 8, 25))
                .build();
        
        upcomingEvent = Event.builder()
                .id(1L)
                .type(EventType.BATTLE)
                .status(EventStatus.UPCOMING)
                .maxParticipants(100)
                .eventDetail(upcomingDetail)
                .build();

        // 진행 중인 이벤트 (시작일과 종료일 사이)
        EventDetail ongoingDetail = EventDetail.builder()
                .eventStartDate(LocalDate.of(2025, 8, 10))
                .eventEndDate(LocalDate.of(2025, 8, 20))
                .build();
        
        ongoingEvent = Event.builder()
                .id(2L)
                .type(EventType.BATTLE)
                .status(EventStatus.ONGOING)
                .maxParticipants(100)
                .eventDetail(ongoingDetail)
                .build();

        // 종료된 이벤트 (종료일이 과거)
        EventDetail endedDetail = EventDetail.builder()
                .eventStartDate(LocalDate.of(2025, 8, 1))
                .eventEndDate(LocalDate.of(2025, 8, 10))
                .build();
        
        endedEvent = Event.builder()
                .id(3L)
                .type(EventType.BATTLE)
                .status(EventStatus.ENDED)
                .maxParticipants(100)
                .eventDetail(endedDetail)
                .build();
    }

    @Test
    @DisplayName("이벤트 상태 계산 - 예정된 이벤트")
    void calculateEventStatus_UpcomingEvent() {
        // When
        EventStatus status = eventStatusService.calculateEventStatus(upcomingEvent, today);

        // Then
        assertThat(status).isEqualTo(EventStatus.UPCOMING);
    }

    @Test
    @DisplayName("이벤트 상태 계산 - 진행 중인 이벤트")
    void calculateEventStatus_OngoingEvent() {
        // When
        EventStatus status = eventStatusService.calculateEventStatus(ongoingEvent, today);

        // Then
        assertThat(status).isEqualTo(EventStatus.ONGOING);
    }

    @Test
    @DisplayName("이벤트 상태 계산 - 종료된 이벤트")
    void calculateEventStatus_EndedEvent() {
        // When
        EventStatus status = eventStatusService.calculateEventStatus(endedEvent, today);

        // Then
        assertThat(status).isEqualTo(EventStatus.ENDED);
    }

    @Test
    @DisplayName("이벤트 상태 계산 - EventDetail이 null인 경우")
    void calculateEventStatus_NullEventDetail() {
        // Given
        Event eventWithoutDetail = Event.builder()
                .id(4L)
                .type(EventType.BATTLE)
                .status(EventStatus.UPCOMING)
                .build();

        // When
        EventStatus status = eventStatusService.calculateEventStatus(eventWithoutDetail, today);

        // Then
        assertThat(status).isEqualTo(EventStatus.UPCOMING); // 기존 상태 반환
    }

    @Test
    @DisplayName("이벤트 상태 계산 - 날짜가 null인 경우")
    void calculateEventStatus_NullDates() {
        // Given
        EventDetail detailWithNullDates = EventDetail.builder()
                .eventStartDate(null)
                .eventEndDate(null)
                .build();
        
        Event eventWithNullDates = Event.builder()
                .id(5L)
                .type(EventType.BATTLE)
                .status(EventStatus.UPCOMING)
                .eventDetail(detailWithNullDates)
                .build();

        // When
        EventStatus status = eventStatusService.calculateEventStatus(eventWithNullDates, today);

        // Then
        assertThat(status).isEqualTo(EventStatus.UPCOMING); // 기존 상태 반환
    }

    @Test
    @DisplayName("이벤트 참여 가능 여부 확인 - 참여 가능한 이벤트")
    void isEventParticipatable_ParticipatableEvent() {
        // When
        boolean isParticipatable = eventStatusService.isEventParticipatable(ongoingEvent);

        // Then
        assertThat(isParticipatable).isTrue();
    }

    @Test
    @DisplayName("이벤트 참여 가능 여부 확인 - 참여 불가능한 이벤트")
    void isEventParticipatable_NotParticipatableEvent() {
        // When
        boolean isParticipatable = eventStatusService.isEventParticipatable(upcomingEvent);

        // Then
        assertThat(isParticipatable).isFalse();
    }

    @Test
    @DisplayName("이벤트 상태가 최신인지 확인 - 최신 상태")
    void isEventStatusUpToDate_UpToDateStatus() {
        // When
        boolean isUpToDate = eventStatusService.isEventStatusUpToDate(ongoingEvent);

        // Then
        assertThat(isUpToDate).isTrue();
    }

    @Test
    @DisplayName("이벤트 상태가 최신인지 확인 - 최신이 아닌 상태")
    void isEventStatusUpToDate_NotUpToDateStatus() {
        // Given
        Event eventWithWrongStatus = Event.builder()
                .id(6L)
                .type(EventType.BATTLE)
                .status(EventStatus.UPCOMING) // 잘못된 상태
                .eventDetail(ongoingEvent.getEventDetail())
                .build();

        // When
        boolean isUpToDate = eventStatusService.isEventStatusUpToDate(eventWithWrongStatus);

        // Then
        assertThat(isUpToDate).isFalse();
    }

    @Test
    @DisplayName("특정 이벤트 상태 업데이트 - 상태 변경이 필요한 경우")
    void updateEventStatus_StatusChangeNeeded() {
        // Given
        Event eventWithWrongStatus = Event.builder()
                .id(7L)
                .type(EventType.BATTLE)
                .status(EventStatus.UPCOMING) // 잘못된 상태
                .eventDetail(ongoingEvent.getEventDetail())
                .build();

        when(eventRepository.findById(7L)).thenReturn(Optional.of(eventWithWrongStatus));

        // When
        boolean updated = eventStatusService.updateEventStatus(7L);

        // Then
        assertThat(updated).isTrue();
        verify(eventRepository).findById(7L);
    }

    @Test
    @DisplayName("특정 이벤트 상태 업데이트 - 상태 변경이 불필요한 경우")
    void updateEventStatus_NoStatusChangeNeeded() {
        // Given
        when(eventRepository.findById(2L)).thenReturn(Optional.of(ongoingEvent));

        // When
        boolean updated = eventStatusService.updateEventStatus(2L);

        // Then
        assertThat(updated).isFalse();
        verify(eventRepository).findById(2L);
    }

    @Test
    @DisplayName("모든 이벤트 상태 업데이트")
    void updateAllEventStatuses() {
        // Given
        List<Event> events = Arrays.asList(upcomingEvent, ongoingEvent, endedEvent);
        when(eventRepository.findAll()).thenReturn(events);

        // When
        eventStatusService.updateAllEventStatuses();

        // Then
        verify(eventRepository).findAll();
        // 각 이벤트의 상태가 올바르게 설정되었는지 확인
        assertThat(upcomingEvent.getStatus()).isEqualTo(EventStatus.UPCOMING);
        assertThat(ongoingEvent.getStatus()).isEqualTo(EventStatus.ONGOING);
        assertThat(endedEvent.getStatus()).isEqualTo(EventStatus.ENDED);
    }
}
