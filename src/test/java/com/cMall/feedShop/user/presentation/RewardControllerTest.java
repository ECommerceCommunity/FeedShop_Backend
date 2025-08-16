package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.request.RewardGrantRequest;
import com.cMall.feedShop.user.application.dto.response.RewardHistoryResponse;
import com.cMall.feedShop.user.application.dto.response.RewardPolicyResponse;
import com.cMall.feedShop.user.application.service.RewardService;
import com.cMall.feedShop.user.domain.enums.RewardType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RewardController.class)
@DisplayName("RewardController 테스트")
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RewardService rewardService;

    @Nested
    @DisplayName("관리자 포인트 지급 API")
    class GrantPointsByAdmin {

        @Test
        @WithMockUser(authorities = "ADMIN")
        @DisplayName("성공: 관리자 포인트 지급")
        void grantPointsByAdmin_Success() throws Exception {
            // given
            RewardGrantRequest request = createRewardGrantRequest(1L, 1000, "이벤트 참여 보상");
            RewardHistoryResponse response = createRewardHistoryResponse(1L, RewardType.ADMIN_GRANT, 1000);

            given(rewardService.grantPointsByAdmin(any(RewardGrantRequest.class), any(UserDetails.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/rewards/admin/grant")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.historyId").value(1))
                    .andExpect(jsonPath("$.data.rewardType").value("ADMIN_GRANT"))
                    .andExpect(jsonPath("$.data.points").value(1000));
        }

        @Test
        @WithMockUser(authorities = "USER")
        @DisplayName("실패: 권한 부족 (403 Forbidden 예상)")
        void grantPointsByAdmin_Fail_Forbidden() throws Exception {
            // given
            RewardGrantRequest request = createRewardGrantRequest(1L, 1000, "이벤트 참여 보상");

            // when & then
            mockMvc.perform(post("/api/rewards/admin/grant")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        @DisplayName("실패: 유효하지 않은 요청 데이터")
        void grantPointsByAdmin_Fail_InvalidRequest() throws Exception {
            // given - userId가 null인 잘못된 요청
            String invalidRequest = """
                {
                    "userId": null,
                    "points": -100,
                    "description": ""
                }
                """;

            // when & then
            mockMvc.perform(post("/api/rewards/admin/grant")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        @DisplayName("실패: 필수 필드 누락")
        void grantPointsByAdmin_Fail_MissingFields() throws Exception {
            // given - description이 누락된 요청
            String incompleteRequest = """
                {
                    "userId": 1,
                    "points": 1000
                }
                """;

            // when & then
            mockMvc.perform(post("/api/rewards/admin/grant")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(incompleteRequest))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("리워드 히스토리 조회 API")
    class GetRewardHistory {

        @Test
        @WithMockUser
        @DisplayName("성공: 리워드 히스토리 조회")
        void getRewardHistory_Success() throws Exception {
            // given
            List<RewardHistoryResponse> historyList = List.of(
                    createRewardHistoryResponse(1L, RewardType.REVIEW_WRITE, 100),
                    createRewardHistoryResponse(2L, RewardType.EVENT_PARTICIPATION, 500)
            );
            Page<RewardHistoryResponse> historyPage = new PageImpl<>(historyList, PageRequest.of(0, 20), 2);

            given(rewardService.getRewardHistory(any(UserDetails.class), eq(0), eq(20)))
                    .willReturn(historyPage);

            // when & then
            mockMvc.perform(get("/api/rewards/history")
                            .param("page", "0")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.content[0].rewardType").value("REVIEW_WRITE"))
                    .andExpect(jsonPath("$.data.content[1].rewardType").value("EVENT_PARTICIPATION"));
        }

        @Test
        @WithMockUser
        @DisplayName("성공: 기본 페이지 파라미터로 조회")
        void getRewardHistory_Success_DefaultParams() throws Exception {
            // given
            Page<RewardHistoryResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            given(rewardService.getRewardHistory(any(UserDetails.class), eq(0), eq(20)))
                    .willReturn(emptyPage);

            // when & then
            mockMvc.perform(get("/api/rewards/history"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @WithMockUser
        @DisplayName("성공: 사용자 정의 페이지 크기로 조회")
        void getRewardHistory_Success_CustomSize() throws Exception {
            // given
            List<RewardHistoryResponse> historyList = List.of(
                    createRewardHistoryResponse(1L, RewardType.BIRTHDAY, 1000)
            );
            Page<RewardHistoryResponse> historyPage = new PageImpl<>(historyList, PageRequest.of(1, 5), 10);

            given(rewardService.getRewardHistory(any(UserDetails.class), eq(1), eq(5)))
                    .willReturn(historyPage);

            // when & then
            mockMvc.perform(get("/api/rewards/history")
                            .param("page", "1")
                            .param("size", "5"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(10));
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자")
        void getRewardHistory_Fail_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/rewards/history"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("리워드 정책 조회 API")
    class GetRewardPolicies {

        @Test
        @DisplayName("성공: 리워드 정책 목록 조회")
        void getRewardPolicies_Success() throws Exception {
            // given
            List<RewardPolicyResponse> policies = List.of(
                    createRewardPolicyResponse(RewardType.REVIEW_WRITE, 100, "리뷰 작성 보상"),
                    createRewardPolicyResponse(RewardType.EVENT_PARTICIPATION, 500, "이벤트 참여 보상"),
                    createRewardPolicyResponse(RewardType.BIRTHDAY, 1000, "생일 축하 포인트")
            );

            given(rewardService.getRewardPolicies()).willReturn(policies);

            // when & then
            mockMvc.perform(get("/api/rewards/policies"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].rewardType").value("REVIEW_WRITE"))
                    .andExpect(jsonPath("$.data[0].points").value(100))
                    .andExpect(jsonPath("$.data[1].rewardType").value("EVENT_PARTICIPATION"))
                    .andExpect(jsonPath("$.data[1].points").value(500))
                    .andExpect(jsonPath("$.data[2].rewardType").value("BIRTHDAY"))
                    .andExpect(jsonPath("$.data[2].points").value(1000));
        }

        @Test
        @DisplayName("성공: 빈 정책 목록 조회")
        void getRewardPolicies_Success_Empty() throws Exception {
            // given
            given(rewardService.getRewardPolicies()).willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/rewards/policies"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("성공: 인증 없이도 접근 가능한 공개 API")
        void getRewardPolicies_Success_PublicAccess() throws Exception {
            // given
            List<RewardPolicyResponse> policies = List.of(
                    createRewardPolicyResponse(RewardType.REVIEW_WRITE, 100, "리뷰 작성 보상")
            );

            given(rewardService.getRewardPolicies()).willReturn(policies);

            // when & then - 인증 없이 요청
            mockMvc.perform(get("/api/rewards/policies"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("API 응답 형태 검증")
    class ApiResponseValidation {

        @Test
        @WithMockUser(authorities = "ADMIN")
        @DisplayName("성공: API 응답 구조 검증")
        void validateApiResponseStructure() throws Exception {
            // given
            RewardGrantRequest request = createRewardGrantRequest(1L, 1000, "테스트 보상");
            RewardHistoryResponse response = createRewardHistoryResponse(1L, RewardType.ADMIN_GRANT, 1000);

            given(rewardService.grantPointsByAdmin(any(RewardGrantRequest.class), any(UserDetails.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/rewards/admin/grant")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").exists())
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser
        @DisplayName("성공: 페이지네이션 응답 구조 검증")
        void validatePaginationResponseStructure() throws Exception {
            // given
            Page<RewardHistoryResponse> historyPage = new PageImpl<>(
                    List.of(createRewardHistoryResponse(1L, RewardType.REVIEW_WRITE, 100)),
                    PageRequest.of(0, 20), 
                    1
            );

            given(rewardService.getRewardHistory(any(UserDetails.class), anyInt(), anyInt()))
                    .willReturn(historyPage);

            // when & then
            mockMvc.perform(get("/api/rewards/history"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").exists())
                    .andExpect(jsonPath("$.data.pageable").exists())
                    .andExpect(jsonPath("$.data.totalElements").exists())
                    .andExpect(jsonPath("$.data.totalPages").exists())
                    .andExpect(jsonPath("$.data.size").exists())
                    .andExpect(jsonPath("$.data.number").exists());
        }
    }

    // 헬퍼 메서드들
    private RewardGrantRequest createRewardGrantRequest(Long userId, Integer points, String description) {
        // RewardGrantRequest는 모든 필드가 private이고 setter가 없으므로 
        // 실제 구현에서는 Jackson을 통한 역직렬화나 @RequestBody로 생성됨
        // 테스트에서는 ObjectMapper를 사용하여 JSON으로 변환하여 테스트
        return new RewardGrantRequest() {
            @Override
            public Long getUserId() { return userId; }
            @Override
            public Integer getPoints() { return points; }
            @Override
            public String getDescription() { return description; }
        };
    }

    private RewardHistoryResponse createRewardHistoryResponse(Long historyId, RewardType rewardType, Integer points) {
        return RewardHistoryResponse.builder()
                .historyId(historyId)
                .rewardType(rewardType)
                .points(points)
                .description(rewardType.getDescription())
                .isProcessed(true)
                .processedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private RewardPolicyResponse createRewardPolicyResponse(RewardType rewardType, Integer points, String description) {
        return RewardPolicyResponse.builder()
                .policyId(1L)
                .rewardType(rewardType)
                .points(points)
                .description(description)
                .isActive(true)
                .dailyLimit(5)
                .monthlyLimit(20)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(30))
                .build();
    }
}
