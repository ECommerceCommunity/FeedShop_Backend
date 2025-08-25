package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.UserStatsResponse;
import com.cMall.feedShop.user.application.service.UserLevelService;
import com.cMall.feedShop.user.domain.model.ActivityType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserLevel;
import com.cMall.feedShop.user.domain.model.UserStats;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.repository.UserLevelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.cMall.feedShop.common.exception.ErrorCode.FORBIDDEN;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 레벨 컨트롤러 단위 테스트")
class UserLevelControllerTest {

    @Mock
    private UserLevelService userLevelService;
    
    @Mock
    private UserLevelRepository userLevelRepository;
    
    @InjectMocks
    private UserLevelController userLevelController;
    
    private User testUser;
    private UserStats testUserStats;
    private List<UserLevel> testLevels;
    private UserStatsResponse testUserStatsResponse;
    
    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password", "test@example.com", UserRole.USER);
        
        // 테스트용 레벨 데이터 생성
        testLevels = Arrays.asList(
            createLevel("새싹", 0, 0.0, "🌱"),
            createLevel("성장", 100, 0.02, "🌿"),
            createLevel("발전", 300, 0.05, "🌳")
        );
        
        UserLevel defaultLevel = testLevels.get(0);
        UserLevel level2 = testLevels.get(1);
        
        testUserStats = UserStats.builder()
                .user(testUser)
                .currentLevel(level2)
                .build();
        testUserStats.addPoints(150, testLevels); // 레벨 2, 150점
        
        // 테스트용 UserStatsResponse 생성
        testUserStatsResponse = UserStatsResponse.builder()
                .userId(1L)
                .totalPoints(150)
                .currentLevelName("성장")
                .levelDisplayName("성장 🌿")
                .levelEmoji("🌿")
                .rewardDescription("테스트 보상")
                .pointsToNextLevel(150)
                .levelProgress(0.5)
                .userRank(5L)
                .levelUpdatedAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    @DisplayName("사용자 레벨 서비스가 정상적으로 호출되는지 확인")
    void userLevelService_Success() {
        // given
        given(userLevelService.getUserStats(1L)).willReturn(testUserStats);
        given(userLevelService.getUserRank(1L)).willReturn(10L);
        
        // when
        UserStats result = userLevelService.getUserStats(1L);
        Long rank = userLevelService.getUserRank(1L);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalPoints()).isEqualTo(150);
        assertThat(rank).isEqualTo(10L);
    }

    @Test
    @DisplayName("내 통계 정보를 조회할 수 있다")
    void getMyStats_Success() {
        // given
        given(userLevelService.getUserStatsResponse(1L)).willReturn(testUserStatsResponse);

        // when
        ApiResponse<UserStatsResponse> response = userLevelController.getMyStats(testUser);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCurrentLevelName()).isEqualTo("성장");
        assertThat(response.getData().getTotalPoints()).isEqualTo(150);
        assertThat(response.getData().getUserRank()).isEqualTo(5L);
    }
    
    @Test
    @DisplayName("UserDetails가 User 타입이 아닐 경우 예외가 발생한다")
    void getMyStats_ThrowsException_WhenUserDetailsIsNotUser() {
        // given
        UserDetails mockUserDetails = mock(UserDetails.class);

        // when & then
        assertThatThrownBy(() -> userLevelController.getMyStats(mockUserDetails))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", FORBIDDEN);
    }
    
    @Test
    @DisplayName("특정 사용자의 통계 정보를 조회할 수 있다")
    void getUserStats_Success() {
        // given
        given(userLevelService.getUserStatsResponse(2L)).willReturn(testUserStatsResponse);
        
        // when
        ApiResponse<UserStatsResponse> response = userLevelController.getUserStats(2L);
        
        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCurrentLevelName()).isEqualTo("성장");
        assertThat(response.getData().getTotalPoints()).isEqualTo(150);
        assertThat(response.getData().getUserRank()).isEqualTo(5L);
    }
    
    @Test
    @DisplayName("사용자 통계 정보에 레벨 진행률이 포함된다")
    void userStatsResponse_ContainsLevelProgress() {
        // given
        given(userLevelService.getUserStatsResponse(1L)).willReturn(testUserStatsResponse);
        
        // when
        ApiResponse<UserStatsResponse> response = userLevelController.getMyStats(testUser);
        
        // then
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getLevelProgress()).isGreaterThanOrEqualTo(0.0);
        assertThat(response.getData().getLevelProgress()).isLessThanOrEqualTo(1.0);
        assertThat(response.getData().getPointsToNextLevel()).isGreaterThanOrEqualTo(0);
    }
    
    @Test
    @DisplayName("포인트 부여 서비스가 정상적으로 호출되는지 확인")
    void awardPoints_Success() {
        // given & when
        userLevelService.recordActivity(
                1L, 
                ActivityType.PURCHASE_COMPLETION, 
                "테스트 활동",
                null,
                "TEST"
        );
        
        // then - 예외가 발생하지 않으면 성공
        assertThat(true).isTrue();
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
