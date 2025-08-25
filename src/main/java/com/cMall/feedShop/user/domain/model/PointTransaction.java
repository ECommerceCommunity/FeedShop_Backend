package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.enums.PointTransactionStatus;
import com.cMall.feedShop.user.domain.enums.PointTransactionType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "point_transactions")
@Getter
@NoArgsConstructor
public class PointTransaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private PointTransactionType transactionType;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "related_order_id")
    private Long relatedOrderId;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PointTransactionStatus status;

    @Builder
    public PointTransaction(User user, PointTransactionType transactionType, Integer points,
                          Integer balanceAfter, String description, Long relatedOrderId,
                          LocalDateTime expiryDate, PointTransactionStatus status) {
        this.user = user;
        this.transactionType = transactionType;
        this.points = points;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.relatedOrderId = relatedOrderId;
        this.expiryDate = expiryDate;
        this.status = status != null ? status : PointTransactionStatus.ACTIVE;
    }

    // 포인트 적립 거래 생성
    public static PointTransaction createEarnTransaction(User user, Integer points, Integer balanceAfter,
                                                        String description, Long orderId) {
        LocalDateTime expiryDate = LocalDateTime.now().plusYears(1); // 1년 후 만료

        return PointTransaction.builder()
                .user(user)
                .transactionType(PointTransactionType.EARN)
                .points(points)
                .balanceAfter(balanceAfter)
                .description(description)
                .relatedOrderId(orderId)
                .expiryDate(expiryDate)
                .status(PointTransactionStatus.ACTIVE)
                .build();
    }

    // 포인트 사용 거래 생성
    public static PointTransaction createUseTransaction(User user, Integer points, Integer balanceAfter,
                                                       String description, Long orderId) {
        return PointTransaction.builder()
                .user(user)
                .transactionType(PointTransactionType.USE)
                .points(points)
                .balanceAfter(balanceAfter)
                .description(description)
                .relatedOrderId(orderId)
                .status(PointTransactionStatus.ACTIVE)
                .build();
    }

    // 포인트 만료 거래 생성
    public static PointTransaction createExpireTransaction(User user, Integer points, Integer balanceAfter,
                                                          String description) {
        return PointTransaction.builder()
                .user(user)
                .transactionType(PointTransactionType.EXPIRE)
                .points(points)
                .balanceAfter(balanceAfter)
                .description(description)
                .status(PointTransactionStatus.EXPIRED)
                .build();
    }

    // 포인트 취소 거래 생성 (주문 취소 시)
    public static PointTransaction createCancelTransaction(User user, Integer points, Integer balanceAfter,
                                                          String description, Long orderId) {
        return PointTransaction.builder()
                .user(user)
                .transactionType(PointTransactionType.CANCEL)
                .points(points)
                .balanceAfter(balanceAfter)
                .description(description)
                .relatedOrderId(orderId)
                .status(PointTransactionStatus.ACTIVE)
                .build();
    }

    // 포인트 만료 상태로 변경
    public void markAsExpired() {
        if (this.status == PointTransactionStatus.ACTIVE) {
            this.status = PointTransactionStatus.EXPIRED;
        }
    }

    // 포인트 사용됨 상태로 변경
    public void markAsUsed() {
        if (this.status == PointTransactionStatus.ACTIVE) {
            this.status = PointTransactionStatus.USED;
        }
    }

    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return this.expiryDate != null && LocalDateTime.now().isAfter(this.expiryDate);
    }

    /**
     * 만료까지 남은 일수 계산
     */
    public long getDaysUntilExpiry() {
        if (this.expiryDate == null) {
            return Long.MAX_VALUE;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), this.expiryDate);
    }
}