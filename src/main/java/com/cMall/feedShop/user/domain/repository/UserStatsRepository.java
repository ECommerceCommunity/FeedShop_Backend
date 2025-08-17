package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserLevel;
import com.cMall.feedShop.user.domain.model.UserStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
    
    // 특정 사용자의 통계 조회
    Optional<UserStats> findByUser(User user);
    
    // 사용자 ID로 통계 조회
    Optional<UserStats> findByUserId(Long userId);
    
    // 특정 레벨 사용자들 조회
    List<UserStats> findByCurrentLevel(UserLevel level);
    
    // 점수 순위 조회 (상위 N명)
    @Query("SELECT us FROM UserStats us ORDER BY us.totalPoints DESC")
    Page<UserStats> findTopUsersByPoints(Pageable pageable);
    
    // 특정 사용자의 순위 조회
    @Query("SELECT COUNT(us) + 1 FROM UserStats us WHERE us.totalPoints > :points")
    Long getUserRankByPoints(@Param("points") Integer points);
    
    // 레벨별 사용자 수 통계
    @Query("SELECT us.currentLevel, COUNT(us) FROM UserStats us GROUP BY us.currentLevel")
    List<Object[]> getLevelDistribution();
    
    // 평균 점수 계산
    @Query("SELECT AVG(us.totalPoints) FROM UserStats us")
    Double getAveragePoints();
    
    // 특정 점수 이상 사용자 수
    Long countByTotalPointsGreaterThanEqual(Integer points);
}
