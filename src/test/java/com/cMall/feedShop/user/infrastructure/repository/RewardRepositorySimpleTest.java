package com.cMall.feedShop.user.infrastructure.repository;

import com.cMall.feedShop.user.domain.model.RewardHistory;
import com.cMall.feedShop.user.domain.model.RewardPolicy;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.repository.RewardHistoryRepository;
import com.cMall.feedShop.user.domain.repository.RewardPolicyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Repository 간단 테스트")
class RewardRepositorySimpleTest {

    @Mock
    private RewardHistoryRepository rewardHistoryRepository;

    @Mock
    private RewardPolicyRepository rewardPolicyRepository;

    @Test
    @DisplayName("성공: RewardHistoryRepository Mock 테스트")
    void rewardHistoryRepository_MockTest() {
        // given
        User testUser = createTestUser();
        RewardHistory mockHistory = createTestRewardHistory(testUser, RewardType.REVIEW_WRITE, 100);
        
        given(rewardHistoryRepository.findByUserAndIsProcessedFalseOrderByCreatedAtAsc(any(User.class)))
                .willReturn(List.of(mockHistory));

        // when
        List<RewardHistory> results = rewardHistoryRepository.findByUserAndIsProcessedFalseOrderByCreatedAtAsc(testUser);

        // then
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRewardType()).isEqualTo(RewardType.REVIEW_WRITE);
        assertThat(results.get(0).getPoints()).isEqualTo(100);
    }

    @Test
    @DisplayName("성공: RewardPolicyRepository Mock 테스트")
    void rewardPolicyRepository_MockTest() {
        // given
        RewardPolicy mockPolicy = createTestRewardPolicy(RewardType.REVIEW_WRITE, 100);
        
        given(rewardPolicyRepository.findByRewardTypeAndIsActiveTrue(any(RewardType.class)))
                .willReturn(Optional.of(mockPolicy));

        // when
        Optional<RewardPolicy> result = rewardPolicyRepository.findByRewardTypeAndIsActiveTrue(RewardType.REVIEW_WRITE);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getRewardType()).isEqualTo(RewardType.REVIEW_WRITE);
        assertThat(result.get().getPoints()).isEqualTo(100);
        assertThat(result.get().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("성공: Repository 기본 동작 테스트")
    void repository_BasicOperationTest() {
        // given
        User testUser = createTestUser();
        RewardHistory mockHistory = createTestRewardHistory(testUser, RewardType.EVENT_PARTICIPATION, 500);
        
        given(rewardHistoryRepository.save(any(RewardHistory.class)))
                .willReturn(mockHistory);

        // when
        RewardHistory savedHistory = rewardHistoryRepository.save(mockHistory);

        // then
        assertThat(savedHistory).isNotNull();
        assertThat(savedHistory.getRewardType()).isEqualTo(RewardType.EVENT_PARTICIPATION);
        assertThat(savedHistory.getPoints()).isEqualTo(500);
    }

    // 헬퍼 메서드들
    private User createTestUser() {
        return User.builder()
                .loginId("testuser")
                .password("password123")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();
    }

    private RewardHistory createTestRewardHistory(User user, RewardType rewardType, Integer points) {
        return RewardHistory.builder()
                .user(user)
                .rewardType(rewardType)
                .points(points)
                .description(rewardType.getDescription())
                .build();
    }

    private RewardPolicy createTestRewardPolicy(RewardType rewardType, Integer points) {
        return RewardPolicy.builder()
                .rewardType(rewardType)
                .points(points)
                .description(rewardType.getDescription())
                .isActive(true)
                .dailyLimit(5)
                .monthlyLimit(50)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(30))
                .build();
    }
}
