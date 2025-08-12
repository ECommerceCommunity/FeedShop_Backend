package com.cMall.feedShop.user.infrastructure.repository;

import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.model.RewardHistory;
import com.cMall.feedShop.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RewardHistoryJpaRepository extends JpaRepository<RewardHistory, Long> {
    
    // 사용자의 리워드 히스토리 조회 (페이징)
    Page<RewardHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 사용자의 특정 타입 리워드 히스토리 조회
    Page<RewardHistory> findByUserAndRewardTypeOrderByCreatedAtDesc(User user, RewardType rewardType, Pageable pageable);
    
    // 사용자의 특정 기간 리워드 히스토리 조회
    @Query("SELECT rh FROM RewardHistory rh WHERE rh.user = :user " +
           "AND rh.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY rh.createdAt DESC")
    Page<RewardHistory> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            @Param("user") User user, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            Pageable pageable);
    
    // 사용자의 미처리 리워드 조회
    List<RewardHistory> findByUserAndIsProcessedFalseOrderByCreatedAtAsc(User user);
    
    // 특정 관련 엔티티의 리워드 히스토리 조회
    Optional<RewardHistory> findByRelatedIdAndRelatedTypeAndRewardType(
            Long relatedId, String relatedType, RewardType rewardType);
    
    // 사용자의 일일 리워드 획득 횟수 조회
    @Query("SELECT COUNT(rh) FROM RewardHistory rh WHERE rh.user = :user " +
           "AND rh.rewardType = :rewardType " +
           "AND DATE(rh.createdAt) = DATE(:date)")
    Long countDailyRewardsByUserAndType(
            @Param("user") User user, 
            @Param("rewardType") RewardType rewardType, 
            @Param("date") LocalDateTime date);
    
    // 사용자의 월간 리워드 획득 횟수 조회
    @Query("SELECT COUNT(rh) FROM RewardHistory rh WHERE rh.user = :user " +
           "AND rh.rewardType = :rewardType " +
           "AND YEAR(rh.createdAt) = YEAR(:date) " +
           "AND MONTH(rh.createdAt) = MONTH(:date)")
    Long countMonthlyRewardsByUserAndType(
            @Param("user") User user, 
            @Param("rewardType") RewardType rewardType, 
            @Param("date") LocalDateTime date);
    
    // 사용자의 일일 리워드 포인트 합계 조회
    @Query("SELECT COALESCE(SUM(rh.points), 0) FROM RewardHistory rh WHERE rh.user = :user " +
           "AND rh.rewardType = :rewardType " +
           "AND DATE(rh.createdAt) = DATE(:date)")
    Integer sumDailyRewardPointsByUserAndType(
            @Param("user") User user, 
            @Param("rewardType") RewardType rewardType, 
            @Param("date") LocalDateTime date);
    
    // 사용자의 월간 리워드 포인트 합계 조회
    @Query("SELECT COALESCE(SUM(rh.points), 0) FROM RewardHistory rh WHERE rh.user = :user " +
           "AND rh.rewardType = :rewardType " +
           "AND YEAR(rh.createdAt) = YEAR(:date) " +
           "AND MONTH(rh.createdAt) = MONTH(:date)")
    Integer sumMonthlyRewardPointsByUserAndType(
            @Param("user") User user, 
            @Param("rewardType") RewardType rewardType, 
            @Param("date") LocalDateTime date);
    
    // 관리자가 지급한 리워드 조회
    Page<RewardHistory> findByAdminIdOrderByCreatedAtDesc(Long adminId, Pageable pageable);
    
    // 미처리 리워드 전체 조회 (스케줄러용)
    List<RewardHistory> findByIsProcessedFalseOrderByCreatedAtAsc();
}
