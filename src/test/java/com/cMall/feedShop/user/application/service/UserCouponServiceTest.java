
package com.cMall.feedShop.user.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.cMall.feedShop.user.application.dto.response.CouponResponse;
import com.cMall.feedShop.user.domain.enums.DiscountType;
import com.cMall.feedShop.user.domain.enums.Gender;
import com.cMall.feedShop.user.domain.enums.UserCouponStatus;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserCoupon;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserCouponRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserCouponServiceTest {

    @InjectMocks
    private UserCouponService userCouponService;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private UserCoupon activeCoupon;

    @BeforeEach
    void setUp() {
        // User 엔티티 생성
        user = User.builder()
                .loginId("test_user")
                .email("test@example.com")
                .password("password")
                .role(UserRole.USER)
                .build();

        // UserProfile을 빌더로 생성하여 User와 연결
        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .name("테스터")
                .nickname("tester")
                .phone("010-1234-5678")
                .gender(Gender.MALE)
                .build();

        // User 엔티티에도 userProfile을 연결
        user.setUserProfile(userProfile);

        // 나머지 UserCoupon 관련 코드
        activeCoupon = UserCoupon.builder()
                .user(user)
                .couponCode("TEST_COUPON")
                .couponName("10% 할인 쿠폰")
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(BigDecimal.TEN)
                .couponStatus(UserCouponStatus.ACTIVE)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("쿠폰 발급 성공 테스트")
    void issueCoupon_Success() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userCouponRepository.existsByCouponCode(anyString())).thenReturn(false);
        when(userCouponRepository.save(any(UserCoupon.class))).thenAnswer(invocation -> {
            UserCoupon savedCoupon = invocation.getArgument(0);
            return savedCoupon;
        });

        // when
        CouponResponse response = userCouponService.issueCoupon(
                "test@example.com", "NEW_COUPON", "새 쿠폰",
                DiscountType.FIXED_DISCOUNT, BigDecimal.valueOf(1000), false,
                LocalDateTime.now().plusDays(10)
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCouponName()).isEqualTo("새 쿠폰");
        verify(userRepository).findByEmail("test@example.com");
        verify(userCouponRepository).existsByCouponCode("NEW_COUPON");
        verify(userCouponRepository).save(any(UserCoupon.class));
    }

    @Test
    @DisplayName("쿠폰 발급 실패 - 존재하지 않는 사용자")
    void issueCoupon_Fail_UserNotFound() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userCouponService.issueCoupon(
                "nouser@example.com", "FAIL_COUPON", "실패",
                DiscountType.FIXED_DISCOUNT, BigDecimal.ZERO, false, LocalDateTime.now().plusDays(1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("쿠폰 발급 실패 - 이미 존재하는 쿠폰 코드")
    void issueCoupon_Fail_CouponCodeExists() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userCouponRepository.existsByCouponCode(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userCouponService.issueCoupon(
                "test@example.com", "EXISTING_COUPON", "존재하는 쿠폰",
                DiscountType.FIXED_DISCOUNT, BigDecimal.ZERO, false, LocalDateTime.now().plusDays(1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Coupon code already exists");
    }

    @Test
    @DisplayName("쿠폰 사용 성공 테스트")
    void useCoupon_Success() {
        // given
        when(userCouponRepository.findByUserEmailAndCouponCode(anyString(), anyString()))
                .thenReturn(Optional.of(activeCoupon));

        // when
        CouponResponse response = userCouponService.useCoupon("test@example.com", "TEST_COUPON");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCouponStatus()).isEqualTo(UserCouponStatus.USED.name());
        assertThat(activeCoupon.getCouponStatus()).isEqualTo(UserCouponStatus.USED);
    }

    @Test
    @DisplayName("쿠폰 사용 실패 - 쿠폰을 찾을 수 없음")
    void useCoupon_Fail_CouponNotFound() {
        // given
        when(userCouponRepository.findByUserEmailAndCouponCode(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userCouponService.useCoupon("test@example.com", "NOT_FOUND"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Coupon not found");
    }

    @Test
    @DisplayName("쿠폰 사용 실패 - 이미 사용된 쿠폰")
    void useCoupon_Fail_AlreadyUsed() {
        // given
        activeCoupon.useCoupon(); // 상태를 USED로 변경
        when(userCouponRepository.findByUserEmailAndCouponCode(anyString(), anyString()))
                .thenReturn(Optional.of(activeCoupon));

        // when & then
        assertThatThrownBy(() -> userCouponService.useCoupon("test@example.com", "TEST_COUPON"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Coupon is not active");
    }

    @Test
    @DisplayName("쿠폰 사용 실패 - 만료된 쿠폰")
    void useCoupon_Fail_Expired() {
        // given
        ReflectionTestUtils.setField(activeCoupon, "expiresAt", LocalDateTime.now().minusDays(1));
        when(userCouponRepository.findByUserEmailAndCouponCode(anyString(), anyString()))
                .thenReturn(Optional.of(activeCoupon));

        // when & then
        assertThatThrownBy(() -> userCouponService.useCoupon("test@example.com", "TEST_COUPON"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Coupon has expired");
        assertThat(activeCoupon.getCouponStatus()).isEqualTo(UserCouponStatus.EXPIRED);
    }

    @Test
    @DisplayName("사용자 쿠폰 목록 조회 테스트")
    void getUserCouponsByEmail_Success() {
        // given
        when(userCouponRepository.findByUserEmailAndCouponStatusAndExpiresAtAfter(anyString(),
                any(UserCouponStatus.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(activeCoupon));

        // when
        List<CouponResponse> responses = userCouponService.getUserCouponsByEmail("test@example.com",
                UserCouponStatus.ACTIVE);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.size()).isEqualTo(1);
        assertThat(responses.get(0).getCouponName()).isEqualTo(activeCoupon.getCouponName());
        verify(userCouponRepository).findByUserEmailAndCouponStatusAndExpiresAtAfter(anyString(), any(),
                any());
    }
}