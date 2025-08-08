package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.enums.UserCouponStatus;
import com.cMall.feedShop.user.domain.model.UserCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon,Integer> {
    // 사용자 ID로 모든 쿠폰 조회
    List<UserCoupon> findByUserId(Long userId);

    // 사용자 ID와 쿠폰 상태로 쿠폰 조회
    List<UserCoupon> findByUserIdAndCouponStatus(Long userId, UserCouponStatus couponStatus);

    // 사용자 ID로 쿠폰 페이징 조회
    Page<UserCoupon> findByUserId(Long userId, Pageable pageable);

    // 사용자 ID와 쿠폰 상태로 쿠폰 페이징 조회
    Page<UserCoupon> findByUserIdAndCouponStatus(Long userId, UserCouponStatus couponStatus, Pageable pageable);

    // 사용자 이메일로 모든 쿠폰 조회
    List<UserCoupon> findByUserEmail(String email);

    // 사용자 이메일과 쿠폰 상태로 쿠폰 조회
    List<UserCoupon> findByUserEmailAndCouponStatus(String email, UserCouponStatus couponStatus);

    // 사용자 이메일로 쿠폰 페이징 조회
    Page<UserCoupon> findByUserEmail(String email, Pageable pageable);

    // 사용자 이메일과 쿠폰 상태로 쿠폰 페이징 조회
    Page<UserCoupon> findByUserEmailAndCouponStatus(String email, UserCouponStatus couponStatus, Pageable pageable);

    // 사용자 이메일과 쿠폰 코드로 쿠폰 조회
    Optional<UserCoupon> findByUserEmailAndCouponCode(String email, String couponCode);

    // 사용자 이메일과 상태, 만료 날짜로 쿠폰 조회 (만료되지 않은 쿠폰)
    List<UserCoupon> findByUserEmailAndCouponStatusAndExpiresAtAfter(String email, UserCouponStatus couponStatus, LocalDateTime now);


    // 쿠폰 코드로 쿠폰 존재 여부 확인
    boolean existsByCouponCode(String couponCode);
}
