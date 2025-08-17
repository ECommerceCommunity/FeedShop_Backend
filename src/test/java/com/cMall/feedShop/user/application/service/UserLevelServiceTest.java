package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.domain.model.*;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.repository.UserActivityRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.repository.UserStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("사용자 레벨 서비스 테스트")
class UserLevelServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserStatsRepository userStatsRepository;
    
    @Mock
    private UserActivityRepository userActivityRepository;
    
    @Mock
    private BadgeService badgeService;
    
    @InjectMocks
    private UserLevelService userLevelService;
    
    private User testUser;
    private UserStats testUserStats;
    
    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password", "test@example.com", UserRole.USER);
        
        testUserStats = UserStats.builder()
                .user(testUser)
                .build();
    }
    
    @Test
    @DisplayName("사용자 활동을 기록하고 점수를 부여할 수 있다")
    void recordActivity_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userActivityRepository.existsByUserAndReferenceIdAndReferenceType(
                testUser, 100L, "ORDER")).willReturn(false);
        given(userStatsRepository.findByUser(testUser)).willReturn(Optional.of(testUserStats));
        
        // when
        userLevelService.recordActivity(1L, ActivityType.PURCHASE_COMPLETION, 
                "구매 완료", 100L, "ORDER");
        
        // then
        verify(userActivityRepository).save(any(UserActivity.class));
        verify(userStatsRepository).save(testUserStats);
    }
    
    @Test
    @DisplayName("중복된 활동은 기록되지 않는다")
    void recordActivity_Duplicate_NotRecorded() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userActivityRepository.existsByUserAndReferenceIdAndReferenceType(
                testUser, 100L, "ORDER")).willReturn(true);
        
        // when
        userLevelService.recordActivity(1L, ActivityType.PURCHASE_COMPLETION, 
                "구매 완료", 100L, "ORDER");
        
        // then
        verify(userActivityRepository, never()).save(any(UserActivity.class));
    }
    
    @Test
    @DisplayName("사용자 통계가 없으면 새로 생성한다")
    void getOrCreateUserStats_CreateNew() {
        // given
        given(userStatsRepository.findByUser(testUser)).willReturn(Optional.empty());
        given(userStatsRepository.save(any(UserStats.class))).willReturn(testUserStats);
        
        // when
        UserStats result = userLevelService.getOrCreateUserStats(testUser);
        
        // then
        assertThat(result).isNotNull();
        verify(userStatsRepository).save(any(UserStats.class));
    }
    
    @Test
    @DisplayName("사용자 현재 레벨과 점수를 조회할 수 있다")
    void getUserStats_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userStatsRepository.findByUser(testUser)).willReturn(Optional.of(testUserStats));
        
        // when
        UserStats result = userLevelService.getUserStats(1L);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentLevel()).isEqualTo(UserLevel.LEVEL_1);
        assertThat(result.getTotalPoints()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("점수 추가 시 레벨업이 발생하면 레벨 관련 뱃지가 수여된다")
    void recordActivity_LevelUp_AwardsLevelBadge() {
        // given
        testUserStats.addPoints(95); // 레벨업 직전 상태
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        // referenceId와 referenceType이 null이므로 중복 체크가 실행되지 않음
        given(userStatsRepository.findByUser(testUser)).willReturn(Optional.of(testUserStats));
        
        // when - 10점 추가로 레벨 2 달성 (총 105점)
        userLevelService.recordActivity(1L, ActivityType.REVIEW_CREATION, 
                "리뷰 작성", null, "REVIEW");
        
        // then
        assertThat(testUserStats.getCurrentLevel()).isEqualTo(UserLevel.LEVEL_2);
        verify(badgeService).awardBadge(1L, BadgeType.EARLY_ADOPTER);
    }
}
