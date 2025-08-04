package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.enums.UserCouponStatus;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.event.domain.model.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon,Long> {
    List<UserCoupon> findByUserAndCouponStatus(User user, UserCouponStatus couponStatus);
    Optional<UserCoupon> findByCouponCode(String couponCode);
}
