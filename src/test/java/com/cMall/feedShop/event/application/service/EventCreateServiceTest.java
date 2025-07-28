package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventCreateResponseDto;
import com.cMall.feedShop.event.application.exception.InvalidEventTypeException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventCreateService 테스트")
class EventCreateServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventValidator eventValidator;

    @InjectMocks
    private EventCreateService eventCreateService;

    private EventCreateRequestDto validRequestDto;
    private Event savedEvent;

    @BeforeEach
    void setUp() {
        // 유효한 요청 DTO 설정
        validRequestDto = EventCreateRequestDto.builder()
                .type(EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .imageUrl("https://example.com/image.jpg")
                .participationMethod("참여 방법")
                .selectionCriteria("선정 기준")
                .precautions("주의사항")
                .purchaseStartDate(LocalDate.now())
                .purchaseEndDate(LocalDate.now().plusDays(7))
                .eventStartDate(LocalDate.now().plusDays(1))
                .eventEndDate(LocalDate.now().plusDays(8))
                .announcement(LocalDate.now().plusDays(9))
                .maxParticipants(100)
                .rewards("🥇 1등: 프리미엄 스니커즈 (가치 30만원)\n🥈 2등: 트렌디한 운동화 (가치 15만원)\n🥉 3등: 스타일리시한 슈즈 (가치 8만원)")
                .build();

        // 저장된 이벤트 설정
        EventDetail eventDetail = EventDetail.builder()
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .rewards("🥇 1등: 프리미엄 스니커즈 (가치 30만원)\n🥈 2등: 트렌디한 운동화 (가치 15만원)\n🥉 3등: 스타일리시한 슈즈 (가치 8만원)")
                .build();

        savedEvent = Event.builder()
                .id(1L)
                .type(EventType.BATTLE)
                .status(EventStatus.UPCOMING) // 자동 계산된 상태
                .maxParticipants(100)
                .createdBy(LocalDateTime.now())
                .build();
        savedEvent.setEventDetail(eventDetail);
    }

    @Test
    @DisplayName("이벤트 생성 성공")
    void createEvent_Success() {
        // Given
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // When
        EventCreateResponseDto result = eventCreateService.createEvent(validRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEventId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("테스트 이벤트");
        assertThat(result.getType()).isEqualTo("battle");
        assertThat(result.getStatus()).isEqualTo("upcoming");
        assertThat(result.getMaxParticipants()).isEqualTo(100);
    }

    @Test
    @DisplayName("검증 실패 시 예외 발생")
    void createEvent_ValidationFailure() {
        // Given
        doThrow(new InvalidEventTypeException())
                .when(eventValidator).validateEventCreateRequest(validRequestDto);

        // When & Then
        assertThatThrownBy(() -> eventCreateService.createEvent(validRequestDto))
                .isInstanceOf(InvalidEventTypeException.class);
    }

    @Test
    @DisplayName("리워드가 없는 이벤트 생성")
    void createEvent_WithoutRewards() {
        // Given
        validRequestDto = EventCreateRequestDto.builder()
                .type(EventType.BATTLE)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .maxParticipants(100)
                .rewards(null)
                .build();
        
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // When
        EventCreateResponseDto result = eventCreateService.createEvent(validRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEventId()).isEqualTo(1L);
    }
} 