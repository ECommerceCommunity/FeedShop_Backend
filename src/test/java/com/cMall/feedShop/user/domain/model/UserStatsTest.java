package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("사용자 통계 도메인 테스트")
class UserStatsTest {

    private User testUser;
    private UserStats userStats;
    
    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password", "test@example.com", UserRole.USER);
        
        userStats = UserStats.builder()
                .user(testUser)
                .build();
    }
    
    @Test
    @DisplayName("점수 추가 시 총 점수가 증가한다")
    void addPoints_IncreasesTotalPoints() {
        // when
        userStats.addPoints(50);
        
        // then
        assertThat(userStats.getTotalPoints()).isEqualTo(50);
    }
    
    @Test
    @DisplayName("레벨 2 달성 시 레벨업이 발생한다")
    void addPoints_LevelUp_ToLevel2() {
        // when
        boolean levelUp = userStats.addPoints(100);
        
        // then
        assertThat(levelUp).isTrue();
        assertThat(userStats.getCurrentLevel()).isEqualTo(UserLevel.LEVEL_2);
        assertThat(userStats.getLevelUpdatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("레벨업이 발생하지 않으면 false를 반환한다")
    void addPoints_NoLevelUp_ReturnsFalse() {
        // when
        boolean levelUp = userStats.addPoints(50);
        
        // then
        assertThat(levelUp).isFalse();
        assertThat(userStats.getCurrentLevel()).isEqualTo(UserLevel.LEVEL_1);
        assertThat(userStats.getLevelUpdatedAt()).isNull();
    }
    
    @Test
    @DisplayName("다음 레벨까지 필요한 점수를 계산할 수 있다")
    void getPointsToNextLevel_CalculatesCorrectly() {
        // given
        userStats.addPoints(50); // 현재 50점, 레벨 1
        
        // when
        int pointsToNext = userStats.getPointsToNextLevel();
        
        // then
        assertThat(pointsToNext).isEqualTo(50); // 레벨 2까지 50점 더 필요
    }
    
    @Test
    @DisplayName("현재 레벨에서의 진행률을 계산할 수 있다")
    void getLevelProgress_CalculatesCorrectly() {
        // given
        userStats.addPoints(50); // 레벨 1에서 50점 (레벨 2까지 100점 필요)
        
        // when
        double progress = userStats.getLevelProgress();
        
        // then
        assertThat(progress).isEqualTo(0.5); // 50% 진행
    }
    
    @Test
    @DisplayName("최고 레벨에서는 진행률이 100%가 된다")
    void getLevelProgress_MaxLevel_Returns100Percent() {
        // given
        userStats.addPoints(10000); // 최고 레벨 달성
        
        // when
        double progress = userStats.getLevelProgress();
        
        // then
        assertThat(progress).isEqualTo(1.0); // 100%
    }
    
    @Test
    @DisplayName("여러 번의 점수 추가로 여러 레벨을 한 번에 상승시킬 수 있다")
    void addPoints_MultipleLevel_LevelUp() {
        // when
        boolean levelUp = userStats.addPoints(1200); // 레벨 5까지 한 번에 상승
        
        // then
        assertThat(levelUp).isTrue();
        assertThat(userStats.getCurrentLevel()).isEqualTo(UserLevel.LEVEL_5);
        assertThat(userStats.getTotalPoints()).isEqualTo(1200);
    }
}
