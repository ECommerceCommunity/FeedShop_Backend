package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.EventResult;
import com.cMall.feedShop.event.domain.EventResultDetail;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.event.domain.repository.EventResultRepository;
import com.cMall.feedShop.user.application.service.PointService;
import com.cMall.feedShop.user.application.service.UserCouponService;
import com.cMall.feedShop.user.application.service.UserLevelService;
import com.cMall.feedShop.user.domain.enums.DiscountType;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRewardServiceTest {

    @Mock
    private EventResultRepository eventResultRepository;

    @Mock
    private PointService pointService;

    @Mock
    private UserLevelService userLevelService;

    @Mock
    private UserCouponService userCouponService;

    @InjectMocks
    private EventRewardService eventRewardService;

    private Event testEvent;
    private EventDetail testEventDetail;
    private EventResult testEventResult;
    private User testUser1;
    private User testUser2;
    private EventResultDetail testResultDetail1;
    private EventResultDetail testResultDetail2;

    @BeforeEach
    void setUp() {
        // 테스트 이벤트 설정
        testEvent = Event.builder()
                .type(EventType.BATTLE)
                .status(EventStatus.ONGOING)
                .maxParticipants(100)
                .build();

        // Event ID 설정 (reflection 사용)
        try {
            java.lang.reflect.Field idField = Event.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testEvent, 1L);
        } catch (Exception e) {
            // reflection 실패 시 테스트 스킵
        }

        // 테스트 이벤트 상세 설정
        testEventDetail = EventDetail.builder()
                .event(testEvent)
                .title("여름 스타일 배틀")
                .description("가장 트렌디한 여름 스타일을 자랑하는 배틀 이벤트입니다.")
                .build();
        
        // Event와 EventDetail 관계 설정
        testEvent.setEventDetail(testEventDetail);

        // 테스트 사용자 설정
        testUser1 = User.builder()
                .email("user1@test.com")
                .build();
        testUser2 = User.builder()
                .email("user2@test.com")
                .build();

        // User ID 설정 (reflection 사용)
        try {
            java.lang.reflect.Field userIdField = User.class.getDeclaredField("id");
            userIdField.setAccessible(true);
            userIdField.set(testUser1, 1L);
            userIdField.set(testUser2, 2L);
        } catch (Exception e) {
            // reflection 실패 시 테스트 스킵
        }

        // 테스트 이벤트 결과 설정
        testEventResult = EventResult.builder()
                .event(testEvent)
                .resultType(EventResult.ResultType.BATTLE_WINNER)
                .totalParticipants(2)
                .resultDetails(new ArrayList<>())
                .build();

        // EventResult ID 설정 (reflection 사용)
        try {
            java.lang.reflect.Field resultIdField = EventResult.class.getDeclaredField("id");
            resultIdField.setAccessible(true);
            resultIdField.set(testEventResult, 1L);
        } catch (Exception e) {
            // reflection 실패 시 테스트 스킵
        }

        // 테스트 결과 상세 설정
        testResultDetail1 = EventResultDetail.builder()
                .eventResult(testEventResult)
                .user(testUser1)
                .rankPosition(1)
                .pointsEarned(10000)
                .badgePointsEarned(100)
                .couponCode("50% 할인쿠폰")
                .rewardProcessed(false)
                .build();

        testResultDetail2 = EventResultDetail.builder()
                .eventResult(testEventResult)
                .user(testUser2)
                .rankPosition(2)
                .pointsEarned(5000)
                .badgePointsEarned(50)
                .couponCode("30% 할인쿠폰")
                .rewardProcessed(false)
                .build();
    }

    @Test
    @DisplayName("성공: 이벤트 리워드 지급 - 포인트, 뱃지점수, 쿠폰 모두 지급")
    void processEventRewards_Success() {
        // given
        when(eventResultRepository.findByEventId(1L))
                .thenReturn(Optional.of(testEventResult));
        
        // EventResult의 resultDetails를 직접 설정
        testEventResult.getResultDetails().add(testResultDetail1);
        testEventResult.getResultDetails().add(testResultDetail2);

        // when
        EventRewardService.RewardProcessResult result = eventRewardService.processEventRewards(1L);

        // then
        assertThat(result.getEventId()).isEqualTo(1L);
        assertThat(result.getTotalParticipants()).isEqualTo(2L);
        assertThat(result.getSuccessfulRewards()).isEqualTo(2L);
        assertThat(result.getFailedRewards()).isEqualTo(0L);

        // 포인트 지급 확인
        verify(pointService, times(1)).earnPoints(eq(testUser1), eq(10000), anyString(), eq(null));
        verify(pointService, times(1)).earnPoints(eq(testUser2), eq(5000), anyString(), eq(null));

        // 뱃지 점수 지급 확인
        verify(userLevelService, times(1)).recordActivity(eq(1L), any(), anyString(), eq(1L), eq("EVENT"));
        verify(userLevelService, times(1)).recordActivity(eq(2L), any(), anyString(), eq(1L), eq("EVENT"));

        // 쿠폰 지급 확인
        verify(userCouponService, times(1)).issueCoupon(
                eq("user1@test.com"),
                startsWith("EVENT_REWARD_1_1_1_"),
                eq("[이벤트 리워드] 여름 스타일 배틀 1등 할인쿠폰"),
                eq(DiscountType.RATE_DISCOUNT),
                eq(BigDecimal.valueOf(50)),
                eq(false),
                any(LocalDateTime.class)
        );
        verify(userCouponService, times(1)).issueCoupon(
                eq("user2@test.com"),
                startsWith("EVENT_REWARD_1_2_2_"),
                eq("[이벤트 리워드] 여름 스타일 배틀 2등 할인쿠폰"),
                eq(DiscountType.RATE_DISCOUNT),
                eq(BigDecimal.valueOf(30)),
                eq(false),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("성공: 이미 지급된 리워드는 스킵")
    void processEventRewards_SkipAlreadyProcessed() {
        // given
        testResultDetail1.markRewardAsProcessed(); // 이미 지급된 상태로 설정
        when(eventResultRepository.findByEventId(1L))
                .thenReturn(Optional.of(testEventResult));
        
        // EventResult의 resultDetails를 직접 설정
        testEventResult.getResultDetails().add(testResultDetail1);
        testEventResult.getResultDetails().add(testResultDetail2);

        // when
        EventRewardService.RewardProcessResult result = eventRewardService.processEventRewards(1L);

        // then
        assertThat(result.getSuccessfulRewards()).isEqualTo(2L);
        assertThat(result.getFailedRewards()).isEqualTo(0L);

        // 이미 지급된 리워드는 스킵되어 서비스 호출되지 않음
        verify(pointService, times(1)).earnPoints(eq(testUser2), eq(5000), anyString(), eq(null));
        verify(pointService, never()).earnPoints(eq(testUser1), anyInt(), anyString(), eq(null));
    }

    @Test
    @DisplayName("실패: 이벤트 결과를 찾을 수 없는 경우")
    void processEventRewards_EventResultNotFound() {
        // given
        when(eventResultRepository.findByEventId(999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventRewardService.processEventRewards(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이벤트 결과를 찾을 수 없습니다");
    }
}
