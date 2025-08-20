package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.model.UserLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLevelRepository extends JpaRepository<UserLevel, Integer> {
    
    // 모든 레벨을 점수 순으로 정렬하여 조회
    @Query("SELECT ul FROM UserLevel ul ORDER BY ul.minPointsRequired ASC")
    List<UserLevel> findAllOrderByMinPointsRequired();
    
    // 특정 점수에 해당하는 레벨 조회
    @Query("SELECT ul FROM UserLevel ul WHERE ul.minPointsRequired <= :points ORDER BY ul.minPointsRequired DESC")
    List<UserLevel> findLevelsByPoints(int points);
    
    // 최소 점수로 레벨 조회
    Optional<UserLevel> findByMinPointsRequired(Integer minPointsRequired);
    
    // 레벨 이름으로 조회
    Optional<UserLevel> findByLevelName(String levelName);
    
    // 최고 레벨 조회
    @Query("SELECT ul FROM UserLevel ul ORDER BY ul.minPointsRequired DESC LIMIT 1")
    Optional<UserLevel> findTopLevel();
}
