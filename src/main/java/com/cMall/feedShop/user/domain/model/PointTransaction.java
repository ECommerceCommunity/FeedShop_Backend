package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.enums.PointTransactionType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Builder
    public PointTransaction(User user, PointTransactionType transactionType, Integer points, 
                          Integer balanceAfter, String description, Long relatedOrderId, LocalDateTime expiryDate) {
        this.user = user;
        this.transactionType = transactionType;
        this.points = points;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.relatedOrderId = relatedOrderId;
        this.expiryDate = expiryDate;
    }

    // 포인트 적립 거래 생성
    public static PointTransaction createEarnTransaction(User user, Integer points, Integer balanceAfter, 
                                                        String description, Long relatedOrderId) {
        return PointTransaction.builder()
                .user(user)
                .transactionType(PointTransactionType.EARN)
                .points(points)
                .balanceAfter(balanceAfter)
                .description(description)
                .relatedOrderId(relatedOrderId)
                .expiryDate(LocalDateTime.now().plusYears(1)) // 1년 후 만료
                .build();
    }

    // 포인트 사용 거래 생성
    public static PointTransaction createUseTransaction(User user, Integer points, Integer balanceAfter, 
                                                       String description, Long relatedOrderId) {
        return PointTransaction.builder()
                .user(user)
                .transactionType(PointTransactionType.USE)
                .points(points)
                .balanceAfter(balanceAfter)
                .description(description)
                .relatedOrderId(relatedOrderId)
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
                .build();
    }

    // 포인트 취소 거래 생성
    public static PointTransaction createCancelTransaction(User user, Integer points, Integer balanceAfter, 
                                                          String description, Long relatedOrderId) {
        return PointTransaction.builder()
                .user(user)
                .transactionType(PointTransactionType.CANCEL)
                .points(points)
                .balanceAfter(balanceAfter)
                .description(description)
                .relatedOrderId(relatedOrderId)
                .build();
    }

    // 만료 여부 확인
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    // 만료까지 남은 일수
    public long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return -1; // 만료일이 없는 경우
        }
        return java.time.Duration.between(LocalDateTime.now(), expiryDate).toDays();
    }
}
