package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.user.application.dto.request.RewardGrantRequest;
import com.cMall.feedShop.user.application.dto.response.RewardHistoryResponse;
import com.cMall.feedShop.user.application.dto.response.RewardPolicyResponse;
import com.cMall.feedShop.user.application.service.RewardService;
import com.cMall.feedShop.user.domain.enums.RewardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RewardController 간단 테스트")
class RewardControllerSimpleTest {

    @Mock
    private RewardService rewardService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private RewardController rewardController;

    @BeforeEach
    void setUp() {
        given(userDetails.getUsername()).willReturn("testuser");
    }

    @Test
    @DisplayName("성공: 관리자 포인트 지급")
    void grantPointsByAdmin_Success() {
        // given
        RewardGrantRequest request = createRewardGrantRequest(1L, 1000, "이벤트 참여 보상");
        RewardHistoryResponse expectedResponse = createRewardHistoryResponse(1L, RewardType.ADMIN_GRANT, 1000);

        given(rewardService.grantPointsByAdmin(any(RewardGrantRequest.class), any(UserDetails.class)))
                .willReturn(expectedResponse);

        // when
        var result = rewardController.grantPointsByAdmin(request, userDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getHistoryId()).isEqualTo(1L);
        assertThat(result.getData().getRewardType()).isEqualTo(RewardType.ADMIN_GRANT);
        assertThat(result.getData().getPoints()).isEqualTo(1000);
    }

    @Test
    @DisplayName("성공: 리워드 히스토리 조회")
    void getRewardHistory_Success() {
        // given
        List<RewardHistoryResponse> historyList = List.of(
                createRewardHistoryResponse(1L, RewardType.REVIEW_WRITE, 100),
                createRewardHistoryResponse(2L, RewardType.EVENT_PARTICIPATION, 500)
        );
        Page<RewardHistoryResponse> historyPage = new PageImpl<>(historyList, PageRequest.of(0, 20), 2);

        given(rewardService.getRewardHistory(any(UserDetails.class), eq(0), eq(20)))
                .willReturn(historyPage);

        // when
        var result = rewardController.getRewardHistory(userDetails, 0, 20);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getContent()).hasSize(2);
        assertThat(result.getData().getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("성공: 리워드 정책 조회")
    void getRewardPolicies_Success() {
        // given
        List<RewardPolicyResponse> policies = List.of(
                createRewardPolicyResponse(RewardType.REVIEW_WRITE, 100, "리뷰 작성 보상"),
                createRewardPolicyResponse(RewardType.EVENT_PARTICIPATION, 500, "이벤트 참여 보상")
        );

        given(rewardService.getRewardPolicies()).willReturn(policies);

        // when
        var result = rewardController.getRewardPolicies();

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).getRewardType()).isEqualTo(RewardType.REVIEW_WRITE);
        assertThat(result.getData().get(1).getRewardType()).isEqualTo(RewardType.EVENT_PARTICIPATION);
    }

    // 헬퍼 메서드들
    private RewardGrantRequest createRewardGrantRequest(Long userId, Integer points, String description) {
        RewardGrantRequest mockRequest = new RewardGrantRequest() {
            @Override
            public Long getUserId() { return userId; }
            @Override
            public Integer getPoints() { return points; }
            @Override
            public String getDescription() { return description; }
        };
        return mockRequest;
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
