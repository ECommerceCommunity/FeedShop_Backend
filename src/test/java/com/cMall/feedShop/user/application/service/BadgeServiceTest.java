package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.BadgeListResponse;
import com.cMall.feedShop.user.application.dto.BadgeResponse;
import com.cMall.feedShop.user.domain.model.BadgeType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserBadge;
import com.cMall.feedShop.user.domain.repository.UserBadgeRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("뱃지 서비스 테스트")
class BadgeServiceTest {

    @Mock
    private UserBadgeRepository userBadgeRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private BadgeService badgeService;
    
    private User testUser;
    private UserBadge testBadge;
    
    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password", "test@example.com", UserRole.USER);
        
        testBadge = UserBadge.builder()
                .user(testUser)
                .badgeType(BadgeType.FIRST_PURCHASE)
                .awardedAt(LocalDateTime.now())
                .isDisplayed(true)
                .build();
    }
    
    @Test
    @DisplayName("사용자의 모든 뱃지를 조회할 수 있다")
    void getUserBadges_Success() {
        // given
        List<UserBadge> badges = Arrays.asList(testBadge);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userBadgeRepository.findByUserOrderByAwardedAtDesc(testUser)).willReturn(badges);
        given(userBadgeRepository.countByUser(testUser)).willReturn(1L);
        given(userBadgeRepository.countByUserAndIsDisplayedTrue(testUser)).willReturn(1L);
        
        // when
        BadgeListResponse response = badgeService.getUserBadges(1L);
        
        // then
        assertThat(response.getBadges()).hasSize(1);
        assertThat(response.getTotalCount()).isEqualTo(1L);
        assertThat(response.getDisplayedCount()).isEqualTo(1L);
        assertThat(response.getBadges().get(0).getBadgeName()).isEqualTo("첫 구매");
    }
    
    @Test
    @DisplayName("뱃지를 성공적으로 수여할 수 있다")
    void awardBadge_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userBadgeRepository.existsByUserAndBadgeType(testUser, BadgeType.FIRST_PURCHASE))
                .willReturn(false);
        given(userBadgeRepository.save(any(UserBadge.class))).willReturn(testBadge);
        
        // when
        BadgeResponse response = badgeService.awardBadge(1L, BadgeType.FIRST_PURCHASE);
        
        // then
        assertThat(response.getBadgeName()).isEqualTo("첫 구매");
        assertThat(response.getBadgeType()).isEqualTo(BadgeType.FIRST_PURCHASE);
        verify(userBadgeRepository).save(any(UserBadge.class));
    }
    
    @Test
    @DisplayName("이미 보유한 뱃지는 중복 수여되지 않는다")
    void awardBadge_AlreadyExists_ThrowsException() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userBadgeRepository.existsByUserAndBadgeType(testUser, BadgeType.FIRST_PURCHASE))
                .willReturn(true);
        
        // when & then
        assertThatThrownBy(() -> badgeService.awardBadge(1L, BadgeType.FIRST_PURCHASE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 보유한 뱃지입니다.");
    }
    
    @Test
    @DisplayName("뱃지 표시/숨김을 토글할 수 있다")
    void toggleBadgeDisplay_Success() {
        // given
        given(userBadgeRepository.findById(1L)).willReturn(Optional.of(testBadge));
        
        // when
        BadgeResponse response = badgeService.toggleBadgeDisplay(1L, 1L);
        
        // then
        assertThat(response.getIsDisplayed()).isFalse(); // 토글되어 false가 됨
    }
    
    @Test
    @DisplayName("구매 관련 뱃지를 자동으로 체크하고 수여한다")
    void checkAndAwardPurchaseBadges_FirstPurchase() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userBadgeRepository.existsByUserAndBadgeType(testUser, BadgeType.FIRST_PURCHASE))
                .willReturn(false);
        given(userBadgeRepository.save(any(UserBadge.class))).willReturn(testBadge);
        
        // when
        badgeService.checkAndAwardPurchaseBadges(1L, 1L, 50000L);
        
        // then
        verify(userBadgeRepository).save(any(UserBadge.class));
    }
    
    @Test
    @DisplayName("리뷰 관련 뱃지를 자동으로 체크하고 수여한다")
    void checkAndAwardReviewBadges_FirstReview() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userBadgeRepository.existsByUserAndBadgeType(testUser, BadgeType.FIRST_REVIEW))
                .willReturn(false);
        given(userBadgeRepository.save(any(UserBadge.class))).willReturn(testBadge);
        
        // when
        badgeService.checkAndAwardReviewBadges(1L, 1L);
        
        // then
        verify(userBadgeRepository).save(any(UserBadge.class));
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자에게 뱃지 수여 시 예외가 발생한다")
    void awardBadge_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> badgeService.awardBadge(1L, BadgeType.FIRST_PURCHASE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }
}
