package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventUpdateRequestDto;
import com.cMall.feedShop.event.application.exception.EventNotFoundException;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.event.domain.repository.EventRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventUpdateService 테스트")
class EventUpdateServiceTest {
    @Mock
    private EventRepository eventRepository;
    @InjectMocks
    private EventUpdateService eventUpdateService;

    private Event event;
    private EventDetail eventDetail;

    @BeforeEach
    void setUp() {
        eventDetail = EventDetail.builder()
                .title("이전 제목")
                .description("이전 설명")
                .eventStartDate(LocalDate.now())
                .eventEndDate(LocalDate.now().plusDays(7))
                .build();
        event = Event.builder()
                .id(1L)
                .type(EventType.BATTLE)
                .status(EventStatus.ONGOING)
                .maxParticipants(100)
                .eventDetail(eventDetail)
                .build();
        event.setEventDetail(eventDetail);
    }

    @Test
    @DisplayName("이벤트 수정 성공")
    void updateEvent_Success() {
        // Given
        EventUpdateRequestDto dto = EventUpdateRequestDto.builder()
                .eventId(1L)
                .title("수정된 제목")
                .description("수정된 설명")
                .build();
        when(eventRepository.findDetailById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        eventUpdateService.updateEvent(dto);

        // Then
        assertThat(event.getEventDetail().getTitle()).isEqualTo("수정된 제목");
        assertThat(event.getEventDetail().getDescription()).isEqualTo("수정된 설명");
        verify(eventRepository).save(event);
    }

    @Test
    @DisplayName("이벤트 수정 실패 - 존재하지 않는 이벤트")
    void updateEvent_NotFound() {
        // Given
        EventUpdateRequestDto dto = EventUpdateRequestDto.builder()
                .eventId(2L)
                .title("수정된 제목")
                .build();
        when(eventRepository.findDetailById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventUpdateService.updateEvent(dto))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("2");
    }

    @Test
    @DisplayName("이벤트 타입 수정 성공")
    void updateEvent_TypeUpdate_Success() {
        // Given
        EventUpdateRequestDto dto = EventUpdateRequestDto.builder()
                .eventId(1L)
                .type(EventType.MISSION)
                .build();
        when(eventRepository.findDetailById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        eventUpdateService.updateEvent(dto);

        // Then
        assertThat(event.getType()).isEqualTo(EventType.MISSION);
        verify(eventRepository).save(event);
    }

    @Test
    @DisplayName("최대 참여자 수 수정 성공")
    void updateEvent_MaxParticipantsUpdate_Success() {
        // Given
        EventUpdateRequestDto dto = EventUpdateRequestDto.builder()
                .eventId(1L)
                .maxParticipants(200)
                .build();
        when(eventRepository.findDetailById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        eventUpdateService.updateEvent(dto);

        // Then
        assertThat(event.getMaxParticipants()).isEqualTo(200);
        verify(eventRepository).save(event);
    }

    @Test
    @DisplayName("소프트 딜리트된 이벤트 수정 실패")
    void updateEvent_SoftDeletedEvent_NotFound() {
        // Given: 소프트 딜리트된 이벤트
        Event softDeletedEvent = Event.builder()
                .id(3L)
                .type(EventType.BATTLE)
                .status(EventStatus.ENDED)
                .deletedAt(LocalDateTime.now()) // 소프트 딜리트됨
                .build();
        
        EventUpdateRequestDto dto = EventUpdateRequestDto.builder()
                .eventId(3L)
                .title("수정된 제목")
                .build();
        when(eventRepository.findDetailById(3L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventUpdateService.updateEvent(dto))
                .isInstanceOf(EventNotFoundException.class);
    }
} 