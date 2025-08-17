package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.user.domain.enums.RewardType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RewardPolicy 도메인 테스트")
class RewardPolicyTest {

    @Nested
    @DisplayName("RewardPolicy 생성")
    class CreateRewardPolicy {

        @Test
        @DisplayName("성공: 기본 정책 생성")
        void createRewardPolicy_Success() {
            // given & when
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.REVIEW_WRITE)
                    .points(100)
                    .description("리뷰 작성 보상")
                    .build();

            // then
            assertThat(policy.getRewardType()).isEqualTo(RewardType.REVIEW_WRITE);
            assertThat(policy.getPoints()).isEqualTo(100);
            assertThat(policy.getDescription()).isEqualTo("리뷰 작성 보상");
            assertThat(policy.getIsActive()).isTrue(); // 기본값 true
        }

        @Test
        @DisplayName("성공: 전체 옵션을 포함한 정책 생성")
        void createRewardPolicy_Success_WithAllOptions() {
            // given
            LocalDateTime validFrom = LocalDateTime.now().minusDays(1);
            LocalDateTime validTo = LocalDateTime.now().plusDays(30);

            // when
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.EVENT_PARTICIPATION)
                    .points(500)
                    .description("이벤트 참여 보상")
                    .isActive(true)
                    .dailyLimit(3)
                    .monthlyLimit(10)
                    .validFrom(validFrom)
                    .validTo(validTo)
                    .build();

