package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.request.RewardGrantRequest;
import com.cMall.feedShop.user.application.dto.response.RewardHistoryResponse;
import com.cMall.feedShop.user.application.dto.response.RewardPolicyResponse;
import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.exception.UserException;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RewardService 테스트")
class RewardServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RewardPolicyRepository rewardPolicyRepository;
    
    @Mock
    private RewardHistoryRepository rewardHistoryRepository;
    
    @Mock
    private PointService pointService;
    
    @Mock
    private UserDetails userDetails;
    
    @InjectMocks
    private RewardService rewardService;

    private User testUser;
    private User adminUser;
    private RewardPolicy testPolicy;
    private RewardHistory testHistory;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .loginId("testuser")
                .email("testuser@example.com")
                .password("password")
                .role(UserRole.USER)
                .build();

        // 테스트용 관리자 생성
        adminUser = User.builder()
                .loginId("admin")
                .email("admin@example.com")
                .password("password")
                .role(UserRole.ADMIN)
                .build();

        // 테스트용 정책 생성
        testPolicy = RewardPolicy.builder()
                .rewardType(RewardType.REVIEW_WRITE)
                .points(100)
                .description("리뷰 작성 보상")
                .isActive(true)
                .dailyLimit(5)
                .monthlyLimit(20)
                .build();

        // 테스트용 히스토리 생성
        testHistory = RewardHistory.builder()
                .user(testUser)
                .rewardType(RewardType.REVIEW_WRITE)
                .points(100)
                .description("리뷰 작성 보상")
                .build();
    }

    @Nested
    @DisplayName("관리자 포인트 지급")
    class GrantPointsByAdmin {

        @Test
        @DisplayName("성공: 관리자가 사용자에게 포인트 지급")
        void grantPointsByAdmin_Success() {
            // given
            RewardGrantRequest request = createRewardGrantRequest(1L, 1000, "이벤트 참여 보상");
            
            given(userDetails.getUsername()).willReturn("admin");
            given(userRepository.findByLoginId("admin")).willReturn(Optional.of(adminUser));
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(rewardHistoryRepository.save(any(RewardHistory.class))).willReturn(testHistory);

            // when
            RewardHistoryResponse result = rewardService.grantPointsByAdmin(request, userDetails);

            // then
            assertThat(result).isNotNull();
            then(pointService).should().earnPoints(eq(testUser), eq(1000), contains("관리자 지급"), isNull());
            then(rewardHistoryRepository).should(times(2)).save(any(RewardHistory.class)); // 저장 + 처리완료
        }

        @Test
        @DisplayName("실패: 관리자가 아닌 사용자의 포인트 지급 시도")
        void grantPointsByAdmin_Fail_NotAdmin() {
            // given
            RewardGrantRequest request = createRewardGrantRequest(1L, 1000, "이벤트 참여 보상");
            
            given(userDetails.getUsername()).willReturn("testuser");
            given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> rewardService.grantPointsByAdmin(request, userDetails))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자에게 포인트 지급")
        void grantPointsByAdmin_Fail_UserNotFound() {
            // given
            RewardGrantRequest request = createRewardGrantRequest(999L, 1000, "이벤트 참여 보상");
            
            given(userDetails.getUsername()).willReturn("admin");
            given(userRepository.findByLoginId("admin")).willReturn(Optional.of(adminUser));
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> rewardService.grantPointsByAdmin(request, userDetails))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 잘못된 포인트 금액 (0 이하)")
        void grantPointsByAdmin_Fail_InvalidPoints() {
            // given
            RewardGrantRequest request = createRewardGrantRequest(1L, 0, "잘못된 포인트");
            
            given(userDetails.getUsername()).willReturn("admin");
            given(userRepository.findByLoginId("admin")).willReturn(Optional.of(adminUser));
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> rewardService.grantPointsByAdmin(request, userDetails))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POINT_AMOUNT);
        }
    }

    @Nested
    @DisplayName("리뷰 작성 보상")
    class GrantReviewReward {

        @Test
        @DisplayName("성공: 일반 리뷰 작성 보상")
        void grantReviewReward_Success_NormalReview() {
            // given
            Long reviewId = 1L;
            String reviewType = "NORMAL";
            
            given(rewardPolicyRepository.findValidPolicyByType(RewardType.REVIEW_WRITE))
                    .willReturn(Optional.of(testPolicy));
            given(rewardHistoryRepository.save(any(RewardHistory.class))).willReturn(testHistory);

            // when
            RewardHistoryResponse result = rewardService.grantReviewReward(testUser, reviewId, reviewType);

            // then
            assertThat(result).isNotNull();
            then(pointService).should().earnPoints(eq(testUser), eq(100), contains("리뷰 보상"), isNull());
            then(rewardHistoryRepository).should(times(2)).save(any(RewardHistory.class));
        }

        @Test
        @DisplayName("성공: 사진 리뷰 작성 보상")
        void grantReviewReward_Success_PhotoReview() {
            // given
            Long reviewId = 1L;
            String reviewType = "PHOTO";
            RewardPolicy photoPolicy = RewardPolicy.builder()
                    .rewardType(RewardType.REVIEW_PHOTO)
                    .points(150)
                    .description("사진 리뷰 작성 보상")
                    .build();
            
            given(rewardPolicyRepository.findValidPolicyByType(RewardType.REVIEW_PHOTO))
                    .willReturn(Optional.of(photoPolicy));
            given(rewardHistoryRepository.save(any(RewardHistory.class))).willReturn(testHistory);

            // when
            RewardHistoryResponse result = rewardService.grantReviewReward(testUser, reviewId, reviewType);

            // then
            assertThat(result).isNotNull();
            then(pointService).should().earnPoints(eq(testUser), eq(150), contains("리뷰 보상"), isNull());
        }

        @Test
        @DisplayName("실패: 중복 리뷰 보상 지급")
        void grantReviewReward_Fail_AlreadyRewarded() {
            // given
            Long reviewId = 1L;
            String reviewType = "NORMAL";
            
            given(rewardPolicyRepository.findValidPolicyByType(RewardType.REVIEW_WRITE))
                    .willReturn(Optional.of(testPolicy));
            given(rewardHistoryRepository.save(any(RewardHistory.class)))
                    .willThrow(new DataIntegrityViolationException("Duplicate key"));

            // when & then
            assertThatThrownBy(() -> rewardService.grantReviewReward(testUser, reviewId, reviewType))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REWARD_ALREADY_GRANTED);
        }

        @Test
        @DisplayName("실패: 정책을 찾을 수 없음")
        void grantReviewReward_Fail_PolicyNotFound() {
            // given
            Long reviewId = 1L;
            String reviewType = "NORMAL";
            
            given(rewardPolicyRepository.findValidPolicyByType(RewardType.REVIEW_WRITE))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> rewardService.grantReviewReward(testUser, reviewId, reviewType))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REWARD_POLICY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("이벤트 참여 보상")
    class GrantEventReward {

        @Test
        @DisplayName("성공: 이벤트 참여 보상")
        void grantEventReward_Success() {
            // given
            Long eventId = 1L;
            RewardType eventRewardType = RewardType.EVENT_PARTICIPATION;
            RewardPolicy eventPolicy = RewardPolicy.builder()
                    .rewardType(eventRewardType)
                    .points(500)
                    .description("이벤트 참여 보상")
                    .dailyLimit(3)
                    .build();
            
            given(rewardPolicyRepository.findValidPolicyByType(eventRewardType))
                    .willReturn(Optional.of(eventPolicy));
            given(rewardHistoryRepository.countDailyRewardsByUserAndType(any(), any(), any()))
                    .willReturn(2L); // 일일 제한 3회 중 2회 사용
            given(rewardHistoryRepository.save(any(RewardHistory.class))).willReturn(testHistory);

            // when
            RewardHistoryResponse result = rewardService.grantEventReward(testUser, eventId, eventRewardType);

            // then
            assertThat(result).isNotNull();
            then(pointService).should().earnPoints(eq(testUser), eq(500), contains("이벤트 보상"), isNull());
        }

        @Test
        @DisplayName("실패: 일일 제한 초과")
        void grantEventReward_Fail_DailyLimitExceeded() {
            // given
            Long eventId = 1L;
            RewardType eventRewardType = RewardType.EVENT_PARTICIPATION;
            RewardPolicy eventPolicy = RewardPolicy.builder()
                    .rewardType(eventRewardType)
                    .points(500)
                    .description("이벤트 참여 보상")
                    .dailyLimit(3)
                    .build();
            
            given(rewardPolicyRepository.findValidPolicyByType(eventRewardType))
                    .willReturn(Optional.of(eventPolicy));
            given(rewardHistoryRepository.countDailyRewardsByUserAndType(any(), any(), any()))
                    .willReturn(3L); // 일일 제한 3회 모두 사용

            // when & then
            assertThatThrownBy(() -> rewardService.grantEventReward(testUser, eventId, eventRewardType))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DAILY_REWARD_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("실패: 월간 제한 초과")
        void grantEventReward_Fail_MonthlyLimitExceeded() {
            // given
            Long eventId = 1L;
            RewardType eventRewardType = RewardType.EVENT_PARTICIPATION;
            RewardPolicy eventPolicy = RewardPolicy.builder()
                    .rewardType(eventRewardType)
                    .points(500)
                    .description("이벤트 참여 보상")
                    .monthlyLimit(10)
                    .build();
            
            given(rewardPolicyRepository.findValidPolicyByType(eventRewardType))
                    .willReturn(Optional.of(eventPolicy));
            given(rewardHistoryRepository.countMonthlyRewardsByUserAndType(any(), any(), any()))
                    .willReturn(10L); // 월간 제한 10회 모두 사용

            // when & then
            assertThatThrownBy(() -> rewardService.grantEventReward(testUser, eventId, eventRewardType))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MONTHLY_REWARD_LIMIT_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("생일 축하 포인트")
    class GrantBirthdayReward {

        @Test
        @DisplayName("성공: 생일 축하 포인트 지급")
        void grantBirthdayReward_Success() {
            // given
            RewardPolicy birthdayPolicy = RewardPolicy.builder()
                    .rewardType(RewardType.BIRTHDAY)
                    .points(1000)
                    .description("생일 축하 포인트")
                    .build();
            
            given(rewardPolicyRepository.findValidPolicyByType(RewardType.BIRTHDAY))
                    .willReturn(Optional.of(birthdayPolicy));
            given(rewardHistoryRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(any(), any(), any(), any()))
                    .willReturn(new PageImpl<>(new ArrayList<>()));
            given(rewardHistoryRepository.save(any(RewardHistory.class))).willReturn(testHistory);

            // when
            RewardHistoryResponse result = rewardService.grantBirthdayReward(testUser);

            // then
            assertThat(result).isNotNull();
            then(pointService).should().earnPoints(eq(testUser), eq(1000), eq("생일 축하 포인트"), isNull());
        }

        @Test
        @DisplayName("성공: 이미 올해 생일 보상 받은 경우 null 반환")
        void grantBirthdayReward_Success_AlreadyReceived() {
            // given
            RewardPolicy birthdayPolicy = RewardPolicy.builder()
                    .rewardType(RewardType.BIRTHDAY)
                    .points(1000)
                    .description("생일 축하 포인트")
                    .build();
            
            RewardHistory birthdayHistory = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.BIRTHDAY)
                    .points(1000)
                    .description("생일 축하 포인트")
                    .build();
            
            given(rewardPolicyRepository.findValidPolicyByType(RewardType.BIRTHDAY))
                    .willReturn(Optional.of(birthdayPolicy));
            given(rewardHistoryRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(any(), any(), any(), any()))
                    .willReturn(new PageImpl<>(List.of(birthdayHistory)));

            // when
            RewardHistoryResponse result = rewardService.grantBirthdayReward(testUser);

            // then
            assertThat(result).isNull();
            then(pointService).should(never()).earnPoints(any(), anyInt(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("첫 구매 보너스")
    class GrantFirstPurchaseReward {

        @Test
        @DisplayName("성공: 첫 구매 보너스 지급")
        void grantFirstPurchaseReward_Success() {
            // given
            Long orderId = 1L;
            RewardPolicy firstPurchasePolicy = RewardPolicy.builder()
                    .rewardType(RewardType.FIRST_PURCHASE)
                    .points(2000)
                    .description("첫 구매 보너스")
                    .build();
            
            given(rewardPolicyRepository.findValidPolicyByType(RewardType.FIRST_PURCHASE))
                    .willReturn(Optional.of(firstPurchasePolicy));
            given(rewardHistoryRepository.save(any(RewardHistory.class))).willReturn(testHistory);

            // when
            RewardHistoryResponse result = rewardService.grantFirstPurchaseReward(testUser, orderId);

            // then
            assertThat(result).isNotNull();
            then(pointService).should().earnPoints(eq(testUser), eq(2000), eq("첫 구매 보너스"), eq(orderId));
        }

        @Test
        @DisplayName("실패: 이미 첫 구매 보너스를 받은 경우")
        void grantFirstPurchaseReward_Fail_AlreadyReceived() {
            // given
            Long orderId = 1L;
            RewardPolicy firstPurchasePolicy = RewardPolicy.builder()
                    .rewardType(RewardType.FIRST_PURCHASE)
                    .points(2000)
                    .description("첫 구매 보너스")
                    .build();
            
            given(rewardPolicyRepository.findValidPolicyByType(RewardType.FIRST_PURCHASE))
                    .willReturn(Optional.of(firstPurchasePolicy));
            given(rewardHistoryRepository.save(any(RewardHistory.class)))
                    .willThrow(new DataIntegrityViolationException("Duplicate key"));

            // when & then
            assertThatThrownBy(() -> rewardService.grantFirstPurchaseReward(testUser, orderId))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REWARD_ALREADY_GRANTED);
        }
    }

    @Nested
    @DisplayName("리워드 히스토리 조회")
    class GetRewardHistory {

        @Test
        @DisplayName("성공: 사용자의 리워드 히스토리 조회")
        void getRewardHistory_Success() {
            // given
            List<RewardHistory> historyList = List.of(testHistory);
            Page<RewardHistory> historyPage = new PageImpl<>(historyList, PageRequest.of(0, 10), 1);
            
            given(userDetails.getUsername()).willReturn("testuser");
            given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(testUser));
            given(rewardHistoryRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class)))
                    .willReturn(historyPage);

            // when
            Page<RewardHistoryResponse> result = rewardService.getRewardHistory(userDetails, 0, 10);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공: 페이지 범위 검증")
        void getRewardHistory_Success_PageValidation() {
            // given
            given(userDetails.getUsername()).willReturn("testuser");
            given(userRepository.findByLoginId("testuser")).willReturn(Optional.of(testUser));
            given(rewardHistoryRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class)))
                    .willReturn(new PageImpl<>(new ArrayList<>()));

            // when
            rewardService.getRewardHistory(userDetails, -1, 0); // 음수 페이지, 0 사이즈

            // then
            then(rewardHistoryRepository).should().findByUserOrderByCreatedAtDesc(eq(testUser), 
                    argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 1));
        }
    }

    @Nested
    @DisplayName("리워드 정책 조회")
    class GetRewardPolicies {

        @Test
        @DisplayName("성공: 유효한 리워드 정책 목록 조회")
        void getRewardPolicies_Success() {
            // given
            List<RewardPolicy> policies = List.of(testPolicy);
            given(rewardPolicyRepository.findValidPolicies()).willReturn(policies);

            // when
            List<RewardPolicyResponse> result = rewardService.getRewardPolicies();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRewardType()).isEqualTo(RewardType.REVIEW_WRITE);
        }

        @Test
        @DisplayName("성공: 빈 정책 목록 조회")
        void getRewardPolicies_Success_Empty() {
            // given
            given(rewardPolicyRepository.findValidPolicies()).willReturn(new ArrayList<>());

            // when
            List<RewardPolicyResponse> result = rewardService.getRewardPolicies();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("미처리 리워드 처리")
    class ProcessPendingRewards {

        @Test
        @DisplayName("성공: 미처리 리워드들을 일괄 처리")
        void processPendingRewards_Success() {
            // given
            RewardHistory pendingReward1 = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.REVIEW_WRITE)
                    .points(100)
                    .description("미처리 리워드 1")
                    .build();
            
            RewardHistory pendingReward2 = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.EVENT_PARTICIPATION)
                    .points(500)
                    .description("미처리 리워드 2")
                    .build();
            
            List<RewardHistory> pendingRewards = List.of(pendingReward1, pendingReward2);
            
            given(rewardHistoryRepository.findByIsProcessedFalseOrderByCreatedAtAsc())
                    .willReturn(pendingRewards);

            // when
            rewardService.processPendingRewards();

            // then
            then(pointService).should(times(2)).earnPoints(any(User.class), anyInt(), anyString(), any());
            then(rewardHistoryRepository).should(times(2)).save(any(RewardHistory.class));
        }

        @Test
        @DisplayName("성공: 일부 실패해도 전체 프로세스 계속 진행")
        void processPendingRewards_Success_ContinueOnFailure() {
            // given
            RewardHistory pendingReward1 = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.REVIEW_WRITE)
                    .points(100)
                    .description("미처리 리워드 1")
                    .build();
            
            RewardHistory pendingReward2 = RewardHistory.builder()
                    .user(testUser)
                    .rewardType(RewardType.EVENT_PARTICIPATION)
                    .points(500)
                    .description("미처리 리워드 2")
                    .build();
            
            List<RewardHistory> pendingRewards = List.of(pendingReward1, pendingReward2);
            
            given(rewardHistoryRepository.findByIsProcessedFalseOrderByCreatedAtAsc())
                    .willReturn(pendingRewards);
            
            // 첫 번째 리워드 처리 시 예외 발생
            willThrow(new RuntimeException("포인트 적립 실패"))
                    .given(pointService).earnPoints(eq(testUser), eq(100), anyString(), any());

            // when
            assertThatCode(() -> rewardService.processPendingRewards())
                    .doesNotThrowAnyException();

            // then
            then(pointService).should(times(2)).earnPoints(any(User.class), anyInt(), anyString(), any());
            then(rewardHistoryRepository).should(times(1)).save(any(RewardHistory.class)); // 성공한 것만 저장
        }
    }

    // 헬퍼 메서드
    private RewardGrantRequest createRewardGrantRequest(Long userId, Integer points, String description) {
        // ReflectionTestUtils나 Builder 패턴이 있다면 사용
        // 현재는 getter만 있으므로 mock을 활용
        RewardGrantRequest mockRequest = mock(RewardGrantRequest.class);
        given(mockRequest.getUserId()).willReturn(userId);
        given(mockRequest.getPoints()).willReturn(points);
        given(mockRequest.getDescription()).willReturn(description);
        return mockRequest;
    }
}
