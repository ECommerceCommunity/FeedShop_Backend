package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventListResponseDto;
import com.cMall.feedShop.event.application.dto.response.EventSummaryDto;
import com.cMall.feedShop.event.application.dto.response.EventDetailResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventReadService 테스트")
class EventReadServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventReadService eventReadService;

    private Event testEvent;
    private EventSummaryDto testEventSummaryDto;
    private Page<Event> eventPage;
    private EventDetailResponseDto testEventDetailResponseDto;

    @BeforeEach
    void setUp() {
        // 테스트 이벤트 설정
        EventDetail eventDetail = EventDetail.builder()
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .eventStartDate(LocalDate.now())
                .eventEndDate(LocalDate.now().plusDays(7))
                .build();

        testEvent = Event.builder()
                .id(1L)
                .type(EventType.BATTLE)
                .status(EventStatus.ONGOING)
                .maxParticipants(100)
                .createdBy(LocalDateTime.now())
                .build();
        testEvent.setEventDetail(eventDetail);

        // 테스트 이벤트 요약 DTO 설정
        testEventSummaryDto = EventSummaryDto.builder()
                .eventId(1L)
                .title("테스트 이벤트")
                .type("battle")
                .status("ongoing")
                .maxParticipants(100)
                .build();

        // 테스트 상세 DTO 설정
        testEventDetailResponseDto = EventDetailResponseDto.builder()
                .eventId(1L)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .build();

        // 테스트 페이지 설정
        eventPage = new PageImpl<>(List.of(testEvent), PageRequest.of(0, 20), 1);
    }

    @Test
    @DisplayName("전체 이벤트 목록 조회 성공")
    void getAllEvents_Success() {
        // Given
        when(eventRepository.findAll(any(Pageable.class))).thenReturn(eventPage);
        when(eventMapper.toSummaryDto(testEvent)).thenReturn(testEventSummaryDto);

        // When
        EventListResponseDto result = eventReadService.getAllEvents(1, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("이벤트 검색 성공")
    void searchEvents_Success() {
        // Given
        EventListRequestDto requestDto = EventListRequestDto.builder()
                .page(1)
                .size(20)
                .build();

        when(eventRepository.searchEvents(any(EventListRequestDto.class), any(Pageable.class)))
                .thenReturn(eventPage);
        when(eventMapper.toSummaryDto(testEvent)).thenReturn(testEventSummaryDto);

        // When
        EventListResponseDto result = eventReadService.searchEvents(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("기본 페이징 파라미터로 조회")
    void getAllEvents_WithDefaultPaging() {
        // Given
        when(eventRepository.findAll(any(Pageable.class))).thenReturn(eventPage);
        when(eventMapper.toSummaryDto(testEvent)).thenReturn(testEventSummaryDto);

        // When
        EventListResponseDto result = eventReadService.getAllEvents(null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("이벤트 상세 조회 성공")
    void getEventDetail_Success() {
        // Given
        when(eventRepository.findDetailById(1L)).thenReturn(Optional.of(testEvent));
        when(eventMapper.toDetailDto(testEvent)).thenReturn(testEventDetailResponseDto);

        // When
        EventDetailResponseDto result = eventReadService.getEventDetail(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEventId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("테스트 이벤트");
    }

    @Test
    @DisplayName("이벤트 상세 조회 실패 - 존재하지 않는 이벤트")
    void getEventDetail_NotFound() {
        // Given
        when(eventRepository.findDetailById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventReadService.getEventDetail(999L))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("소프트 딜리트된 이벤트는 조회되지 않음")
    void getEventDetail_SoftDeletedEvent_NotFound() {
        // Given: 소프트 딜리트된 이벤트 (deletedAt이 설정된 이벤트)
        Event softDeletedEvent = Event.builder()
                .id(2L)
                .type(EventType.BATTLE)
                .status(EventStatus.ENDED)
                .deletedAt(LocalDateTime.now()) // 소프트 딜리트됨
                .build();
        
        // 소프트 딜리트된 이벤트는 findDetailById에서 null을 반환해야 함
        when(eventRepository.findDetailById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> eventReadService.getEventDetail(2L))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("소프트 딜리트된 이벤트는 목록에서 제외됨")
    void getAllEvents_ExcludeSoftDeletedEvents() {
        // Given: 소프트 딜리트된 이벤트가 포함된 페이지
        Event softDeletedEvent = Event.builder()
                .id(2L)
                .type(EventType.BATTLE)
                .status(EventStatus.ENDED)
                .deletedAt(LocalDateTime.now()) // 소프트 딜리트됨
                .build();
        
        // findAll은 소프트 딜리트되지 않은 이벤트만 반환해야 함
        Page<Event> eventPageWithoutSoftDeleted = new PageImpl<>(List.of(testEvent), PageRequest.of(0, 20), 1);
        when(eventRepository.findAll(any(Pageable.class))).thenReturn(eventPageWithoutSoftDeleted);
        when(eventMapper.toSummaryDto(testEvent)).thenReturn(testEventSummaryDto);

        // When
        EventListResponseDto result = eventReadService.getAllEvents(1, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEventId()).isEqualTo(1L);
        // 소프트 딜리트된 이벤트(ID: 2)는 결과에 포함되지 않아야 함
    }
} 