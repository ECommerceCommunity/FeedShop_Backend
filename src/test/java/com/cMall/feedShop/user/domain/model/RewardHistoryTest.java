package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RewardHistory 도메인 테스트")
class RewardHistoryTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .loginId("testuser")
                .email("testuser@example.com")
                .password("password")
                .role(UserRole.USER)
                .build();
    }

    @Nested
    @DisplayName("RewardHistory 생성")
    class CreateRewardHistory {

        @Test
        @DisplayName("성공: 기본 리워드 히스토리 생성")
        void createRewardHistory_Success_Basic() {
            // given & when
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.REVIEW_WRITE)
                    .points(100)
                    .description("리뷰 작성 보상")
                    .build();

            // then
            assertThat(history.getUser()).isEqualTo(testUser);
            assertThat(history.getRewardType()).isEqualTo(RewardType.REVIEW_WRITE);
            assertThat(history.getPoints()).isEqualTo(100);
            assertThat(history.getDescription()).isEqualTo("리뷰 작성 보상");
            assertThat(history.getIsProcessed()).isFalse(); // 기본값 false
            assertThat(history.getProcessedAt()).isNull();
            assertThat(history.getRelatedId()).isNull();
            assertThat(history.getRelatedType()).isNull();
            assertThat(history.getAdminId()).isNull();
        }

        @Test
        @DisplayName("성공: 관련 엔티티 정보를 포함한 리워드 히스토리 생성")
        void createRewardHistory_Success_WithRelatedEntity() {
            // given & when
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.REVIEW_WRITE)
                    .points(100)
                    .description("리뷰 작성 보상")
                    .relatedId(123L)
                    .relatedType("REVIEW")
                    .build();

            // then
            assertThat(history.getUser()).isEqualTo(testUser);
            assertThat(history.getRewardType()).isEqualTo(RewardType.REVIEW_WRITE);
            assertThat(history.getPoints()).isEqualTo(100);
            assertThat(history.getDescription()).isEqualTo("리뷰 작성 보상");
            assertThat(history.getRelatedId()).isEqualTo(123L);
            assertThat(history.getRelatedType()).isEqualTo("REVIEW");
            assertThat(history.getIsProcessed()).isFalse();
        }

        @Test
        @DisplayName("성공: 관리자 지급 리워드 히스토리 생성")
        void createRewardHistory_Success_AdminGrant() {
            // given & when
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.ADMIN_GRANT)
                    .points(1000)
                    .description("이벤트 참여 보상")
                    .adminId(999L)
                    .build();

            // then
            assertThat(history.getUser()).isEqualTo(testUser);
            assertThat(history.getRewardType()).isEqualTo(RewardType.ADMIN_GRANT);
            assertThat(history.getPoints()).isEqualTo(1000);
            assertThat(history.getDescription()).isEqualTo("이벤트 참여 보상");
            assertThat(history.getAdminId()).isEqualTo(999L);
            assertThat(history.getIsProcessed()).isFalse();
        }

        @Test
        @DisplayName("성공: 모든 필드를 포함한 리워드 히스토리 생성")
        void createRewardHistory_Success_AllFields() {
            // given & when
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.EVENT_PARTICIPATION)
                    .points(500)
                    .description("이벤트 참여 보상")
                    .relatedId(456L)
                    .relatedType("EVENT")
                    .adminId(999L)
                    .build();

            // then
            assertThat(history.getUser()).isEqualTo(testUser);
            assertThat(history.getRewardType()).isEqualTo(RewardType.EVENT_PARTICIPATION);
            assertThat(history.getPoints()).isEqualTo(500);
            assertThat(history.getDescription()).isEqualTo("이벤트 참여 보상");
            assertThat(history.getRelatedId()).isEqualTo(456L);
            assertThat(history.getRelatedType()).isEqualTo("EVENT");
            assertThat(history.getAdminId()).isEqualTo(999L);
            assertThat(history.getIsProcessed()).isFalse();
            assertThat(history.getProcessedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("처리 상태 관리")
    class ProcessingStatus {

        @Test
        @DisplayName("성공: 미처리에서 처리 완료로 변경")
        void markAsProcessed_Success() {
            // given
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.BIRTHDAY)
                    .points(1000)
                    .description("생일 축하 포인트")
                    .build();

            assertThat(history.getIsProcessed()).isFalse();
            assertThat(history.getProcessedAt()).isNull();

            // when
            LocalDateTime beforeProcess = LocalDateTime.now();
            history.markAsProcessed();
            LocalDateTime afterProcess = LocalDateTime.now();

            // then
            assertThat(history.getIsProcessed()).isTrue();
            assertThat(history.getProcessedAt()).isNotNull();
            assertThat(history.getProcessedAt()).isBetween(beforeProcess, afterProcess);
        }

        @Test
        @DisplayName("성공: 이미 처리된 히스토리를 다시 처리 완료로 표시")
        void markAsProcessed_Success_AlreadyProcessed() {
            // given
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.FIRST_PURCHASE)
                    .points(2000)
                    .description("첫 구매 보너스")
                    .build();

            history.markAsProcessed();
            LocalDateTime firstProcessedAt = history.getProcessedAt();

            // when - 잠시 대기 후 다시 처리
            try {
                Thread.sleep(10); // 시간 차이를 만들기 위해
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            history.markAsProcessed();

            // then
            assertThat(history.getIsProcessed()).isTrue();
            assertThat(history.getProcessedAt()).isNotEqualTo(firstProcessedAt); // 새로운 시간으로 업데이트
            assertThat(history.getProcessedAt()).isAfter(firstProcessedAt);
        }

        @Test
        @DisplayName("성공: 처리 완료에서 미처리로 변경")
        void markAsUnprocessed_Success() {
            // given
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.REVIEW_PHOTO)
                    .points(150)
                    .description("사진 리뷰 보상")
                    .build();

            history.markAsProcessed(); // 먼저 처리 완료로 변경
            assertThat(history.getIsProcessed()).isTrue();
            assertThat(history.getProcessedAt()).isNotNull();

            // when
            history.markAsUnprocessed();

            // then
            assertThat(history.getIsProcessed()).isFalse();
            assertThat(history.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("성공: 이미 미처리인 히스토리를 다시 미처리로 표시")
        void markAsUnprocessed_Success_AlreadyUnprocessed() {
            // given
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.COMPENSATION)
                    .points(300)
                    .description("보상 포인트")
                    .build();

            assertThat(history.getIsProcessed()).isFalse();
            assertThat(history.getProcessedAt()).isNull();

            // when
            history.markAsUnprocessed();

            // then
            assertThat(history.getIsProcessed()).isFalse();
            assertThat(history.getProcessedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("리워드 타입별 테스트")
    class RewardTypeTests {

        @Test
        @DisplayName("성공: 리뷰 관련 리워드 히스토리")
        void createRewardHistory_Success_ReviewRewards() {
            // given & when
            RewardHistory normalReview = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.REVIEW_WRITE)
                    .points(100)
                    .description("일반 리뷰 작성")
                    .relatedId(1L)
                    .relatedType("REVIEW")
                    .build();

            RewardHistory photoReview = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.REVIEW_PHOTO)
                    .points(150)
                    .description("사진 리뷰 작성")
                    .relatedId(2L)
                    .relatedType("REVIEW")
                    .build();

            RewardHistory qualityReview = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.REVIEW_QUALITY)
                    .points(200)
                    .description("고품질 리뷰 작성")
                    .relatedId(3L)
                    .relatedType("REVIEW")
                    .build();

            // then
            assertThat(normalReview.getRewardType()).isEqualTo(RewardType.REVIEW_WRITE);
            assertThat(normalReview.getPoints()).isEqualTo(100);
            
            assertThat(photoReview.getRewardType()).isEqualTo(RewardType.REVIEW_PHOTO);
            assertThat(photoReview.getPoints()).isEqualTo(150);
            
            assertThat(qualityReview.getRewardType()).isEqualTo(RewardType.REVIEW_QUALITY);
            assertThat(qualityReview.getPoints()).isEqualTo(200);
        }

        @Test
        @DisplayName("성공: 이벤트 관련 리워드 히스토리")
        void createRewardHistory_Success_EventRewards() {
            // given & when
            RewardHistory participation = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.EVENT_PARTICIPATION)
                    .points(500)
                    .description("이벤트 참여 보상")
                    .relatedId(10L)
                    .relatedType("EVENT")
                    .build();

            RewardHistory winner = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.EVENT_WINNER)
                    .points(5000)
                    .description("이벤트 당첨 보상")
                    .relatedId(10L)
                    .relatedType("EVENT")
                    .build();

            // then
            assertThat(participation.getRewardType()).isEqualTo(RewardType.EVENT_PARTICIPATION);
            assertThat(participation.getPoints()).isEqualTo(500);
            
            assertThat(winner.getRewardType()).isEqualTo(RewardType.EVENT_WINNER);
            assertThat(winner.getPoints()).isEqualTo(5000);
        }

        @Test
        @DisplayName("성공: 특별 이벤트 리워드 히스토리")
        void createRewardHistory_Success_SpecialRewards() {
            // given & when
            RewardHistory birthday = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.BIRTHDAY)
                    .points(1000)
                    .description("생일 축하 포인트")
                    .build();

            RewardHistory firstPurchase = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.FIRST_PURCHASE)
                    .points(2000)
                    .description("첫 구매 보너스")
                    .relatedId(100L)
                    .relatedType("ORDER")
                    .build();

            // then
            assertThat(birthday.getRewardType()).isEqualTo(RewardType.BIRTHDAY);
            assertThat(birthday.getPoints()).isEqualTo(1000);
            assertThat(birthday.getRelatedId()).isNull();
            assertThat(birthday.getRelatedType()).isNull();
            
            assertThat(firstPurchase.getRewardType()).isEqualTo(RewardType.FIRST_PURCHASE);
            assertThat(firstPurchase.getPoints()).isEqualTo(2000);
            assertThat(firstPurchase.getRelatedId()).isEqualTo(100L);
            assertThat(firstPurchase.getRelatedType()).isEqualTo("ORDER");
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCases {

        @Test
        @DisplayName("성공: 0 포인트 리워드 히스토리")
        void createRewardHistory_Success_ZeroPoints() {
            // given & when
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.ADJUSTMENT)
                    .points(0)
                    .description("포인트 조정")
                    .build();

            // then
            assertThat(history.getPoints()).isEqualTo(0);
            assertThat(history.getRewardType()).isEqualTo(RewardType.ADJUSTMENT);
        }

        @Test
        @DisplayName("성공: 음수 포인트 리워드 히스토리 (포인트 차감)")
        void createRewardHistory_Success_NegativePoints() {
            // given & when
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.ADJUSTMENT)
                    .points(-500)
                    .description("포인트 차감")
                    .build();

            // then
            assertThat(history.getPoints()).isEqualTo(-500);
            assertThat(history.getRewardType()).isEqualTo(RewardType.ADJUSTMENT);
        }

        @Test
        @DisplayName("성공: 매우 큰 포인트 값")
        void createRewardHistory_Success_LargePoints() {
            // given & when
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.COMPENSATION)
                    .points(Integer.MAX_VALUE)
                    .description("최대 포인트 보상")
                    .build();

            // then
            assertThat(history.getPoints()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("성공: 빈 설명문")
        void createRewardHistory_Success_EmptyDescription() {
            // given & when
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.ADMIN_GRANT)
                    .points(100)
                    .description("")
                    .build();

            // then
            assertThat(history.getDescription()).isEqualTo("");
        }

        @Test
        @DisplayName("성용: null 설명문")
        void createRewardHistory_Success_NullDescription() {
            // given & when
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.ADMIN_GRANT)
                    .points(100)
                    .description(null)
                    .build();

            // then
            assertThat(history.getDescription()).isNull();
        }

        @Test
        @DisplayName("성공: 매우 긴 설명문")
        void createRewardHistory_Success_LongDescription() {
            // given
            String longDescription = "a".repeat(500); // 500자 설명문

            // when
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.COMPENSATION)
                    .points(1000)
                    .description(longDescription)
                    .build();

            // then
            assertThat(history.getDescription()).isEqualTo(longDescription);
            assertThat(history.getDescription().length()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("상태 변경 시나리오")
    class StatusChangeScenarios {

        @Test
        @DisplayName("성공: 처리 상태 여러 번 변경")
        void statusChange_Success_MultipleChanges() {
            // given
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.REFERRAL)
                    .points(300)
                    .description("추천인 보상")
                    .build();

            // 초기 상태 확인
            assertThat(history.getIsProcessed()).isFalse();
            assertThat(history.getProcessedAt()).isNull();

            // when & then - 여러 번 상태 변경
            // 1. 처리 완료
            history.markAsProcessed();
            assertThat(history.getIsProcessed()).isTrue();
            assertThat(history.getProcessedAt()).isNotNull();
            LocalDateTime firstProcessed = history.getProcessedAt();

            // 2. 미처리로 변경
            history.markAsUnprocessed();
            assertThat(history.getIsProcessed()).isFalse();
            assertThat(history.getProcessedAt()).isNull();

            // 3. 다시 처리 완료
            history.markAsProcessed();
            assertThat(history.getIsProcessed()).isTrue();
            assertThat(history.getProcessedAt()).isNotNull();
            assertThat(history.getProcessedAt()).isAfter(firstProcessed);
        }

        @Test
        @DisplayName("성공: 처리 시간 정확성 검증")
        void statusChange_Success_ProcessedTimeAccuracy() {
            // given
            RewardHistory history = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.REFERRED)
                    .points(200)
                    .description("피추천인 보상")
                    .build();

            // when
            LocalDateTime beforeMark = LocalDateTime.now();
            history.markAsProcessed();
            LocalDateTime afterMark = LocalDateTime.now();

            // then
            assertThat(history.getProcessedAt()).isNotNull();
            assertThat(history.getProcessedAt()).isBetween(beforeMark, afterMark);
            assertThat(history.getProcessedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }
    }
}
