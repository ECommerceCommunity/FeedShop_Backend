package com.cMall.feedShop.user.infrastructure.repository;

import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.RewardHistory;
import com.cMall.feedShop.user.domain.model.RewardPolicy;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.RewardHistoryRepository;
import com.cMall.feedShop.user.domain.repository.RewardPolicyRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Reward Repository 테스트")
class RewardRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RewardHistoryRepository rewardHistoryRepository;

    @Autowired
    private RewardPolicyRepository rewardPolicyRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;
    private RewardPolicy reviewPolicy;
    private RewardPolicy eventPolicy;
    private RewardPolicy expiredPolicy;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .loginId("testuser")
                .email("test@example.com")
                .password("password")
                .role(UserRole.USER)
                .build();
        testUser = userRepository.save(testUser);

        anotherUser = User.builder()
                .loginId("anotheruser")
                .email("another@example.com")
                .password("password")
                .role(UserRole.USER)
                .build();
        anotherUser = userRepository.save(anotherUser);

        // 테스트 정책 생성
        reviewPolicy = RewardPolicy.builder()
                .rewardType(RewardType.REVIEW_WRITE)
                .points(100)
                .description("리뷰 작성 보상")
                .isActive(true)
                .dailyLimit(5)
                .monthlyLimit(20)
                .build();
        reviewPolicy = rewardPolicyRepository.save(reviewPolicy);

        eventPolicy = RewardPolicy.builder()
                .rewardType(RewardType.EVENT_PARTICIPATION)
                .points(500)
                .description("이벤트 참여 보상")
                .isActive(true)
                .dailyLimit(3)
                .monthlyLimit(10)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(30))
                .build();
        eventPolicy = rewardPolicyRepository.save(eventPolicy);

        expiredPolicy = RewardPolicy.builder()
                .rewardType(RewardType.BIRTHDAY)
                .points(1000)
                .description("만료된 생일 정책")
                .isActive(true)
                .validFrom(LocalDateTime.now().minusDays(30))
                .validTo(LocalDateTime.now().minusDays(1)) // 어제 만료
                .build();
        expiredPolicy = rewardPolicyRepository.save(expiredPolicy);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("RewardHistoryRepository 테스트")
    class RewardHistoryRepositoryTest {

        @Test
        @DisplayName("성공: 사용자별 리워드 히스토리 조회")
        void findByUserOrderByCreatedAtDesc_Success() {
            // given
            RewardHistory history1 = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 100, "리뷰1");
            RewardHistory history2 = createRewardHistory(testUser, RewardType.EVENT_PARTICIPATION, 500, "이벤트1");
            RewardHistory history3 = createRewardHistory(anotherUser, RewardType.REVIEW_WRITE, 100, "다른유저 리뷰");

            rewardHistoryRepository.saveAll(List.of(history1, history2, history3));
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<RewardHistory> result = rewardHistoryRepository.findByUserOrderByCreatedAtDesc(testUser, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(RewardHistory::getUser)
                    .containsOnly(testUser);
            // 최신순 정렬 확인
            assertThat(result.getContent().get(0).getCreatedAt())
                    .isAfterOrEqualTo(result.getContent().get(1).getCreatedAt());
        }

        @Test
        @DisplayName("성공: 사용자의 특정 타입 리워드 히스토리 조회")
        void findByUserAndRewardTypeOrderByCreatedAtDesc_Success() {
            // given
            RewardHistory history1 = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 100, "리뷰1");
            RewardHistory history2 = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 100, "리뷰2");
            RewardHistory history3 = createRewardHistory(testUser, RewardType.EVENT_PARTICIPATION, 500, "이벤트1");

            rewardHistoryRepository.saveAll(List.of(history1, history2, history3));
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<RewardHistory> result = rewardHistoryRepository
                    .findByUserAndRewardTypeOrderByCreatedAtDesc(testUser, RewardType.REVIEW_WRITE, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(RewardHistory::getRewardType)
                    .containsOnly(RewardType.REVIEW_WRITE);
        }

        @Test
        @DisplayName("성공: 특정 기간 리워드 히스토리 조회")
        void findByUserAndCreatedAtBetweenOrderByCreatedAtDesc_Success() {
            // given
            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);

            RewardHistory history1 = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 100, "최근 리뷰");
            RewardHistory history2 = createRewardHistory(testUser, RewardType.EVENT_PARTICIPATION, 500, "최근 이벤트");

            rewardHistoryRepository.saveAll(List.of(history1, history2));
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<RewardHistory> result = rewardHistoryRepository
                    .findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(testUser, startDate, endDate, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allSatisfy(history -> {
                assertThat(history.getCreatedAt()).isBetween(startDate, endDate);
                assertThat(history.getUser()).isEqualTo(testUser);
            });
        }

        @Test
        @DisplayName("성공: 미처리 리워드 조회")
        void findByUserAndIsProcessedFalseOrderByCreatedAtAsc_Success() {
            // given
            RewardHistory processedHistory = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 100, "처리됨");
            processedHistory.markAsProcessed();

            RewardHistory unprocessedHistory1 = createRewardHistory(testUser, RewardType.EVENT_PARTICIPATION, 500, "미처리1");
            RewardHistory unprocessedHistory2 = createRewardHistory(testUser, RewardType.BIRTHDAY, 1000, "미처리2");

            rewardHistoryRepository.saveAll(List.of(processedHistory, unprocessedHistory1, unprocessedHistory2));
            entityManager.flush();

            // when
            List<RewardHistory> result = rewardHistoryRepository
                    .findByUserAndIsProcessedFalseOrderByCreatedAtAsc(testUser);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(RewardHistory::getIsProcessed)
                    .containsOnly(false);
            // 오래된 순 정렬 확인
            assertThat(result.get(0).getCreatedAt())
                    .isBeforeOrEqualTo(result.get(1).getCreatedAt());
        }

        @Test
        @DisplayName("성공: 특정 관련 엔티티의 리워드 히스토리 조회")
        void findByRelatedIdAndRelatedTypeAndRewardType_Success() {
            // given
            Long reviewId = 123L;
            RewardHistory history = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 100, "리뷰 보상");
            history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.REVIEW_WRITE)
                    .points(100)
                    .description("리뷰 보상")
                    .relatedId(reviewId)
                    .relatedType("REVIEW")
                    .build();

            rewardHistoryRepository.save(history);
            entityManager.flush();

            // when
            Optional<RewardHistory> result = rewardHistoryRepository
                    .findByRelatedIdAndRelatedTypeAndRewardType(reviewId, "REVIEW", RewardType.REVIEW_WRITE);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getRelatedId()).isEqualTo(reviewId);
            assertThat(result.get().getRelatedType()).isEqualTo("REVIEW");
            assertThat(result.get().getRewardType()).isEqualTo(RewardType.REVIEW_WRITE);
        }

        @Test
        @DisplayName("성공: 일일 리워드 획득 횟수 조회")
        void countDailyRewardsByUserAndType_Success() {
            // given
            LocalDateTime today = LocalDateTime.now();
            LocalDateTime yesterday = today.minusDays(1);

            // 오늘 리뷰 보상 2개
            RewardHistory todayReview1 = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 100, "오늘 리뷰1");
            RewardHistory todayReview2 = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 100, "오늘 리뷰2");
            
            // 어제 리뷰 보상 1개 (카운트에 포함되지 않아야 함)
            RewardHistory yesterdayReview = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 100, "어제 리뷰");
            setCreatedAt(yesterdayReview, yesterday);

            rewardHistoryRepository.saveAll(List.of(todayReview1, todayReview2, yesterdayReview));
            entityManager.flush();

            // when
            Long count = rewardHistoryRepository
                    .countDailyRewardsByUserAndType(testUser, RewardType.REVIEW_WRITE, today);

            // then
            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("성공: 월간 리워드 획득 횟수 조회")
        void countMonthlyRewardsByUserAndType_Success() {
            // given
            LocalDateTime thisMonth = LocalDateTime.now();
            LocalDateTime lastMonth = thisMonth.minusMonths(1);

            // 이번 달 이벤트 보상 3개
            RewardHistory thisMonthEvent1 = createRewardHistory(testUser, RewardType.EVENT_PARTICIPATION, 500, "이번달 이벤트1");
            RewardHistory thisMonthEvent2 = createRewardHistory(testUser, RewardType.EVENT_PARTICIPATION, 500, "이번달 이벤트2");
            RewardHistory thisMonthEvent3 = createRewardHistory(testUser, RewardType.EVENT_PARTICIPATION, 500, "이번달 이벤트3");
            
            // 지난 달 이벤트 보상 1개 (카운트에 포함되지 않아야 함)
            RewardHistory lastMonthEvent = createRewardHistory(testUser, RewardType.EVENT_PARTICIPATION, 500, "지난달 이벤트");
            setCreatedAt(lastMonthEvent, lastMonth);

            rewardHistoryRepository.saveAll(List.of(thisMonthEvent1, thisMonthEvent2, thisMonthEvent3, lastMonthEvent));
            entityManager.flush();

            // when
            Long count = rewardHistoryRepository
                    .countMonthlyRewardsByUserAndType(testUser, RewardType.EVENT_PARTICIPATION, thisMonth);

            // then
            assertThat(count).isEqualTo(3L);
        }

        @Test
        @DisplayName("성공: 일일 리워드 포인트 합계 조회")
        void sumDailyRewardPointsByUserAndType_Success() {
            // given
            LocalDateTime today = LocalDateTime.now();

            RewardHistory review1 = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 100, "리뷰1");
            RewardHistory review2 = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 150, "리뷰2");
            RewardHistory event = createRewardHistory(testUser, RewardType.EVENT_PARTICIPATION, 500, "이벤트");

            rewardHistoryRepository.saveAll(List.of(review1, review2, event));
            entityManager.flush();

            // when
            Integer sum = rewardHistoryRepository
                    .sumDailyRewardPointsByUserAndType(testUser, RewardType.REVIEW_WRITE, today);

            // then
            assertThat(sum).isEqualTo(250); // 100 + 150
        }

        @Test
        @DisplayName("성공: 관리자가 지급한 리워드 조회")
        void findByAdminIdOrderByCreatedAtDesc_Success() {
            // given
            Long adminId = 999L;
            
            RewardHistory adminReward1 = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.ADMIN_GRANT)
                    .points(1000)
                    .description("관리자 지급1")
                    .adminId(adminId)
                    .build();
            
            RewardHistory adminReward2 = RewardHistory.builder()
                    .user(anotherUser)
                    .rewardType(RewardType.ADMIN_GRANT)
                    .points(2000)
                    .description("관리자 지급2")
                    .adminId(adminId)
                    .build();
            
            RewardHistory otherAdminReward = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.ADMIN_GRANT)
                    .points(500)
                    .description("다른 관리자 지급")
                    .adminId(888L)
                    .build();

            rewardHistoryRepository.saveAll(List.of(adminReward1, adminReward2, otherAdminReward));
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<RewardHistory> result = rewardHistoryRepository.findByAdminIdOrderByCreatedAtDesc(adminId, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(RewardHistory::getAdminId)
                    .containsOnly(adminId);
        }

        @Test
        @DisplayName("성공: 전체 미처리 리워드 조회")
        void findByIsProcessedFalseOrderByCreatedAtAsc_Success() {
            // given
            RewardHistory processed = createRewardHistory(testUser, RewardType.REVIEW_WRITE, 100, "처리됨");
            processed.markAsProcessed();

            RewardHistory unprocessed1 = createRewardHistory(testUser, RewardType.EVENT_PARTICIPATION, 500, "미처리1");
            RewardHistory unprocessed2 = createRewardHistory(anotherUser, RewardType.BIRTHDAY, 1000, "미처리2");

            rewardHistoryRepository.saveAll(List.of(processed, unprocessed1, unprocessed2));
            entityManager.flush();

            // when
            List<RewardHistory> result = rewardHistoryRepository.findByIsProcessedFalseOrderByCreatedAtAsc();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(RewardHistory::getIsProcessed)
                    .containsOnly(false);
        }
    }

    @Nested
    @DisplayName("RewardPolicyRepository 테스트")
    class RewardPolicyRepositoryTest {

        @Test
        @DisplayName("성공: 활성화된 정책 조회")
        void findByIsActiveTrue_Success() {
            // given
            RewardPolicy inactivePolicy = RewardPolicy.builder()
                    .rewardType(RewardType.FIRST_PURCHASE)
                    .points(2000)
                    .description("비활성화된 정책")
                    .isActive(false)
                    .build();
            rewardPolicyRepository.save(inactivePolicy);
            entityManager.flush();

            // when
            List<RewardPolicy> result = rewardPolicyRepository.findByIsActiveTrue();

            // then
            assertThat(result).hasSize(2); // reviewPolicy, eventPolicy (expiredPolicy는 활성화되어 있지만 만료됨)
            assertThat(result).extracting(RewardPolicy::getIsActive)
                    .containsOnly(true);
        }

        @Test
        @DisplayName("성공: 특정 타입의 활성화된 정책 조회")
        void findByRewardTypeAndIsActiveTrue_Success() {
            // when
            Optional<RewardPolicy> result = rewardPolicyRepository
                    .findByRewardTypeAndIsActiveTrue(RewardType.REVIEW_WRITE);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getRewardType()).isEqualTo(RewardType.REVIEW_WRITE);
            assertThat(result.get().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("실패: 비활성화된 정책 조회")
        void findByRewardTypeAndIsActiveTrue_Fail_Inactive() {
            // given
            RewardPolicy inactivePolicy = RewardPolicy.builder()
                    .rewardType(RewardType.COMPENSATION)
                    .points(300)
                    .description("비활성화된 보상 정책")
                    .isActive(false)
                    .build();
            rewardPolicyRepository.save(inactivePolicy);
            entityManager.flush();

            // when
            Optional<RewardPolicy> result = rewardPolicyRepository
                    .findByRewardTypeAndIsActiveTrue(RewardType.COMPENSATION);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공: 유효한 정책들 조회")
        void findValidPolicies_Success() {
            // when
            List<RewardPolicy> result = rewardPolicyRepository.findValidPolicies();

            // then
            assertThat(result).hasSize(2); // reviewPolicy, eventPolicy (expiredPolicy 제외)
            assertThat(result).extracting(RewardPolicy::getRewardType)
                    .containsExactlyInAnyOrder(RewardType.REVIEW_WRITE, RewardType.EVENT_PARTICIPATION);
            
            // 모든 정책이 현재 유효한지 확인
            assertThat(result).allSatisfy(policy -> assertThat(policy.isValid()).isTrue());
        }

        @Test
        @DisplayName("성공: 특정 타입의 유효한 정책 조회")
        void findValidPolicyByType_Success() {
            // when
            Optional<RewardPolicy> result = rewardPolicyRepository
                    .findValidPolicyByType(RewardType.EVENT_PARTICIPATION);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getRewardType()).isEqualTo(RewardType.EVENT_PARTICIPATION);
            assertThat(result.get().isValid()).isTrue();
        }

        @Test
        @DisplayName("실패: 만료된 정책 조회")
        void findValidPolicyByType_Fail_Expired() {
            // when
            Optional<RewardPolicy> result = rewardPolicyRepository
                    .findValidPolicyByType(RewardType.BIRTHDAY);

            // then
            assertThat(result).isEmpty(); // expiredPolicy는 만료되어 조회되지 않음
        }

        @Test
        @DisplayName("성공: 미래 시작일 정책은 유효하지 않음")
        void findValidPolicies_Success_FutureStartDate() {
            // given
            RewardPolicy futurePolicy = RewardPolicy.builder()
                    .rewardType(RewardType.REFERRAL)
                    .points(300)
                    .description("미래 시작 정책")
                    .isActive(true)
                    .validFrom(LocalDateTime.now().plusDays(1)) // 내일부터 시작
                    .build();
            rewardPolicyRepository.save(futurePolicy);
            entityManager.flush();

            // when
            List<RewardPolicy> result = rewardPolicyRepository.findValidPolicies();

            // then
            assertThat(result).hasSize(2); // futurePolicy는 포함되지 않음
            assertThat(result).extracting(RewardPolicy::getRewardType)
                    .doesNotContain(RewardType.REFERRAL);
        }
    }

    // 헬퍼 메서드들
    private RewardHistory createRewardHistory(User user, RewardType rewardType, Integer points, String description) {
        return RewardHistory.builder()
                .user(user)
                .rewardType(rewardType)
                .points(points)
                .description(description)
                .build();
    }

    private void setCreatedAt(RewardHistory history, LocalDateTime createdAt) {
        // 테스트를 위해 ReflectionTestUtils 또는 @TestPropertySource 등을 사용할 수 있지만
        // 여기서는 간단히 데이터베이스에 저장 후 직접 수정하는 방식을 사용
        entityManager.persistAndFlush(history);
        entityManager.getEntityManager().createQuery("UPDATE RewardHistory rh SET rh.createdAt = :createdAt WHERE rh.historyId = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", history.getHistoryId())
                .executeUpdate();
        entityManager.refresh(history);
    }
}
