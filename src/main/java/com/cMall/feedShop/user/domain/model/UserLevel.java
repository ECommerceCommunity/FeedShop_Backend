package com.cMall.feedShop.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_levels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLevel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "level_id")
    private Integer levelId;
    
    @Column(name = "level_name", nullable = false, length = 20)
    private String levelName;
    
    @Column(name = "min_points_required", nullable = false)
    private Integer minPointsRequired;
    
    @Column(name = "discount_rate", nullable = false)
    private Double discountRate;
    
    @Column(name = "emoji", length = 10)
    private String emoji;
    
    @Column(name = "reward_description", length = 200)
    private String rewardDescription;
    
    @Builder
    public UserLevel(String levelName, Integer minPointsRequired, Double discountRate, 
                    String emoji, String rewardDescription) {
        this.levelName = levelName;
        this.minPointsRequired = minPointsRequired;
        this.discountRate = discountRate;
        this.emoji = emoji;
        this.rewardDescription = rewardDescription;
    }
    
    public String getDisplayName() {
        return String.format("Lv.%d %s %s", levelId, emoji != null ? emoji : "", levelName);
    }
    
    /**
     * 점수에 따른 레벨 계산 (정적 메서드로 유지)
     */
    public static UserLevel fromPoints(int totalPoints, java.util.List<UserLevel> levels) {
        UserLevel result = null;
        for (UserLevel level : levels) {
            if (totalPoints >= level.getMinPointsRequired()) {
                if (result == null || level.getMinPointsRequired() > result.getMinPointsRequired()) {
                    result = level;
                }
            }
        }
        return result != null ? result : levels.stream()
                .filter(level -> level.getMinPointsRequired() == 0)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 다음 레벨까지 필요한 점수
     */
    public int getPointsToNextLevel(int currentPoints, java.util.List<UserLevel> levels) {
        return levels.stream()
                .filter(level -> level.getMinPointsRequired() > this.minPointsRequired)
                .mapToInt(level -> level.getMinPointsRequired() - currentPoints)
                .min()
                .orElse(0);
    }
}
