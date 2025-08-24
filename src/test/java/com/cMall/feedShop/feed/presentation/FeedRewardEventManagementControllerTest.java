package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.feed.application.dto.request.FeedRewardEventSearchRequest;
import com.cMall.feedShop.feed.application.dto.response.FeedRewardEventResponseDto;
import com.cMall.feedShop.feed.application.service.FeedRewardEventManagementService;
import com.cMall.feedShop.feed.domain.model.FeedRewardEvent;
import com.cMall.feedShop.user.domain.enums.RewardType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(FeedRewardEventManagementController.class)
@DisplayName("FeedRewardEventManagementController 테스트")
class FeedRewardEventManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedRewardEventManagementService feedRewardEventManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private FeedRewardEventResponseDto testResponseDto;
    private FeedRewardEventSearchRequest testSearchRequest;

    @BeforeEach
    void setUp() {
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
    @WithMockUser(roles = "ADMIN")
    void getRewardEvents_Success() throws Exception {
        // given
        Page<FeedRewardEventResponseDto> responsePage = new PageImpl<>(List.of(testResponseDto));
        when(feedRewardEventManagementService.getRewardEvents(any(FeedRewardEventSearchRequest.class)))
                .thenReturn(responsePage);

        // when & then
        mockMvc.perform(get("/api/admin/feed-reward-events")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].eventId").value(1))
                .andExpect(jsonPath("$.content[0].feedTitle").value("테스트 피드"));

        verify(feedRewardEventManagementService).getRewardEvents(any(FeedRewardEventSearchRequest.class));
    }

    @Test
    @DisplayName("리워드 이벤트 목록 조회 실패 - 잘못된 파라미터")
    @WithMockUser(roles = "ADMIN")
    void getRewardEvents_Failure_InvalidParameters() throws Exception {
        // given
        when(feedRewardEventManagementService.getRewardEvents(any(FeedRewardEventSearchRequest.class)))
                .thenThrow(new IllegalArgumentException("잘못된 요청 파라미터입니다"));

        // when & then
        mockMvc.perform(get("/api/admin/feed-reward-events")
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(feedRewardEventManagementService).getRewardEvents(any(FeedRewardEventSearchRequest.class));
    }

    @Test
    @DisplayName("사용자별 리워드 이벤트 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getRewardEventsByUser_Success() throws Exception {
        // given
        when(feedRewardEventManagementService.getRewardEventsByUser(1L))
                .thenReturn(List.of(testResponseDto));

        // when & then
        mockMvc.perform(get("/api/admin/feed-reward-events/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value(1))
                .andExpect(jsonPath("$[0].userId").value(1));

        verify(feedRewardEventManagementService).getRewardEventsByUser(1L);
    }

    @Test
    @DisplayName("피드별 리워드 이벤트 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getRewardEventsByFeed_Success() throws Exception {
        // given
        when(feedRewardEventManagementService.getRewardEventsByFeed(1L))
                .thenReturn(List.of(testResponseDto));

        // when & then
        mockMvc.perform(get("/api/admin/feed-reward-events/feed/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value(1))
                .andExpect(jsonPath("$[0].feedId").value(1));

        verify(feedRewardEventManagementService).getRewardEventsByFeed(1L);
    }

    @Test
    @DisplayName("리워드 이벤트 상세 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getRewardEventDetail_Success() throws Exception {
        // given
        when(feedRewardEventManagementService.getRewardEventDetail(1L))
                .thenReturn(testResponseDto);

        // when & then
        mockMvc.perform(get("/api/admin/feed-reward-events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.feedTitle").value("테스트 피드"));

        verify(feedRewardEventManagementService).getRewardEventDetail(1L);
    }

    @Test
    @DisplayName("리워드 이벤트 상세 조회 실패 - 이벤트 없음")
    @WithMockUser(roles = "ADMIN")
    void getRewardEventDetail_Failure_EventNotFound() throws Exception {
        // given
        when(feedRewardEventManagementService.getRewardEventDetail(1L))
                .thenThrow(new RuntimeException("리워드 이벤트를 찾을 수 없습니다: 1"));

        // when & then
        mockMvc.perform(get("/api/admin/feed-reward-events/1"))
                .andExpect(status().isNotFound());

        verify(feedRewardEventManagementService).getRewardEventDetail(1L);
    }

    @Test
    @DisplayName("리워드 이벤트 통계 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getRewardEventStatistics_Success() throws Exception {
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

        // when & then
        mockMvc.perform(get("/api/admin/feed-reward-events/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(10))
                .andExpect(jsonPath("$.pendingEvents").value(5))
                .andExpect(jsonPath("$.totalPoints").value(500));

        verify(feedRewardEventManagementService).getRewardEventStatistics();
    }

    @Test
    @DisplayName("일별 리워드 이벤트 통계 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getDailyRewardEventStatistics_Success() throws Exception {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        Map<String, Object> dailyStatistics = Map.of(
                "dailyCreatedEvents", Map.of("2024-01-01", 5L),
                "dailyProcessedEvents", Map.of("2024-01-01", 3L),
                "dailyPoints", Map.of("2024-01-01", 300)
        );
        
        when(feedRewardEventManagementService.getDailyRewardEventStatistics(any(), any()))
                .thenReturn(dailyStatistics);

        // when & then
        mockMvc.perform(get("/api/admin/feed-reward-events/statistics/daily")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCreatedEvents").exists())
                .andExpect(jsonPath("$.dailyProcessedEvents").exists())
                .andExpect(jsonPath("$.dailyPoints").exists());

        verify(feedRewardEventManagementService).getDailyRewardEventStatistics(any(), any());
    }

    @Test
    @DisplayName("수동 이벤트 처리 성공")
    @WithMockUser(roles = "ADMIN")
    void processEventManually_Success() throws Exception {
        // given
        doNothing().when(feedRewardEventManagementService).processEventManually(1L);

        // when & then
        mockMvc.perform(post("/api/admin/feed-reward-events/1/process"))
                .andExpect(status().isOk());

        verify(feedRewardEventManagementService).processEventManually(1L);
    }

    @Test
    @DisplayName("수동 이벤트 처리 실패 - 이벤트 처리 실패")
    @WithMockUser(roles = "ADMIN")
    void processEventManually_Failure_ProcessingFailed() throws Exception {
        // given
        doThrow(new RuntimeException("리워드 이벤트 처리에 실패했습니다"))
                .when(feedRewardEventManagementService).processEventManually(1L);

        // when & then
        mockMvc.perform(post("/api/admin/feed-reward-events/1/process"))
                .andExpect(status().isInternalServerError());

        verify(feedRewardEventManagementService).processEventManually(1L);
    }

    @Test
    @DisplayName("실패한 이벤트 재처리 성공")
    @WithMockUser(roles = "ADMIN")
    void retryFailedEvents_Success() throws Exception {
        // given
        doNothing().when(feedRewardEventManagementService).retryFailedEvents();

        // when & then
        mockMvc.perform(post("/api/admin/feed-reward-events/retry-failed"))
                .andExpect(status().isOk());

        verify(feedRewardEventManagementService).retryFailedEvents();
    }

    @Test
    @DisplayName("리워드 이벤트 요약 정보 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getRewardEventSummary_Success() throws Exception {
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

        // when & then
        mockMvc.perform(get("/api/admin/feed-reward-events/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(10))
                .andExpect(jsonPath("$.pendingEvents").value(5))
                .andExpect(jsonPath("$.totalPoints").value(500));

        verify(feedRewardEventManagementService).getRewardEventStatistics();
    }

    @Test
    @DisplayName("권한 없음 - 일반 사용자 접근")
    @WithMockUser(roles = "USER")
    void accessDenied_UserRole() throws Exception {
        // when & then
        mockMvc.perform(get("/api/admin/feed-reward-events"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("권한 없음 - 인증되지 않은 사용자")
    void accessDenied_Unauthenticated() throws Exception {
        // when & then
        mockMvc.perform(get("/api/admin/feed-reward-events"))
                .andExpect(status().isUnauthorized());
    }
}
