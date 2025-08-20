package com.cMall.feedShop.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_level_id", nullable = false)
    private UserLevel currentLevel;
    
    @Column(name = "level_updated_at")
    private LocalDateTime levelUpdatedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Builder
    public UserStats(User user, UserLevel currentLevel) {
        this.user = user;
        this.totalPoints = 0;
        this.currentLevel = currentLevel;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 점수 추가 및 레벨 업데이트
     */
    public boolean addPoints(int points, java.util.List<UserLevel> allLevels) {
        this.totalPoints += points;
        this.updatedAt = LocalDateTime.now();
        
        UserLevel newLevel = UserLevel.fromPoints(this.totalPoints, allLevels);
        boolean levelUp = !newLevel.equals(this.currentLevel);
        
        if (levelUp) {
            this.currentLevel = newLevel;
            this.levelUpdatedAt = LocalDateTime.now();
        }
        
        return levelUp;
    }
    
    /**
     * 다음 레벨까지 필요한 점수
     */
    public int getPointsToNextLevel(java.util.List<UserLevel> allLevels) {
        return currentLevel.getPointsToNextLevel(totalPoints, allLevels);
    }
    
    /**
     * 현재 레벨에서의 진행률 (0.0 ~ 1.0)
     */
    public double getLevelProgress(java.util.List<UserLevel> allLevels) {
        java.util.List<UserLevel> sortedLevels = allLevels.stream()
                .sorted((l1, l2) -> Integer.compare(l1.getMinPointsRequired(), l2.getMinPointsRequired()))
                .toList();
        
        int currentLevelIndex = sortedLevels.indexOf(currentLevel);
        
        if (currentLevelIndex >= sortedLevels.size() - 1) {
            return 1.0; // 최고 레벨
        }
        
        UserLevel nextLevel = sortedLevels.get(currentLevelIndex + 1);
        int currentLevelPoints = currentLevel.getMinPointsRequired();
        int nextLevelPoints = nextLevel.getMinPointsRequired();
        int pointsInCurrentLevel = totalPoints - currentLevelPoints;
        int pointsRequiredForLevel = nextLevelPoints - currentLevelPoints;
        
        return Math.min(1.0, (double) pointsInCurrentLevel / pointsRequiredForLevel);
    }
}
