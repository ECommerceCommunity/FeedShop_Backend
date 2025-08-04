//package com.cMall.feedShop.user.application.service;
//
//import com.cMall.feedShop.event.application.service.EventReadService;
//import com.cMall.feedShop.user.domain.model.UserCoupon;
//import com.cMall.feedShop.user.application.dto.response.CouponResponse; // 이 DTO를 사용합니다.
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//d
//@Service
//@RequiredArgsConstructor
//public class UserCouponService {
//    private final EventReadService eventReadService;
//
//    public List<CouponResponse> getUserCoupons(Long userId) {
//        // 1. event 도메인의 엔티티(UserCoupon) 리스트를 가져옵니다.
//        List<UserCoupon> userCoupons = eventReadService.getCouponsByUserId(userId);
//
//        // 2. Stream을 사용하여 엔티티를 DTO로 변환합니다.
//        return userCoupons.stream()
//                .map(this::mapToCouponResponse)
//                .collect(Collectors.toList());
//    }
//
//    // UserCoupon 엔티티를 CouponResponse DTO로 변환하는 private 메서드
//    private CouponResponse mapToCouponResponse(UserCoupon userCoupon) {
//        return CouponResponse.builder()
//                .couponName(userCoupon.getCouponName())
//                .discountValue(userCoupon.getDiscountValue())
//                .isFreeShipping(userCoupon.isFreeShipping())
//                .couponStatus(userCoupon.getCouponStatus().name()) // Enum을 String으로 변환
//                .expiresAt(userCoupon.getExpiresAt())
//                .build();
//    }
//}