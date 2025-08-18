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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false)
    private UserLevel currentLevel = UserLevel.LEVEL_1;
    
    @Column(name = "level_updated_at")
    private LocalDateTime levelUpdatedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Builder
    public UserStats(User user) {
        this.user = user;
        this.totalPoints = 0;
        this.currentLevel = UserLevel.LEVEL_1;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 점수 추가 및 레벨 업데이트
     */
    public boolean addPoints(int points) {
        this.totalPoints += points;
        this.updatedAt = LocalDateTime.now();
        
        UserLevel newLevel = UserLevel.fromPoints(this.totalPoints);
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
    public int getPointsToNextLevel() {
        return currentLevel.getPointsToNextLevel(totalPoints);
    }
    
    /**
     * 현재 레벨에서의 진행률 (0.0 ~ 1.0)
     */
    public double getLevelProgress() {
        UserLevel[] levels = UserLevel.values();
        int currentLevelIndex = currentLevel.ordinal();
        
        if (currentLevelIndex >= levels.length - 1) {
            return 1.0; // 최고 레벨
        }
        
        UserLevel nextLevel = levels[currentLevelIndex + 1];
        int currentLevelPoints = currentLevel.getRequiredPoints();
        int nextLevelPoints = nextLevel.getRequiredPoints();
        int pointsInCurrentLevel = totalPoints - currentLevelPoints;
        int pointsRequiredForLevel = nextLevelPoints - currentLevelPoints;
        
        return Math.min(1.0, (double) pointsInCurrentLevel / pointsRequiredForLevel);
    }
}
