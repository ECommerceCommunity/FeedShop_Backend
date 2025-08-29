package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventListResponseDto;
import com.cMall.feedShop.event.application.dto.response.EventDetailResponseDto;
import com.cMall.feedShop.event.application.dto.response.EventSummaryDto;
import com.cMall.feedShop.event.application.service.EventReadService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventReadController 테스트")
class EventReadControllerTest {

    @Mock
    private EventReadService eventReadService;

    @InjectMocks
    private EventReadController eventReadController;

    private EventListResponseDto testEventListResponse;
    private EventDetailResponseDto testEventDetailResponse;
    private List<EventSummaryDto> testEventSummaryList;

    @BeforeEach
    void setUp() {
        // 테스트용 이벤트 목록 응답 생성
        testEventListResponse = EventListResponseDto.builder()
                .content(Arrays.asList(
                    EventSummaryDto.builder()
                        .eventId(1L)
                        .title("테스트 이벤트 1")
                        .type("RANKING")
                        .build(),
                    EventSummaryDto.builder()
                        .eventId(2L)
                        .title("테스트 이벤트 2")
                        .type("BATTLE")
                        .build()
                ))
                .totalElements(2L)
                .totalPages(1)
                .page(0)
                .size(10)
                .build();

        // 테스트용 이벤트 상세 응답 생성
        testEventDetailResponse = EventDetailResponseDto.builder()
                .eventId(1L)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .type("RANKING")
                .status("ONGOING")
                .eventStartDate("2024-01-01")
                .eventEndDate("2024-12-31")
                .maxParticipants(100)
                .build();

        // 테스트용 이벤트 요약 목록 생성
        testEventSummaryList = Arrays.asList(
            EventSummaryDto.builder()
                .eventId(1L)
                .title("테스트 이벤트 1")
                .type("RANKING")
                .build(),
            EventSummaryDto.builder()
                .eventId(2L)
                .title("테스트 이벤트 2")
                .type("BATTLE")
                .build()
        );
    }

    @Test
    @DisplayName("전체 이벤트 목록 조회 - 성공")
    void getAllEvents_Success() {
        // given
        given(eventReadService.getAllEvents(anyInt(), anyInt(), anyString()))
                .willReturn(testEventListResponse);

        // when
        EventListResponseDto response = eventReadController.getAllEvents(0, 10, "createdAt,desc");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2L);
        assertThat(response.getPage()).isEqualTo(0);

        verify(eventReadService).getAllEvents(0, 10, "createdAt,desc");
    }

    @Test
    @DisplayName("전체 이벤트 목록 조회 - 기본값 사용")
    void getAllEvents_WithDefaultValues() {
        // given
        given(eventReadService.getAllEvents(any(), any(), any()))
                .willReturn(testEventListResponse);

        // when
        EventListResponseDto response = eventReadController.getAllEvents(null, null, null);

        // then
        assertThat(response).isNotNull();
        verify(eventReadService).getAllEvents(null, null, null);
    }

    @Test
    @DisplayName("이벤트 검색/필터/정렬 - 성공")
    void searchEvents_Success() {
        // given
        EventListRequestDto requestDto = EventListRequestDto.builder()
                .keyword("테스트")
                .type("RANKING")
                .status("ONGOING")
                .page(0)
                .size(10)
                .build();

        given(eventReadService.searchEvents(any(EventListRequestDto.class)))
                .willReturn(testEventListResponse);

        // when
        EventListResponseDto response = eventReadController.searchEvents(requestDto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2L);

        verify(eventReadService).searchEvents(requestDto);
    }

    @Test
    @DisplayName("이벤트 상세 조회 - 성공")
    void getEventDetail_Success() {
        // given
        Long eventId = 1L;
        given(eventReadService.getEventDetail(eventId))
                .willReturn(testEventDetailResponse);

        // when
        EventDetailResponseDto response = eventReadController.getEventDetail(eventId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEventId()).isEqualTo(eventId);
        assertThat(response.getTitle()).isEqualTo("테스트 이벤트");
        assertThat(response.getType()).isEqualTo("RANKING");
        assertThat(response.getStatus()).isEqualTo("ONGOING");

        verify(eventReadService).getEventDetail(eventId);
    }

    @Test
    @DisplayName("피드 참여 가능 이벤트 목록 조회 - 성공")
    void getFeedAvailableEvents_Success() {
        // given
        given(eventReadService.getFeedAvailableEvents())
                .willReturn(testEventSummaryList);

        // when
        ApiResponse<List<EventSummaryDto>> response = eventReadController.getFeedAvailableEvents();

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).getEventId()).isEqualTo(1L);
        assertThat(response.getData().get(1).getEventId()).isEqualTo(2L);

        verify(eventReadService).getFeedAvailableEvents();
    }
}
