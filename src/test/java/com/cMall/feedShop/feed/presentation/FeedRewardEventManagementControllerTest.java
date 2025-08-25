package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.feed.application.dto.request.FeedRewardEventSearchRequest;
import com.cMall.feedShop.feed.application.dto.response.FeedRewardEventResponseDto;
import com.cMall.feedShop.feed.application.service.FeedRewardEventManagementService;
import com.cMall.feedShop.feed.domain.model.FeedRewardEvent;
import com.cMall.feedShop.user.domain.enums.RewardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedRewardEventManagementController 테스트")
class FeedRewardEventManagementControllerTest {

    @Mock
    private FeedRewardEventManagementService feedRewardEventManagementService;

    private FeedRewardEventManagementController controller;

    private FeedRewardEventResponseDto testResponseDto;
    private FeedRewardEventSearchRequest testSearchRequest;

    @BeforeEach
    void setUp() {
        controller = new FeedRewardEventManagementController(feedRewardEventManagementService);
        
        // 테스트 응답 DTO 생성 (빌더 패턴 활용)
        testResponseDto = FeedRewardEventResponseDto.builder()
                .eventId(1L)
                .feedId(1L)
                .feedTitle("테스트 피드")
                .userId(1L)
                .userNickname("테스트 사용자")
                .rewardType(RewardType.FEED_CREATION)
                .rewardTypeDisplayName("피드 생성")
                .eventStatus(FeedRewardEvent.EventStatus.PENDING)
                .eventStatusDisplayName("대기중")
                .points(100)
                .description("테스트 설명")
                .relatedData("{}")
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 테스트 검색 요청 생성 (빌더 패턴 활용)
        testSearchRequest = FeedRewardEventSearchRequest.builder()
                .page(0)
                .size(20)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();
    }

    @Test
    @DisplayName("리워드 이벤트 목록 조회 성공")
    void getRewardEvents_Success() {
        // given
        Page<FeedRewardEventResponseDto> responsePage = new PageImpl<>(List.of(testResponseDto));
        when(feedRewardEventManagementService.getRewardEvents(any(FeedRewardEventSearchRequest.class)))
                .thenReturn(responsePage);

        // when
        ResponseEntity<Page<FeedRewardEventResponseDto>> response = controller.getRewardEvents(testSearchRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getEventId()).isEqualTo(1L);
        assertThat(response.getBody().getContent().get(0).getFeedTitle()).isEqualTo("테스트 피드");

        verify(feedRewardEventManagementService).getRewardEvents(any(FeedRewardEventSearchRequest.class));
    }

    @Test
    @DisplayName("사용자별 리워드 이벤트 조회 성공")
    void getRewardEventsByUser_Success() {
        // given
        when(feedRewardEventManagementService.getRewardEventsByUser(1L))
                .thenReturn(List.of(testResponseDto));

        // when
        ResponseEntity<List<FeedRewardEventResponseDto>> response = controller.getRewardEventsByUser(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getEventId()).isEqualTo(1L);
        assertThat(response.getBody().get(0).getUserId()).isEqualTo(1L);

        verify(feedRewardEventManagementService).getRewardEventsByUser(1L);
    }

    @Test
    @DisplayName("피드별 리워드 이벤트 조회 성공")
    void getRewardEventsByFeed_Success() {
        // given
        when(feedRewardEventManagementService.getRewardEventsByFeed(1L))
                .thenReturn(List.of(testResponseDto));

        // when
        ResponseEntity<List<FeedRewardEventResponseDto>> response = controller.getRewardEventsByFeed(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getEventId()).isEqualTo(1L);
        assertThat(response.getBody().get(0).getFeedId()).isEqualTo(1L);

        verify(feedRewardEventManagementService).getRewardEventsByFeed(1L);
    }

    @Test
    @DisplayName("리워드 이벤트 상세 조회 성공")
    void getRewardEventDetail_Success() {
        // given
        when(feedRewardEventManagementService.getRewardEventDetail(1L))
                .thenReturn(testResponseDto);

        // when
        ResponseEntity<FeedRewardEventResponseDto> response = controller.getRewardEventDetail(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEventId()).isEqualTo(1L);
        assertThat(response.getBody().getFeedTitle()).isEqualTo("테스트 피드");

        verify(feedRewardEventManagementService).getRewardEventDetail(1L);
    }

    @Test
    @DisplayName("리워드 이벤트 통계 조회 성공")
    void getRewardEventStatistics_Success() {
        // given
        Map<String, Object> statistics = Map.of(
                "totalEvents", 10L,
                "pendingEvents", 5L,
                "processedEvents", 3L,
                "failedEvents", 2L,
                "totalPoints", 500
        );
        when(feedRewardEventManagementService.getRewardEventStatistics())
                .thenReturn(statistics);

        // when
        ResponseEntity<Map<String, Object>> response = controller.getRewardEventStatistics();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalEvents")).isEqualTo(10L);
        assertThat(response.getBody().get("pendingEvents")).isEqualTo(5L);
        assertThat(response.getBody().get("totalPoints")).isEqualTo(500);

        verify(feedRewardEventManagementService).getRewardEventStatistics();
    }

    @Test
    @DisplayName("수동 이벤트 처리 성공")
    void processEventManually_Success() {
        // given
        doNothing().when(feedRewardEventManagementService).processEventManually(1L);

        // when
        ResponseEntity<Void> response = controller.processEventManually(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(feedRewardEventManagementService).processEventManually(1L);
    }

    @Test
    @DisplayName("실패한 이벤트 재처리 성공")
    void retryFailedEvents_Success() {
        // given
        doNothing().when(feedRewardEventManagementService).retryFailedEvents();

        // when
        ResponseEntity<Void> response = controller.retryFailedEvents();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(feedRewardEventManagementService).retryFailedEvents();
    }

    @Test
    @DisplayName("리워드 이벤트 요약 정보 조회 성공")
    void getRewardEventSummary_Success() {
        // given
        Map<String, Object> summary = Map.of(
                "totalEvents", 10L,
                "pendingEvents", 5L,
                "processedEvents", 3L,
                "failedEvents", 2L,
                "totalPoints", 500
        );
        when(feedRewardEventManagementService.getRewardEventStatistics())
                .thenReturn(summary);

        // when
        ResponseEntity<Map<String, Object>> response = controller.getRewardEventSummary();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalEvents")).isEqualTo(10L);
        assertThat(response.getBody().get("pendingEvents")).isEqualTo(5L);
        assertThat(response.getBody().get("totalPoints")).isEqualTo(500);

        verify(feedRewardEventManagementService).getRewardEventStatistics();
    }
}
