package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.model.UserLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 레벨 리포지토리 테스트")
class UserLevelRepositoryTest {

    @Mock
    private UserLevelRepository userLevelRepository;

    @Test
    @DisplayName("모든 레벨을 점수 순으로 조회할 수 있다")
    void findAllOrderByMinPointsRequired() {
        // given
        UserLevel level1 = UserLevel.builder()
                .levelName("새싹")
                .minPointsRequired(0)
                .discountRate(0.0)
                .emoji("🌱")
                .rewardDescription("새로운 시작")
                .build();
        
        UserLevel level2 = UserLevel.builder()
                .levelName("성장")
                .minPointsRequired(100)
                .discountRate(0.02)
                .emoji("🌿")
                .rewardDescription("포인트 지급")
                .build();
        
        List<UserLevel> expectedLevels = Arrays.asList(level1, level2);
        when(userLevelRepository.findAllOrderByMinPointsRequired()).thenReturn(expectedLevels);

        // when
        List<UserLevel> result = userLevelRepository.findAllOrderByMinPointsRequired();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMinPointsRequired()).isEqualTo(0);
        assertThat(result.get(1).getMinPointsRequired()).isEqualTo(100);
    }

    @Test
    @DisplayName("특정 점수에 해당하는 레벨들을 조회할 수 있다")
    void findLevelsByPoints() {
        // given
        UserLevel level1 = UserLevel.builder()
                .levelName("새싹")
                .minPointsRequired(0)
                .discountRate(0.0)
                .emoji("🌱")
                .rewardDescription("새로운 시작")
                .build();
        
        UserLevel level2 = UserLevel.builder()
                .levelName("성장")
                .minPointsRequired(100)
                .discountRate(0.02)
                .emoji("🌿")
                .rewardDescription("포인트 지급")
                .build();
        
        List<UserLevel> expectedLevels = Arrays.asList(level2, level1); // 내림차순 정렬
        when(userLevelRepository.findLevelsByPoints(150)).thenReturn(expectedLevels);

        // when
        List<UserLevel> result = userLevelRepository.findLevelsByPoints(150);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMinPointsRequired()).isEqualTo(100); // 내림차순 정렬
        assertThat(result.get(1).getMinPointsRequired()).isEqualTo(0);
    }

    @Test
    @DisplayName("최소 점수로 레벨을 조회할 수 있다")
    void findByMinPointsRequired() {
        // given
        UserLevel level = UserLevel.builder()
                .levelName("성장")
                .minPointsRequired(100)
                .discountRate(0.02)
                .emoji("🌿")
                .rewardDescription("포인트 지급")
                .build();
        
        when(userLevelRepository.findByMinPointsRequired(100)).thenReturn(Optional.of(level));

        // when
        Optional<UserLevel> result = userLevelRepository.findByMinPointsRequired(100);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getLevelName()).isEqualTo("성장");
    }

    @Test
    @DisplayName("레벨 이름으로 레벨을 조회할 수 있다")
    void findByLevelName() {
        // given
        UserLevel level = UserLevel.builder()
                .levelName("전문가")
                .minPointsRequired(1000)
                .discountRate(0.10)
                .emoji("👑")
                .rewardDescription("이벤트 우선 참여권")
                .build();
        
        when(userLevelRepository.findByLevelName("전문가")).thenReturn(Optional.of(level));

        // when
        Optional<UserLevel> result = userLevelRepository.findByLevelName("전문가");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMinPointsRequired()).isEqualTo(1000);
    }

    @Test
    @DisplayName("최고 레벨을 조회할 수 있다")
    void findTopLevel() {
        // given
        UserLevel level1 = UserLevel.builder()
                .levelName("새싹")
                .minPointsRequired(0)
                .discountRate(0.0)
                .emoji("🌱")
                .rewardDescription("새로운 시작")
                .build();
        
        UserLevel level2 = UserLevel.builder()
                .levelName("갓")
                .minPointsRequired(5500)
                .discountRate(0.25)
                .emoji("🚀")
                .rewardDescription("모든 혜택")
                .build();
        
        when(userLevelRepository.findTopLevel()).thenReturn(Optional.of(level2));

        // when
        Optional<UserLevel> result = userLevelRepository.findTopLevel();

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getLevelName()).isEqualTo("갓");
        assertThat(result.get().getMinPointsRequired()).isEqualTo(5500);
    }
}
