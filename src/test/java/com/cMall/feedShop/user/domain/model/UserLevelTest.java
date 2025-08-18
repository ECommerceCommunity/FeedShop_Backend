package com.cMall.feedShop.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("사용자 레벨 열거형 테스트")
class UserLevelTest {

    @ParameterizedTest
    @CsvSource({
        "0, LEVEL_1",
        "50, LEVEL_1", 
        "100, LEVEL_2",
        "299, LEVEL_2",
        "300, LEVEL_3",
        "1000, LEVEL_5",
        "5500, LEVEL_10",
        "10000, LEVEL_10"
    })
    @DisplayName("점수에 따라 올바른 레벨을 반환한다")
    void fromPoints_ReturnsCorrectLevel(int points, UserLevel expectedLevel) {
        // when
        UserLevel result = UserLevel.fromPoints(points);
        
        // then
        assertThat(result).isEqualTo(expectedLevel);
    }
    
    @Test
    @DisplayName("레벨 1에서 다음 레벨까지 필요한 점수를 계산한다")
    void getPointsToNextLevel_Level1() {
        // given
        int currentPoints = 50;
        
        // when
        int pointsToNext = UserLevel.LEVEL_1.getPointsToNextLevel(currentPoints);
        
        // then
        assertThat(pointsToNext).isEqualTo(50); // 레벨 2까지 50점 더 필요
    }
    
    @Test
    @DisplayName("레벨 2에서 다음 레벨까지 필요한 점수를 계산한다")
    void getPointsToNextLevel_Level2() {
        // given
        int currentPoints = 150;
        
        // when
        int pointsToNext = UserLevel.LEVEL_2.getPointsToNextLevel(currentPoints);
        
        // then
        assertThat(pointsToNext).isEqualTo(150); // 레벨 3까지 150점 더 필요
    }
    
    @Test
    @DisplayName("최고 레벨에서는 다음 레벨까지 필요한 점수가 0이다")
    void getPointsToNextLevel_MaxLevel() {
        // given
        int currentPoints = 6000;
        
        // when
        int pointsToNext = UserLevel.LEVEL_10.getPointsToNextLevel(currentPoints);
        
        // then
        assertThat(pointsToNext).isEqualTo(0);
    }
    
    @Test
    @DisplayName("레벨 표시 이름이 올바르게 생성된다")
    void getDisplayName_FormatsCorrectly() {
        // when & then
        assertThat(UserLevel.LEVEL_1.getDisplayName()).isEqualTo("Lv.1 🌱 새싹");
        assertThat(UserLevel.LEVEL_5.getDisplayName()).isEqualTo("Lv.5 👑 전문가");
        assertThat(UserLevel.LEVEL_10.getDisplayName()).isEqualTo("Lv.10 🚀 갓");
    }
    
    @Test
    @DisplayName("각 레벨의 기본 정보가 올바르게 설정되어 있다")
    void levelProperties_AreCorrect() {
        // Level 1
        assertThat(UserLevel.LEVEL_1.getLevelNumber()).isEqualTo(0);
        assertThat(UserLevel.LEVEL_1.getRequiredPoints()).isEqualTo(0);
        assertThat(UserLevel.LEVEL_1.getName()).isEqualTo("새싹");
        assertThat(UserLevel.LEVEL_1.getEmoji()).isEqualTo("🌱");
        
        // Level 5
        assertThat(UserLevel.LEVEL_5.getLevelNumber()).isEqualTo(4);
        assertThat(UserLevel.LEVEL_5.getRequiredPoints()).isEqualTo(1000);
        assertThat(UserLevel.LEVEL_5.getName()).isEqualTo("전문가");
        assertThat(UserLevel.LEVEL_5.getEmoji()).isEqualTo("👑");
        
        // Level 10
        assertThat(UserLevel.LEVEL_10.getLevelNumber()).isEqualTo(9);
        assertThat(UserLevel.LEVEL_10.getRequiredPoints()).isEqualTo(5500);
        assertThat(UserLevel.LEVEL_10.getName()).isEqualTo("갓");
        assertThat(UserLevel.LEVEL_10.getEmoji()).isEqualTo("🚀");
    }
}
