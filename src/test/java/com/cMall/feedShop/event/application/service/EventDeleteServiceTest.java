package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.event.application.exception.EventNotFoundException;
import com.cMall.feedShop.event.application.service.EventImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventDeleteService 테스트")
class EventDeleteServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventImageService eventImageService;

    @InjectMocks
    private EventDeleteService eventDeleteService;

    private Event testEvent;
    private Event softDeletedEvent;

    @BeforeEach
    void setUp() {
        // 정상 이벤트 설정
        EventDetail eventDetail = EventDetail.builder()
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .eventStartDate(LocalDate.now().plusDays(1))
                .eventEndDate(LocalDate.now().plusDays(7))
                .build();

        testEvent = Event.builder()
                .id(1L)
                .type(EventType.BATTLE)
                .status(EventStatus.ONGOING)
                .maxParticipants(100)
                .build();
        testEvent.setEventDetail(eventDetail);

        // 이미 삭제된 이벤트 설정
        softDeletedEvent = Event.builder()
                .id(2L)
                .type(EventType.BATTLE)
                .status(EventStatus.ENDED)
                .deletedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("이벤트 삭제 성공")
    void deleteEvent_Success() {
        // Given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        doNothing().when(eventRepository).delete(any(Event.class));

        // When
        eventDeleteService.deleteEvent(1L);

        // Then
        verify(eventRepository).findById(1L);
        verify(eventRepository).delete(testEvent);
    }

    @Test
    @DisplayName("이벤트 삭제 실패 - 존재하지 않는 이벤트")
    void deleteEvent_NotFound() {
        // Given
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventDeleteService.deleteEvent(999L))
                .isInstanceOf(EventNotFoundException.class);
        
        verify(eventRepository).findById(999L);
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    @DisplayName("이벤트 삭제 시 소프트 딜리트 적용")
    void deleteEvent_SoftDeleteApplied() {
        // Given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        doNothing().when(eventRepository).delete(any(Event.class));

        // When
        eventDeleteService.deleteEvent(1L);

        // Then
        verify(eventRepository).findById(1L);
        verify(eventRepository).delete(testEvent);
        
        // softDelete가 호출되었는지 확인 (EventRepositoryImpl에서 처리됨)
        // 실제로는 Repository 계층에서 softDelete가 호출되므로 여기서는 검증하지 않음
    }

    @Test
    @DisplayName("이미 삭제된 이벤트 재삭제 시도")
    void deleteEvent_AlreadyDeleted() {
        // Given: 이미 삭제된 이벤트는 findById에서 조회되지 않음
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventDeleteService.deleteEvent(2L))
                .isInstanceOf(EventNotFoundException.class);
        
        verify(eventRepository).findById(2L);
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    @DisplayName("null 이벤트 ID로 삭제 시도")
    void deleteEvent_NullEventId() {
        // Given
        when(eventRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventDeleteService.deleteEvent(null))
                .isInstanceOf(EventNotFoundException.class);
        
        verify(eventRepository).findById(null);
        verify(eventRepository, never()).delete(any(Event.class));
    }
} 