            // then
            assertThat(policy.getRewardType()).isEqualTo(RewardType.EVENT_PARTICIPATION);
            assertThat(policy.getPoints()).isEqualTo(500);
            assertThat(policy.getDescription()).isEqualTo("이벤트 참여 보상");
            assertThat(policy.getIsActive()).isTrue();
            assertThat(policy.getDailyLimit()).isEqualTo(3);
            assertThat(policy.getMonthlyLimit()).isEqualTo(10);
            assertThat(policy.getValidFrom()).isEqualTo(validFrom);
            assertThat(policy.getValidTo()).isEqualTo(validTo);
        }

        @Test
        @DisplayName("성공: isActive가 null일 때 기본값 true 적용")
        void createRewardPolicy_Success_DefaultIsActive() {
            // given & when
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.BIRTHDAY)
                    .points(1000)
                    .description("생일 축하 포인트")
                    .isActive(null) // null 값 전달
                    .build();

            // then
            assertThat(policy.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("성공: isActive가 false로 설정")
        void createRewardPolicy_Success_InactivePolicy() {
            // given & when
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.FIRST_PURCHASE)
                    .points(2000)
                    .description("첫 구매 보너스")
                    .isActive(false)
                    .build();

            // then
            assertThat(policy.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("정책 유효성 검증")
    class ValidatePolicy {

        @Test
        @DisplayName("성공: 활성화된 정책이고 유효 기간 내")
        void isValid_Success_ActiveAndWithinPeriod() {
            // given
            LocalDateTime validFrom = LocalDateTime.now().minusDays(1);
            LocalDateTime validTo = LocalDateTime.now().plusDays(30);

            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.REVIEW_WRITE)
                    .points(100)
                    .description("리뷰 작성 보상")
                    .isActive(true)
                    .validFrom(validFrom)
                    .validTo(validTo)
                    .build();

            // when & then
            assertThat(policy.isValid()).isTrue();
        }

        @Test
        @DisplayName("성공: 활성화된 정책이고 유효 기간 제한 없음")
        void isValid_Success_ActiveWithoutPeriod() {
            // given
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.BIRTHDAY)
                    .points(1000)
                    .description("생일 축하 포인트")
                    .isActive(true)
                    .build(); // validFrom, validTo 없음

            // when & then
            assertThat(policy.isValid()).isTrue();
        }

        @Test
        @DisplayName("실패: 비활성화된 정책")
        void isValid_Fail_InactivePolicy() {
            // given
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.REVIEW_WRITE)
                    .points(100)
                    .description("리뷰 작성 보상")
                    .isActive(false)
                    .build();

            // when & then
            assertThat(policy.isValid()).isFalse();
        }

        @Test
        @DisplayName("실패: 유효 시작 일자 이전")
        void isValid_Fail_BeforeValidFrom() {
            // given
            LocalDateTime validFrom = LocalDateTime.now().plusDays(1); // 내일부터 유효
            LocalDateTime validTo = LocalDateTime.now().plusDays(30);

            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.EVENT_PARTICIPATION)
                    .points(500)
                    .description("이벤트 참여 보상")
                    .isActive(true)
                    .validFrom(validFrom)
                    .validTo(validTo)
                    .build();

            // when & then
            assertThat(policy.isValid()).isFalse();
        }

        @Test
        @DisplayName("실패: 유효 종료 일자 이후")
        void isValid_Fail_AfterValidTo() {
            // given
            LocalDateTime validFrom = LocalDateTime.now().minusDays(30);
            LocalDateTime validTo = LocalDateTime.now().minusDays(1); // 어제까지 유효

            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.FIRST_PURCHASE)
                    .points(2000)
                    .description("첫 구매 보너스")
                    .isActive(true)
                    .validFrom(validFrom)
                    .validTo(validTo)
                    .build();

            // when & then
            assertThat(policy.isValid()).isFalse();
        }

        @Test
        @DisplayName("성공: validFrom만 설정되고 현재 시간이 이후")
        void isValid_Success_OnlyValidFrom() {
            // given
            LocalDateTime validFrom = LocalDateTime.now().minusHours(1);

            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.REVIEW_PHOTO)
                    .points(150)
                    .description("사진 리뷰 보상")
                    .isActive(true)
                    .validFrom(validFrom)
                    .build(); // validTo 없음

            // when & then
            assertThat(policy.isValid()).isTrue();
        }

        @Test
        @DisplayName("성공: validTo만 설정되고 현재 시간이 이전")
        void isValid_Success_OnlyValidTo() {
            // given
            LocalDateTime validTo = LocalDateTime.now().plusHours(1);

            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.REVIEW_QUALITY)
                    .points(200)
                    .description("고품질 리뷰 보상")
                    .isActive(true)
                    .validTo(validTo)
                    .build(); // validFrom 없음

            // when & then
            assertThat(policy.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("정책 업데이트")
    class UpdatePolicy {

        @Test
        @DisplayName("성공: 모든 필드 업데이트")
        void updatePolicy_Success_AllFields() {
            // given
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.REVIEW_WRITE)
                    .points(100)
                    .description("기존 설명")
                    .isActive(true)
                    .dailyLimit(5)
                    .monthlyLimit(20)
                    .build();

            LocalDateTime newValidFrom = LocalDateTime.now();
            LocalDateTime newValidTo = LocalDateTime.now().plusDays(60);

            // when
            policy.updatePolicy(
                    200, // 새로운 포인트
                    "업데이트된 설명",
                    false, // 비활성화
                    10, // 새로운 일일 제한
                    50, // 새로운 월간 제한
                    newValidFrom,
                    newValidTo
            );

            // then
            assertThat(policy.getPoints()).isEqualTo(200);
            assertThat(policy.getDescription()).isEqualTo("업데이트된 설명");
            assertThat(policy.getIsActive()).isFalse();
            assertThat(policy.getDailyLimit()).isEqualTo(10);
            assertThat(policy.getMonthlyLimit()).isEqualTo(50);
            assertThat(policy.getValidFrom()).isEqualTo(newValidFrom);
            assertThat(policy.getValidTo()).isEqualTo(newValidTo);
        }

        @Test
        @DisplayName("성공: 일부 필드만 업데이트 (null 값 무시)")
        void updatePolicy_Success_PartialUpdate() {
            // given
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.EVENT_PARTICIPATION)
                    .points(500)
                    .description("기존 설명")
                    .isActive(true)
                    .dailyLimit(3)
                    .monthlyLimit(10)
                    .build();

            // when - 포인트와 설명만 업데이트, 나머지는 null
            policy.updatePolicy(
                    1000, // 포인트만 변경
                    "새로운 설명", // 설명만 변경
                    null, // isActive 유지
                    null, // dailyLimit 유지
                    null, // monthlyLimit 유지
                    null, // validFrom 유지
                    null  // validTo 유지
            );

            // then
            assertThat(policy.getPoints()).isEqualTo(1000); // 변경됨
            assertThat(policy.getDescription()).isEqualTo("새로운 설명"); // 변경됨
            assertThat(policy.getIsActive()).isTrue(); // 기존 값 유지
            assertThat(policy.getDailyLimit()).isEqualTo(3); // 기존 값 유지
            assertThat(policy.getMonthlyLimit()).isEqualTo(10); // 기존 값 유지
        }

        @Test
        @DisplayName("성공: 모든 매개변수가 null인 경우 기존 값 유지")
        void updatePolicy_Success_NoChanges() {
            // given
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.BIRTHDAY)
                    .points(1000)
                    .description("생일 축하 포인트")
                    .isActive(true)
                    .build();

            Integer originalPoints = policy.getPoints();
            String originalDescription = policy.getDescription();
            Boolean originalIsActive = policy.getIsActive();

            // when - 모든 매개변수 null
            policy.updatePolicy(null, null, null, null, null, null, null);

            // then - 모든 값이 기존과 동일
            assertThat(policy.getPoints()).isEqualTo(originalPoints);
            assertThat(policy.getDescription()).isEqualTo(originalDescription);
            assertThat(policy.getIsActive()).isEqualTo(originalIsActive);
        }
    }

    @Nested
    @DisplayName("정책 비활성화")
    class DeactivatePolicy {

        @Test
        @DisplayName("성공: 활성화된 정책 비활성화")
        void deactivate_Success() {
            // given
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.REVIEW_WRITE)
                    .points(100)
                    .description("리뷰 작성 보상")
                    .isActive(true)
                    .build();

            // when
            policy.deactivate();

            // then
            assertThat(policy.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("성공: 이미 비활성화된 정책에 대한 비활성화 호출")
        void deactivate_Success_AlreadyInactive() {
            // given
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.FIRST_PURCHASE)
                    .points(2000)
                    .description("첫 구매 보너스")
                    .isActive(false)
                    .build();

            // when
            policy.deactivate();

            // then
            assertThat(policy.getIsActive()).isFalse(); // 여전히 false
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCases {

        @Test
        @DisplayName("성공: 경계값 테스트 - 현재 시간과 정확히 같은 validFrom")
        void isValid_Success_ExactValidFrom() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime exactValidFrom = LocalDateTime.of(
                    now.getYear(), now.getMonth(), now.getDayOfMonth(),
                    now.getHour(), now.getMinute(), now.getSecond()
            );

            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.COMPENSATION)
                    .points(300)
                    .description("보상 포인트")
                    .isActive(true)
                    .validFrom(exactValidFrom)
                    .build();

            // when & then
            assertThat(policy.isValid()).isTrue();
        }

        @Test
        @DisplayName("성공: 경계값 테스트 - 현재 시간과 정확히 같은 validTo")
        void isValid_Success_ExactValidTo() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime exactValidTo = LocalDateTime.of(
                    now.getYear(), now.getMonth(), now.getDayOfMonth(),
                    now.getHour(), now.getMinute(), now.getSecond()
            );

            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.ADJUSTMENT)
                    .points(100)
                    .description("포인트 조정")
                    .isActive(true)
                    .validTo(exactValidTo)
                    .build();

            // when & then - 정확히 같은 시간은 이후로 판단되어 유효하지 않음
            assertThat(policy.isValid()).isFalse();
        }

        @Test
        @DisplayName("성공: 0 포인트 정책 생성")
        void createRewardPolicy_Success_ZeroPoints() {
            // given & when
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.ADMIN_GRANT)
                    .points(0)
                    .description("0 포인트 테스트")
                    .build();

            // then
            assertThat(policy.getPoints()).isEqualTo(0);
        }

        @Test
        @DisplayName("성공: 매우 큰 포인트 값")
        void createRewardPolicy_Success_LargePoints() {
            // given & when
            RewardPolicy policy = RewardPolicy.builder()
                    .rewardType(RewardType.EVENT_WINNER)
                    .points(Integer.MAX_VALUE)
                    .description("최대 포인트 테스트")
                    .build();

            // then
            assertThat(policy.getPoints()).isEqualTo(Integer.MAX_VALUE);
        }
    }
}
