package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.event.application.dto.request.EventResultCreateRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventResultResponseDto;
import com.cMall.feedShop.event.application.service.EventResultManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventResultControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EventResultManagementService eventResultManagementService;

    @InjectMocks
    private EventResultController eventResultController;

    private ObjectMapper objectMapper;
    private EventResultResponseDto testEventResultResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventResultController).build();
        objectMapper = new ObjectMapper();

        // 테스트 응답 DTO 설정
        EventResultResponseDto.EventResultDetailResponseDto detailDto = 
                EventResultResponseDto.EventResultDetailResponseDto.builder()
                        .id(1L)
                        .userId(1L)
                        .userName("테스트 사용자")
                        .feedId(1L)
                        .feedTitle("테스트 피드")
                        .rankPosition(1)
                        .voteCount(10L)
                        .pointsEarned(1000)
                        .badgePointsEarned(50)
                        .couponCode("TEST_COUPON")
                        .couponDescription("테스트 쿠폰")
                        .rewardProcessed(false)
                        .rewardProcessedAt(null)
                        .build();

        testEventResultResponse = EventResultResponseDto.builder()
                .id(1L)
                .eventId(1L)
                .eventTitle("테스트 이벤트")
                .resultType("BATTLE_WINNER")
                .announcedAt(LocalDateTime.now())
                .totalParticipants(1)
                .totalVotes(10L)
                .resultDetails(Arrays.asList(detailDto))
                .build();
    }

    @Test
    @DisplayName("이벤트 결과 생성 API - 성공")
    void createEventResult_Success() throws Exception {
        // given
        EventResultCreateRequestDto requestDto = EventResultCreateRequestDto.builder()
                .forceRecalculate(false)
                .build();

        when(eventResultManagementService.createEventResult(any(EventResultCreateRequestDto.class)))
                .thenReturn(testEventResultResponse);

        // when & then
        mockMvc.perform(post("/api/v2/events/1/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이벤트 결과가 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.eventId").value(1))
                .andExpect(jsonPath("$.data.resultType").value("BATTLE_WINNER"));

        verify(eventResultManagementService).createEventResult(any(EventResultCreateRequestDto.class));
    }

    @Test
    @DisplayName("이벤트 결과 조회 API - 성공")
    void getEventResult_Success() throws Exception {
        // given
        when(eventResultManagementService.getEventResult(1L))
                .thenReturn(testEventResultResponse);

        // when & then
        mockMvc.perform(get("/api/v2/events/1/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이벤트 결과를 성공적으로 조회했습니다."))
                .andExpect(jsonPath("$.data.eventId").value(1))
                .andExpect(jsonPath("$.data.resultDetails").isArray())
                .andExpect(jsonPath("$.data.resultDetails[0].rankPosition").value(1));

        verify(eventResultManagementService).getEventResult(1L);
    }

    @Test
    @DisplayName("이벤트 결과 존재 여부 확인 API - 성공")
    void hasEventResult_Success() throws Exception {
        // given
        when(eventResultManagementService.hasEventResult(1L))
                .thenReturn(true);

        // when & then
        mockMvc.perform(get("/api/v2/events/1/results/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이벤트 결과 존재 여부를 확인했습니다."))
                .andExpect(jsonPath("$.data").value(true));

        verify(eventResultManagementService).hasEventResult(1L);
    }

    @Test
    @DisplayName("이벤트 결과 삭제 API - 성공")
    void deleteEventResult_Success() throws Exception {
        // given
        doNothing().when(eventResultManagementService).deleteEventResult(1L);

        // when & then
        mockMvc.perform(delete("/api/v2/events/1/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이벤트 결과가 성공적으로 삭제되었습니다."));

        verify(eventResultManagementService).deleteEventResult(1L);
    }

    @Test
    @DisplayName("이벤트 결과 재계산 API - 성공")
    void recalculateEventResult_Success() throws Exception {
        // given
        when(eventResultManagementService.recalculateEventResult(1L))
                .thenReturn(testEventResultResponse);

        // when & then
        mockMvc.perform(post("/api/v2/events/1/results/recalculate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이벤트 결과가 성공적으로 재계산되었습니다."))
                .andExpect(jsonPath("$.data.eventId").value(1));

        verify(eventResultManagementService).recalculateEventResult(1L);
    }
}
