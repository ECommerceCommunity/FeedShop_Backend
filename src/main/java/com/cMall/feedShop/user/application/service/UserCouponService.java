package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.domain.enums.DiscountType;
import com.cMall.feedShop.user.domain.enums.UserCouponStatus;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserCoupon;
import com.cMall.feedShop.user.application.dto.response.CouponResponse;
import com.cMall.feedShop.user.domain.repository.UserCouponRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCouponService {
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    // 사용자 이메일로 쿠폰 목록 조회
    public List<CouponResponse> getUserCouponsByEmail(String email, UserCouponStatus status) {
        List<UserCoupon> userCoupons = status != null
                ? userCouponRepository.findByUserEmailAndCouponStatusAndExpiresAtAfter(email, status, LocalDateTime.now())
                : userCouponRepository.findByUserEmail(email);
        return userCoupons.stream()
                .map(this::mapToCouponResponse)
                .collect(Collectors.toList());
    }

    public Page<CouponResponse> getUserCouponsByEmail(String email, UserCouponStatus status, Pageable pageable) {
        Page<UserCoupon> userCouponPage;
        if (status != null) {
            // (1) 상태가 지정된 경우, 만료되지 않은 쿠폰만 필터링
            userCouponPage = userCouponRepository.findByUserEmailAndCouponStatusAndExpiresAtAfter(email, status, LocalDateTime.now(), pageable);
        } else {
            // (2) 상태가 지정되지 않은 경우, 모든 쿠폰 조회 (만료 조건 제외)
            userCouponPage = userCouponRepository.findByUserEmail(email, pageable);
        }
        return userCouponPage.map(this::mapToCouponResponse);
    }

    // 쿠폰 발급
    @Transactional
    public CouponResponse issueCoupon(String email, String couponCode, String couponName, DiscountType discountType,
                                      BigDecimal discountValue, boolean isFreeShipping, LocalDateTime expiresAt) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        if (userCouponRepository.existsByCouponCode(couponCode)) {
            throw new IllegalArgumentException("Coupon code already exists: " + couponCode);
        }

        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .couponCode(couponCode)
                .couponName(couponName)
                .discountType(discountType)
                .discountValue(discountValue)
                .isFreeShipping(isFreeShipping)
                .couponStatus(UserCouponStatus.ACTIVE)
                .issuedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();

        userCouponRepository.save(userCoupon);
        return mapToCouponResponse(userCoupon);
    }

    // 쿠폰 사용
    @Transactional(noRollbackFor = IllegalStateException.class)
    public CouponResponse useCoupon(String email, String couponCode) {
        UserCoupon userCoupon = userCouponRepository.findByUserEmailAndCouponCode(email, couponCode)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found for user: " + email + ", code: " + couponCode));

        // 유효성 검증 로직
        if (userCoupon.getCouponStatus() != UserCouponStatus.ACTIVE) {
            throw new IllegalStateException("Coupon is not active: " + couponCode);
        }
        if (userCoupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            // 만료된 쿠폰의 상태를 변경하는 메서드를 엔티티에 추가
            userCoupon.expireCoupon();
            throw new IllegalStateException("Coupon has expired: " + couponCode);
        }

        // 엔티티 내부의 메서드를 호출하여 상태 변경
        userCoupon.useCoupon();

        // 이 시점에서는 JPA가 변경을 감지하고 자동으로 DB에 반영하므로,
        // userCouponRepository.save(userCoupon)는 생략해도 됩니다.
        // 하지만 명시적으로 호출하는 것도 나쁘지 않습니다.

        return mapToCouponResponse(userCoupon);
    }

    // UserCoupon 엔티티를 CouponResponse DTO로 변환
    private CouponResponse mapToCouponResponse(UserCoupon userCoupon) {
        return CouponResponse.builder()
                .couponName(userCoupon.getCouponName())
                .discountValue(userCoupon.getDiscountValue())
                .isFreeShipping(userCoupon.isFreeShipping())
                .couponStatus(userCoupon.getCouponStatus().name())
                .expiresAt(userCoupon.getExpiresAt())
                .build();
    }
}