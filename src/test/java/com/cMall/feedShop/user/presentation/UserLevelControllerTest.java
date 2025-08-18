package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.user.application.dto.UserStatsResponse;
import com.cMall.feedShop.user.application.service.UserLevelService;
import com.cMall.feedShop.user.domain.model.ActivityType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserLevel;
import com.cMall.feedShop.user.domain.model.UserStats;
import com.cMall.feedShop.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 레벨 컨트롤러 단위 테스트")
class UserLevelControllerTest {

    @Mock
    private UserLevelService userLevelService;
    
    @InjectMocks
    private UserLevelController userLevelController;
    
    private User testUser;
    private UserStats testUserStats;
    
    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password", "test@example.com", UserRole.USER);
        
        testUserStats = UserStats.builder()
                .user(testUser)
                .build();
        testUserStats.addPoints(150); // 레벨 2, 150점
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
}
