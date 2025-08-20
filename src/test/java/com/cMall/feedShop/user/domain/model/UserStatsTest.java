package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("사용자 통계 도메인 테스트")
class UserStatsTest {

    private User testUser;
    private UserStats userStats;
    private List<UserLevel> testLevels;
    
    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password", "test@example.com", UserRole.USER);
        
        // 테스트용 레벨 데이터 생성
        testLevels = Arrays.asList(
            createLevel("새싹", 0, 0.0, "🌱"),
            createLevel("성장", 100, 0.02, "🌿"),
            createLevel("발전", 300, 0.05, "🌳"),
            createLevel("도전", 600, 0.08, "🏅"),
            createLevel("전문가", 1000, 0.10, "👑"),
            createLevel("마스터", 1500, 0.12, "💎"),
            createLevel("레전드", 2200, 0.15, "⭐"),
            createLevel("챔피언", 3000, 0.18, "🔥"),
            createLevel("슈퍼스타", 4000, 0.20, "✨"),
            createLevel("갓", 5500, 0.25, "🚀")
        );
        
        UserLevel defaultLevel = testLevels.get(0); // 새싹 레벨
        
        userStats = UserStats.builder()
                .user(testUser)
                .currentLevel(defaultLevel)
                .build();
    }
    
    @Test
    @DisplayName("점수 추가 시 총 점수가 증가한다")
    void addPoints_IncreasesTotalPoints() {
        // when
        userStats.addPoints(50, testLevels);
        
        // then
        assertThat(userStats.getTotalPoints()).isEqualTo(50);
    }
    
    @Test
    @DisplayName("레벨 2 달성 시 레벨업이 발생한다")
    void addPoints_LevelUp_ToLevel2() {
        // when
        boolean levelUp = userStats.addPoints(100, testLevels);
        
        // then
        assertThat(levelUp).isTrue();
        assertThat(userStats.getCurrentLevel().getLevelName()).isEqualTo("성장");
        assertThat(userStats.getLevelUpdatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("레벨업이 발생하지 않으면 false를 반환한다")
    void addPoints_NoLevelUp_ReturnsFalse() {
        // when
        boolean levelUp = userStats.addPoints(50, testLevels);
        
        // then
        assertThat(levelUp).isFalse();
        assertThat(userStats.getCurrentLevel().getLevelName()).isEqualTo("새싹");
        assertThat(userStats.getLevelUpdatedAt()).isNull();
    }
    
    @Test
    @DisplayName("다음 레벨까지 필요한 점수를 계산할 수 있다")
    void getPointsToNextLevel_CalculatesCorrectly() {
        // given
        userStats.addPoints(50, testLevels); // 현재 50점, 레벨 1
        
        // when
        int pointsToNext = userStats.getPointsToNextLevel(testLevels);
        
        // then
        assertThat(pointsToNext).isEqualTo(50); // 레벨 2까지 50점 더 필요
    }
    
    @Test
    @DisplayName("현재 레벨에서의 진행률을 계산할 수 있다")
    void getLevelProgress_CalculatesCorrectly() {
        // given
        userStats.addPoints(50, testLevels); // 레벨 1에서 50점 (레벨 2까지 100점 필요)
        
        // when
        double progress = userStats.getLevelProgress(testLevels);
        
        // then
        assertThat(progress).isEqualTo(0.5); // 50% 진행
    }
    
    @Test
    @DisplayName("최고 레벨에서는 진행률이 100%가 된다")
    void getLevelProgress_MaxLevel_Returns100Percent() {
        // given
        userStats.addPoints(10000, testLevels); // 최고 레벨 달성
        
        // when
        double progress = userStats.getLevelProgress(testLevels);
        
        // then
        assertThat(progress).isEqualTo(1.0); // 100%
    }
    
    @Test
    @DisplayName("여러 번의 점수 추가로 여러 레벨을 한 번에 상승시킬 수 있다")
    void addPoints_MultipleLevel_LevelUp() {
        // when
        boolean levelUp = userStats.addPoints(1200, testLevels); // 레벨 5까지 한 번에 상승
        
        // then
        assertThat(levelUp).isTrue();
        assertThat(userStats.getCurrentLevel().getLevelName()).isEqualTo("전문가");
        assertThat(userStats.getTotalPoints()).isEqualTo(1200);
    }
    
    @Test
    @DisplayName("레벨업 시 levelUpdatedAt이 업데이트된다")
    void addPoints_LevelUp_UpdatesLevelUpdatedAt() throws InterruptedException {
        // given
        java.time.LocalDateTime beforeLevelUp = java.time.LocalDateTime.now();
        java.lang.Thread.sleep(1); // 1ms 대기
        
        // when
        userStats.addPoints(100, testLevels); // 레벨업 발생
        
        // then
        assertThat(userStats.getLevelUpdatedAt()).isNotNull();
        assertThat(userStats.getLevelUpdatedAt()).isAfter(beforeLevelUp);
    }
    
    @Test
    @DisplayName("레벨업이 발생하지 않으면 levelUpdatedAt이 변경되지 않는다")
    void addPoints_NoLevelUp_LevelUpdatedAtUnchanged() {
        // given
        userStats.addPoints(50, testLevels); // 레벨업 없음 (50점)
        java.time.LocalDateTime beforeAddPoints = userStats.getLevelUpdatedAt();
        
        // when
        userStats.addPoints(20, testLevels); // 여전히 레벨업 없음 (총 70점)
        
        // then
        assertThat(userStats.getLevelUpdatedAt()).isEqualTo(beforeAddPoints);
    }
    
    @Test
    @DisplayName("연속 레벨업이 발생할 수 있다")
    void addPoints_ConsecutiveLevelUps() {
        // given
        userStats.addPoints(50, testLevels); // 레벨 1, 50점
        
        // when
        boolean levelUp1 = userStats.addPoints(50, testLevels); // 레벨 2 달성
        boolean levelUp2 = userStats.addPoints(200, testLevels); // 레벨 3 달성
        
        // then
        assertThat(levelUp1).isTrue();
        assertThat(levelUp2).isTrue();
        assertThat(userStats.getCurrentLevel().getLevelName()).isEqualTo("발전");
        assertThat(userStats.getTotalPoints()).isEqualTo(300);
    }
    
    @Test
    @DisplayName("현재 레벨 정보가 올바르게 반환된다")
    void getCurrentLevel_ReturnsCorrectLevel() {
        // given
        userStats.addPoints(150, testLevels); // 레벨 2 달성
        
        // when
        UserLevel currentLevel = userStats.getCurrentLevel();
        
        // then
        assertThat(currentLevel.getLevelName()).isEqualTo("성장");
        assertThat(currentLevel.getMinPointsRequired()).isEqualTo(100);
        assertThat(currentLevel.getDiscountRate()).isEqualTo(0.02);
    }
    
    private UserLevel createLevel(String name, int minPoints, double discountRate, String emoji) {
        return UserLevel.builder()
                .levelName(name)
                .minPointsRequired(minPoints)
                .discountRate(discountRate)
                .emoji(emoji)
                .rewardDescription("테스트 보상")
                .build();
    }
}
