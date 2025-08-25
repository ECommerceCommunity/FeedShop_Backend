package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.model.BadgeType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    
    // 특정 사용자의 모든 뱃지 조회
    List<UserBadge> findByUserOrderByAwardedAtDesc(User user);
    
    // 특정 사용자의 표시되는 뱃지만 조회
    List<UserBadge> findByUserAndIsDisplayedTrueOrderByAwardedAtDesc(User user);
    
    // 특정 사용자가 특정 뱃지를 가지고 있는지 확인
    Optional<UserBadge> findByUserAndBadgeType(User user, BadgeType badgeType);
    
    // 특정 사용자가 특정 뱃지를 가지고 있는지 boolean으로 확인
    boolean existsByUserAndBadgeType(User user, BadgeType badgeType);
    
    // 특정 사용자의 뱃지 개수
    long countByUser(User user);
    
    // 특정 사용자의 표시되는 뱃지 개수
    long countByUserAndIsDisplayedTrue(User user);
    
    // 특정 뱃지 타입을 가진 모든 사용자 조회
    List<UserBadge> findByBadgeType(BadgeType badgeType);
    
    // 최근에 획득한 뱃지들 조회 (관리자용)
    @Query("SELECT ub FROM UserBadge ub ORDER BY ub.awardedAt DESC")
    List<UserBadge> findRecentBadges(@Param("limit") int limit);
}
