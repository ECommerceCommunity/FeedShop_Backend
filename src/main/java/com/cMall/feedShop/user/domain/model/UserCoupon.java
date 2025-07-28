package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.user.domain.enums.DiscountType;
import com.cMall.feedShop.user.domain.enums.UserCouponStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(name="coupon_code", nullable=false,  unique=true)
    private String couponCode;

    @Column(name="coupon_name", nullable = false)
    private String couponName;

    @Enumerated(EnumType.STRING)
    @Column(name="discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name="discount_value")
    private Double discountValue;

    @Column(name="is_free_shiping", nullable = false)
    private boolean isFreeShipping = false;

    @Enumerated(EnumType.STRING)
    @Column(name="coupon_status", nullable = false)
    private UserCouponStatus couponStatus = UserCouponStatus.ACTIVE;

    @CreatedDate
    @Column(name="issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name="expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
