package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.model.ActivityType;
import com.cMall.feedShop.user.domain.model.DailyPoints;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    
    // 특정 사용자의 활동 내역 조회 (최신순)
    Page<UserActivity> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 특정 사용자의 특정 활동 타입 조회
    List<UserActivity> findByUserAndActivityType(User user, ActivityType activityType);
    
    // 특정 사용자의 총 점수 계산
    @Query("SELECT COALESCE(SUM(ua.pointsEarned), 0) FROM UserActivity ua WHERE ua.user = :user")
    Integer getTotalPointsByUser(@Param("user") User user);
    
    // 특정 기간 내 사용자 활동 조회
    List<UserActivity> findByUserAndCreatedAtBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
    
    // 특정 활동 타입의 총 발생 횟수
    Long countByActivityType(ActivityType activityType);
    
    // 참조 ID와 타입으로 활동 존재 여부 확인 (중복 방지)
    boolean existsByUserAndReferenceIdAndReferenceType(User user, Long referenceId, String referenceType);
    
    // 최근 활동 내역 조회 (관리자용)
    @Query("SELECT ua FROM UserActivity ua ORDER BY ua.createdAt DESC")
    Page<UserActivity> findRecentActivities(Pageable pageable);
    
    // 특정 사용자의 일별 점수 통계 (Object[] 반환)
    @Query("SELECT CAST(ua.createdAt AS date) as date, SUM(ua.pointsEarned) as totalPoints " +
            "FROM UserActivity ua WHERE ua.user = :user " +
            "AND ua.createdAt >= :startDate " +
            "GROUP BY CAST(ua.createdAt AS date) " +
            "ORDER BY CAST(ua.createdAt AS date) DESC")
    List<Object[]> getDailyPointsStatistics(@Param("user") User user, @Param("startDate") LocalDate startDate);
}
