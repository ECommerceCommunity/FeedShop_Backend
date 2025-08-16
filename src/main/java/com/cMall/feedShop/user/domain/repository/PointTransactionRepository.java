package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.model.PointTransaction;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.enums.PointTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    
    // 사용자의 모든 포인트 거래 내역 조회 (최신순)
    Page<PointTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 사용자의 특정 타입 포인트 거래 내역 조회 (최신순)
    Page<PointTransaction> findByUserAndTransactionTypeOrderByCreatedAtDesc(User user, PointTransactionType transactionType, Pageable pageable);
    
    // 사용자의 특정 기간 포인트 거래 내역 조회 (최신순)
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.user = :user AND pt.createdAt BETWEEN :startDate AND :endDate ORDER BY pt.createdAt DESC")
    Page<PointTransaction> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            @Param("user") User user, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            Pageable pageable);
    
    // 사용자의 만료 예정 포인트 조회 (30일 이내)
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.user = :user AND pt.transactionType = 'EARN' AND pt.expiryDate BETWEEN :now AND :expiryDate AND pt.points > 0 ORDER BY pt.expiryDate ASC")
    List<PointTransaction> findExpiringPointsByUser(
            @Param("user") User user, 
            @Param("now") LocalDateTime now, 
            @Param("expiryDate") LocalDateTime expiryDate);
    
    // 사용자의 만료된 포인트 조회
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.user = :user AND pt.transactionType = 'EARN' AND pt.expiryDate < :now AND pt.points > 0")
    List<PointTransaction> findExpiredPointsByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    // 주문 관련 포인트 거래 내역 조회
    List<PointTransaction> findByUserAndRelatedOrderIdOrderByCreatedAtDesc(User user, Long orderId);
    
    // 사용자의 총 적립 포인트 (만료되지 않은)
    @Query("SELECT COALESCE(SUM(pt.points), 0) FROM PointTransaction pt WHERE pt.user = :user AND pt.transactionType = 'EARN' AND (pt.expiryDate IS NULL OR pt.expiryDate > :now)")
    Integer sumValidEarnedPointsByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    // 사용자의 총 사용 포인트
    @Query("SELECT COALESCE(SUM(pt.points), 0) FROM PointTransaction pt WHERE pt.user = :user AND pt.transactionType = 'USE'")
    Integer sumUsedPointsByUser(@Param("user") User user);
    
    // 사용자의 총 만료 포인트
    @Query("SELECT COALESCE(SUM(pt.points), 0) FROM PointTransaction pt WHERE pt.user = :user AND pt.transactionType = 'EXPIRE'")
    Integer sumExpiredPointsByUser(@Param("user") User user);
}
