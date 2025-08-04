//package com.cMall.feedShop.event.domain.repository;
//
//import com.cMall.feedShop.user.domain.enums.UserCouponStatus;
//import com.cMall.feedShop.user.domain.model.User;
//import com.cMall.feedShop.user.domain.model.UserCoupon;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface UserCouponRepository extends JpaRepository<UserCoupon,Long> {
//    // User 객체를 통해 찾는 방식
//    List<UserCoupon> findByUserAndCouponStatus(User user, UserCouponStatus couponStatus);
//
//    // User ID를 통해 찾는 방식 (더 범용적으로 사용될 수 있음)
//    List<UserCoupon> findByUserIdAndCouponStatus(Long userId, UserCouponStatus couponStatus);
//
//    Optional<UserCoupon> findByCouponCode(String couponCode);
//}
