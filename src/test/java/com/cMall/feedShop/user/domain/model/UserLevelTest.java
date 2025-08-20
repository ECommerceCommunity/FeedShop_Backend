package com.cMall.feedShop.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("사용자 레벨 엔티티 테스트")
class UserLevelTest {

    @Test
    @DisplayName("점수에 따라 올바른 레벨을 반환한다")
    void fromPoints_ReturnsCorrectLevel() {
        // given
        List<UserLevel> levels = createTestLevels();
        
        // when & then
        assertThat(UserLevel.fromPoints(0, levels).getLevelName()).isEqualTo("새싹");
        assertThat(UserLevel.fromPoints(50, levels).getLevelName()).isEqualTo("새싹");
        assertThat(UserLevel.fromPoints(100, levels).getLevelName()).isEqualTo("성장");
        assertThat(UserLevel.fromPoints(299, levels).getLevelName()).isEqualTo("성장");
        assertThat(UserLevel.fromPoints(300, levels).getLevelName()).isEqualTo("발전");
        assertThat(UserLevel.fromPoints(1000, levels).getLevelName()).isEqualTo("전문가");
        assertThat(UserLevel.fromPoints(5500, levels).getLevelName()).isEqualTo("갓");
        assertThat(UserLevel.fromPoints(10000, levels).getLevelName()).isEqualTo("갓");
    }
    
    @Test
    @DisplayName("레벨 1에서 다음 레벨까지 필요한 점수를 계산한다")
    void getPointsToNextLevel_Level1() {
        // given
        List<UserLevel> levels = createTestLevels();
        UserLevel level1 = levels.get(0);
        int currentPoints = 50;
        
        // when
        int pointsToNext = level1.getPointsToNextLevel(currentPoints, levels);
        
        // then
        assertThat(pointsToNext).isEqualTo(50); // 레벨 2까지 50점 더 필요
    }
    
    @Test
    @DisplayName("레벨 2에서 다음 레벨까지 필요한 점수를 계산한다")
    void getPointsToNextLevel_Level2() {
        // given
        List<UserLevel> levels = createTestLevels();
        UserLevel level2 = levels.get(1);
        int currentPoints = 150;
        
        // when
        int pointsToNext = level2.getPointsToNextLevel(currentPoints, levels);
        
        // then
        assertThat(pointsToNext).isEqualTo(150); // 레벨 3까지 150점 더 필요
    }
    
    @Test
    @DisplayName("최고 레벨에서는 다음 레벨까지 필요한 점수가 0이다")
    void getPointsToNextLevel_MaxLevel() {
        // given
        List<UserLevel> levels = createTestLevels();
        UserLevel level10 = levels.get(9);
        int currentPoints = 6000;
        
        // when
        int pointsToNext = level10.getPointsToNextLevel(currentPoints, levels);
        
        // then
        assertThat(pointsToNext).isEqualTo(0);
    }
    
    @Test
    @DisplayName("레벨 표시 이름이 올바르게 생성된다")
    void getDisplayName_FormatsCorrectly() {
        // given
        UserLevel level1 = UserLevel.builder()
                .levelName("새싹")
                .minPointsRequired(0)
                .discountRate(0.0)
                .emoji("🌱")
                .rewardDescription("새로운 시작")
                .build();
        
        UserLevel level5 = UserLevel.builder()
                .levelName("전문가")
                .minPointsRequired(1000)
                .discountRate(0.10)
                .emoji("👑")
                .rewardDescription("이벤트 우선 참여권")
                .build();
        
        // when & then
        assertThat(level1.getDisplayName()).contains("🌱 새싹");
        assertThat(level5.getDisplayName()).contains("👑 전문가");
    }
    
    @Test
    @DisplayName("각 레벨의 기본 정보가 올바르게 설정되어 있다")
    void levelProperties_AreCorrect() {
        // given
        UserLevel level1 = UserLevel.builder()
                .levelName("새싹")
                .minPointsRequired(0)
                .discountRate(0.0)
                .emoji("🌱")
                .rewardDescription("새로운 시작")
                .build();
        
        UserLevel level5 = UserLevel.builder()
                .levelName("전문가")
                .minPointsRequired(1000)
                .discountRate(0.10)
                .emoji("👑")
                .rewardDescription("이벤트 우선 참여권")
                .build();
        
        // when & then
        assertThat(level1.getMinPointsRequired()).isEqualTo(0);
        assertThat(level1.getLevelName()).isEqualTo("새싹");
        assertThat(level1.getEmoji()).isEqualTo("🌱");
        
        assertThat(level5.getMinPointsRequired()).isEqualTo(1000);
        assertThat(level5.getLevelName()).isEqualTo("전문가");
        assertThat(level5.getEmoji()).isEqualTo("👑");
    }
    
    private List<UserLevel> createTestLevels() {
        return Arrays.asList(
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
