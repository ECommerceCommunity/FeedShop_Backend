package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.user.domain.enums.DiscountType;
import com.cMall.feedShop.user.domain.enums.UserCouponStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="user_coupons", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_coupon_code", columnList = "coupon_code")
})
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
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
    private BigDecimal discountValue;

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

    @Builder
    public UserCoupon(User user, String couponCode, String couponName, DiscountType discountType,
                      BigDecimal discountValue, boolean isFreeShipping, UserCouponStatus couponStatus,
                      LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.user = user;
        this.couponCode = couponCode;
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.isFreeShipping = isFreeShipping;
        this.couponStatus = couponStatus;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    /**
     * 쿠폰 사용 로직
     */
    public void useCoupon() {
        // 이미 사용되었거나 만료된 쿠폰은 사용 불가
        if (this.couponStatus != UserCouponStatus.ACTIVE) {
            throw new IllegalStateException("이미 사용되었거나 만료된 쿠폰입니다.");
        }
        // 유효기간이 지난 쿠폰은 사용 불가
        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("쿠폰이 이미 만료되었습니다.");
        }

        this.couponStatus = UserCouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void expireCoupon() {
        // 쿠폰 상태가 ACTIVE일 때만 만료 처리
        if (this.couponStatus == UserCouponStatus.ACTIVE) {
            this.couponStatus = UserCouponStatus.EXPIRED;
        } else {
            // 이미 사용되었거나 만료된 쿠폰은 상태를 변경할 필요 없음
            throw new IllegalStateException("이미 사용되었거나 만료된 쿠폰은 상태를 변경할 수 없습니다.");
        }
    }
}