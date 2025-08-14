package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
<<<<<<< HEAD
import com.cMall.feedShop.user.domain.enums.PointTransactionStatus;
=======
>>>>>>> rewardChange
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

<<<<<<< HEAD
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) // ⭐ 새로운 필드: 트랜잭션 상태
    private PointTransactionStatus status;

    @Builder
    public PointTransaction(User user, PointTransactionType transactionType, Integer points,
                          Integer balanceAfter, String description, Long relatedOrderId, LocalDateTime expiryDate, PointTransactionStatus status) {
=======
    @Builder
    public PointTransaction(User user, PointTransactionType transactionType, Integer points, 
                          Integer balanceAfter, String description, Long relatedOrderId, LocalDateTime expiryDate) {
>>>>>>> rewardChange
        this.user = user;
        this.transactionType = transactionType;
        this.points = points;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.relatedOrderId = relatedOrderId;
        this.expiryDate = expiryDate;
<<<<<<< HEAD
        this.status = status;
    }

    // 포인트 적립 거래 생성
    public static PointTransaction createEarnTransaction(User user, Integer points, Integer balanceAfter,
=======
    }

    // 포인트 적립 거래 생성
    public static PointTransaction createEarnTransaction(User user, Integer points, Integer balanceAfter, 
>>>>>>> rewardChange
                                                        String description, Long relatedOrderId) {
        return PointTransaction.builder()
                .user(user)
                .transactionType(PointTransactionType.EARN)
                .points(points)
                .balanceAfter(balanceAfter)
                .description(description)
                .relatedOrderId(relatedOrderId)
                .expiryDate(LocalDateTime.now().plusYears(1)) // 1년 후 만료
<<<<<<< HEAD
                .status(PointTransactionStatus.ACTIVE)
=======
>>>>>>> rewardChange
                .build();
    }

    // 포인트 사용 거래 생성
<<<<<<< HEAD
    public static PointTransaction createUseTransaction(User user, Integer points, Integer balanceAfter,
=======
    public static PointTransaction createUseTransaction(User user, Integer points, Integer balanceAfter, 
>>>>>>> rewardChange
                                                       String description, Long relatedOrderId) {
        return PointTransaction.builder()
                .user(user)
                .transactionType(PointTransactionType.USE)
                .points(points)
                .balanceAfter(balanceAfter)
                .description(description)
                .relatedOrderId(relatedOrderId)
<<<<<<< HEAD
                .status(PointTransactionStatus.USED)
=======
>>>>>>> rewardChange
                .build();
    }

    // 포인트 만료 거래 생성
<<<<<<< HEAD
    public static PointTransaction createExpireTransaction(User user, Integer points, Integer balanceAfter,
=======
    public static PointTransaction createExpireTransaction(User user, Integer points, Integer balanceAfter, 
>>>>>>> rewardChange
                                                          String description) {
        return PointTransaction.builder()
                .user(user)
                .transactionType(PointTransactionType.EXPIRE)
                .points(points)
                .balanceAfter(balanceAfter)
                .description(description)
<<<<<<< HEAD
                .status(PointTransactionStatus.EXPIRED)
=======
>>>>>>> rewardChange
                .build();
    }

    // 포인트 취소 거래 생성
<<<<<<< HEAD
    public static PointTransaction createCancelTransaction(User user, Integer points, Integer balanceAfter,
=======
    public static PointTransaction createCancelTransaction(User user, Integer points, Integer balanceAfter, 
>>>>>>> rewardChange
                                                          String description, Long relatedOrderId) {
        return PointTransaction.builder()
                .user(user)
                .transactionType(PointTransactionType.CANCEL)
                .points(points)
                .balanceAfter(balanceAfter)
                .description(description)
                .relatedOrderId(relatedOrderId)
<<<<<<< HEAD
                .status(PointTransactionStatus.CANCELLED)
                .build();
    }

    //만료 상태로 업데이트하는 메서드
    public void markAsExpired() {
        if (this.status == PointTransactionStatus.ACTIVE) { // ACTIVE 상태인 경우에만 만료 처리
            this.status = PointTransactionStatus.EXPIRED;
            // 만약 만료 처리 시 특정 로직이 더 필요하다면 추가
        }
    }

    //사용 상태로 업데이트하는 메서드 (개별 적립 포인트가 사용될 때)
    public void markAsUsed() {
        if (this.status == PointTransactionStatus.ACTIVE) { // ACTIVE 상태인 경우에만 사용 처리
            this.status = PointTransactionStatus.USED;
        }
    }

        // 만료 여부 확인
=======
                .build();
    }

    // 만료 여부 확인
>>>>>>> rewardChange
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
