package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.event.application.exception.EventNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventDeleteService 테스트")
class EventDeleteServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventDeleteService eventDeleteService;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        testEvent = Event.builder()
                .id(1L)
                .type(EventType.BATTLE)
                .status(EventStatus.ONGOING)
                .maxParticipants(100)
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
        verify(eventRepository).delete(argThat(event -> {
            // softDelete 메서드가 호출되어 deletedAt이 설정되었는지 확인
            return event.isDeleted();
        }));
    }
} 