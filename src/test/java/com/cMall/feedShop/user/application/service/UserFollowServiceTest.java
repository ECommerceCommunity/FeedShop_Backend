package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.response.FollowToggleResponseDto;
import com.cMall.feedShop.user.application.dto.response.UserFollowCountResponseDto;
import com.cMall.feedShop.user.application.dto.response.UserFollowItemDto;
import com.cMall.feedShop.user.application.dto.response.UserFollowListResponseDto;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserFollow;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserFollowRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserFollowServiceTest {

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserFollowService userFollowService;

    private User follower;
    private User following;
    private UserFollow userFollow;
    private UserProfile userProfile;

    @BeforeEach
    void setUp() {
        // UserProfile 설정
        userProfile = mock(UserProfile.class);
        when(userProfile.getNickname()).thenReturn("테스트유저");

        // User 설정
        follower = mock(User.class);
        following = mock(User.class);
        
        // UserFollow 설정
        userFollow = mock(UserFollow.class);
    }

    @Test
    @DisplayName("팔로우 토글 - 팔로우 추가 성공")
    void toggleFollow_addFollow_success() {
        // given
        when(follower.getId()).thenReturn(1L);
        when(following.getId()).thenReturn(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(userFollowRepository.existsByFollower_IdAndFollowing_Id(1L, 2L)).thenReturn(false);
        when(userFollowRepository.save(any(UserFollow.class))).thenReturn(userFollow);
        when(userFollowRepository.countByFollowing_Id(2L)).thenReturn(1L);
        when(userFollowRepository.countByFollower_Id(1L)).thenReturn(0L);

        // when
        FollowToggleResponseDto result = userFollowService.toggleFollow(1L, 2L);

        // then
        assertThat(result.isFollowing()).isTrue();
        assertThat(result.getFollowerCount()).isEqualTo(1L);
        assertThat(result.getFollowingCount()).isEqualTo(0L);
        assertThat(result.getMessage()).isEqualTo("팔로우가 완료되었습니다.");
        
        verify(userFollowRepository).save(any(UserFollow.class));
        verify(userFollowRepository).countByFollowing_Id(2L);
        verify(userFollowRepository).countByFollower_Id(1L);
    }

    @Test
    @DisplayName("팔로우 토글 - 언팔로우 성공")
    void toggleFollow_removeFollow_success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(userFollowRepository.existsByFollower_IdAndFollowing_Id(1L, 2L)).thenReturn(true);
        when(userFollowRepository.countByFollowing_Id(2L)).thenReturn(0L);
        when(userFollowRepository.countByFollower_Id(1L)).thenReturn(0L);

        // when
        FollowToggleResponseDto result = userFollowService.toggleFollow(1L, 2L);

        // then
        assertThat(result.isFollowing()).isFalse();
        assertThat(result.getFollowerCount()).isEqualTo(0L);
        assertThat(result.getFollowingCount()).isEqualTo(0L);
        assertThat(result.getMessage()).isEqualTo("언팔로우가 완료되었습니다.");
        
        verify(userFollowRepository).deleteByFollower_IdAndFollowing_Id(1L, 2L);
        verify(userFollowRepository).countByFollowing_Id(2L);
        verify(userFollowRepository).countByFollower_Id(1L);
    }

    @Test
    @DisplayName("팔로우 토글 - 자기 자신을 팔로우할 수 없음")
    void toggleFollow_selfFollow_throwsException() {
        // when & then
        assertThatThrownBy(() -> userFollowService.toggleFollow(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE)
                .hasMessage("자기 자신을 팔로우할 수 없습니다.");
    }

    @Test
    @DisplayName("팔로우 토글 - 팔로워 사용자가 존재하지 않음")
    void toggleFollow_followerNotFound_throwsException() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userFollowService.toggleFollow(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
                .hasMessage("팔로워 사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("팔로우 토글 - 팔로잉 사용자가 존재하지 않음")
    void toggleFollow_followingNotFound_throwsException() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userFollowService.toggleFollow(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
                .hasMessage("팔로잉 사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("사용자 팔로우 수 조회 - 성공")
    void getUserFollowCount_success() {
        // given
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userFollowRepository.countByFollowing_Id(1L)).thenReturn(5L);
        when(userFollowRepository.countByFollower_Id(1L)).thenReturn(3L);

        // when
        UserFollowCountResponseDto result = userFollowService.getUserFollowCount(1L);

        // then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getFollowerCount()).isEqualTo(5L);
        assertThat(result.getFollowingCount()).isEqualTo(3L);
        
        verify(userRepository).existsById(1L);
        verify(userFollowRepository).countByFollowing_Id(1L);
        verify(userFollowRepository).countByFollower_Id(1L);
    }

    @Test
    @DisplayName("사용자 팔로우 수 조회 - 사용자가 존재하지 않음")
    void getUserFollowCount_userNotFound_throwsException() {
        // given
        when(userRepository.existsById(1L)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userFollowService.getUserFollowCount(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("사용자 팔로워 목록 조회 - 성공")
    void getFollowers_success() {
        // given
        when(userRepository.existsById(1L)).thenReturn(true);
        
        // UserFollow mock 설정
        when(userFollow.getFollower()).thenReturn(following);
        when(following.getId()).thenReturn(2L);
        when(following.getUserProfile()).thenReturn(userProfile);
        when(userFollow.getCreatedAt()).thenReturn(LocalDateTime.now());
        
        Page<UserFollow> followersPage = new PageImpl<>(List.of(userFollow));
        when(userFollowRepository.findFollowersByUserId(eq(1L), any(Pageable.class))).thenReturn(followersPage);

        // when
        UserFollowListResponseDto result = userFollowService.getFollowers(1L, 0, 20);

        // then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsers()).hasSize(1);
        assertThat(result.getPagination()).isNotNull();
        assertThat(result.getPagination().getTotalElements()).isEqualTo(1L);
        
        verify(userRepository).existsById(1L);
        verify(userFollowRepository).findFollowersByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("사용자 팔로잉 목록 조회 - 성공")
    void getFollowings_success() {
        // given
        when(userRepository.existsById(1L)).thenReturn(true);
        
        // UserFollow mock 설정
        when(userFollow.getFollowing()).thenReturn(following);
        when(following.getId()).thenReturn(2L);
        when(following.getUserProfile()).thenReturn(userProfile);
        when(userFollow.getCreatedAt()).thenReturn(LocalDateTime.now());
        
        Page<UserFollow> followingsPage = new PageImpl<>(List.of(userFollow));
        when(userFollowRepository.findFollowingsByUserId(eq(1L), any(Pageable.class))).thenReturn(followingsPage);

        // when
        UserFollowListResponseDto result = userFollowService.getFollowings(1L, 0, 20);

        // then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsers()).hasSize(1);
        assertThat(result.getPagination()).isNotNull();
        assertThat(result.getPagination().getTotalElements()).isEqualTo(1L);
        
        verify(userRepository).existsById(1L);
        verify(userFollowRepository).findFollowingsByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("사용자 팔로워 목록 조회 - 사용자가 존재하지 않음")
    void getFollowers_userNotFound_throwsException() {
        // given
        when(userRepository.existsById(1L)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userFollowService.getFollowers(1L, 0, 20))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("사용자 팔로잉 목록 조회 - 사용자가 존재하지 않음")
    void getFollowings_userNotFound_throwsException() {
        // given
        when(userRepository.existsById(1L)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userFollowService.getFollowings(1L, 0, 20))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("팔로우 상태 확인 - 팔로우 중")
    void isFollowing_following_returnsTrue() {
        // given
        when(userFollowRepository.existsByFollower_IdAndFollowing_Id(1L, 2L)).thenReturn(true);

        // when
        boolean result = userFollowService.isFollowing(1L, 2L);

        // then
        assertThat(result).isTrue();
        verify(userFollowRepository).existsByFollower_IdAndFollowing_Id(1L, 2L);
    }

    @Test
    @DisplayName("팔로우 상태 확인 - 팔로우하지 않음")
    void isFollowing_notFollowing_returnsFalse() {
        // given
        when(userFollowRepository.existsByFollower_IdAndFollowing_Id(1L, 2L)).thenReturn(false);

        // when
        boolean result = userFollowService.isFollowing(1L, 2L);

        // then
        assertThat(result).isFalse();
        verify(userFollowRepository).existsByFollower_IdAndFollowing_Id(1L, 2L);
    }

    @Test
    @DisplayName("팔로우 상태 확인 - null 값 처리")
    void isFollowing_nullValues_returnsFalse() {
        // when
        boolean result1 = userFollowService.isFollowing(null, 2L);
        boolean result2 = userFollowService.isFollowing(1L, null);

        // then
        assertThat(result1).isFalse();
        assertThat(result2).isFalse();
    }

    @Test
    @DisplayName("팔로잉 사용자 ID 목록 조회 - 성공")
    void getFollowingUserIds_success() {
        // given
        List<Long> followingIds = List.of(2L, 3L, 4L);
        when(userFollowRepository.findFollowingUserIdsByUserId(1L)).thenReturn(followingIds);

        // when
        List<Long> result = userFollowService.getFollowingUserIds(1L);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(2L, 3L, 4L);
        verify(userFollowRepository).findFollowingUserIdsByUserId(1L);
    }

    @Test
    @DisplayName("팔로잉 사용자 ID 목록 조회 - null 값 처리")
    void getFollowingUserIds_nullUserId_returnsEmptyList() {
        // when
        List<Long> result = userFollowService.getFollowingUserIds(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("팔로워 사용자 ID 목록 조회 - 성공")
    void getFollowerUserIds_success() {
        // given
        List<Long> followerIds = List.of(5L, 6L, 7L);
        when(userFollowRepository.findFollowerUserIdsByUserId(1L)).thenReturn(followerIds);

        // when
        List<Long> result = userFollowService.getFollowerUserIds(1L);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(5L, 6L, 7L);
        verify(userFollowRepository).findFollowerUserIdsByUserId(1L);
    }

    @Test
    @DisplayName("팔로워 사용자 ID 목록 조회 - null 값 처리")
    void getFollowerUserIds_nullUserId_returnsEmptyList() {
        // when
        List<Long> result = userFollowService.getFollowerUserIds(null);

        // then
        assertThat(result).isEmpty();
    }
}
