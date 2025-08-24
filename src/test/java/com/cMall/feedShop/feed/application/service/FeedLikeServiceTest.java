package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.response.LikeToggleResponseDto;
import com.cMall.feedShop.feed.application.dto.response.LikeUserResponseDto;
import com.cMall.feedShop.feed.application.dto.response.MyLikedFeedsResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.entity.FeedLike;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedLikeRepository;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedLikeServiceTest {

    @Mock
    private FeedLikeRepository feedLikeRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private FeedLikeService feedLikeService;

    private User user;
    private Feed feed;
    private FeedLike feedLike;
    private UserProfile userProfile;

    @BeforeEach
    void setUp() {
        userProfile = UserProfile.builder()
                .nickname("테스트유저")
                .build();

        // User를 Mock으로 생성 (stub 없이)
        user = mock(User.class);

        // Feed를 Mock으로 생성 (stub 없이)
        feed = mock(Feed.class);
        
        feedLike = FeedLike.builder()
                .feed(feed)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요 추가 성공")
    void toggleLike_addLike_success() {
        // given
        when(feed.isDeleted()).thenReturn(false);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(feedRepository.findById(anyLong())).thenReturn(Optional.of(feed));
        when(feedLikeRepository.existsByFeed_IdAndUser_Id(anyLong(), anyLong())).thenReturn(false);
        when(feedLikeRepository.save(any(FeedLike.class))).thenReturn(feedLike);

        // when
        LikeToggleResponseDto result = feedLikeService.toggleLike(1L, 1L);

        // then
        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikeCount()).isEqualTo(0);
        verify(feed).incrementLikeCount();
        verify(feedLikeRepository).save(any(FeedLike.class));
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요 취소 성공")
    void toggleLike_removeLike_success() {
        // given
        when(feed.isDeleted()).thenReturn(false);
        when(feed.getLikeCount()).thenReturn(1);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(feedRepository.findById(anyLong())).thenReturn(Optional.of(feed));
        when(feedLikeRepository.existsByFeed_IdAndUser_Id(anyLong(), anyLong())).thenReturn(true);

        // when
        LikeToggleResponseDto result = feedLikeService.toggleLike(1L, 1L);

        // then
        assertThat(result.isLiked()).isFalse();
        assertThat(result.getLikeCount()).isEqualTo(1);
        verify(feed).decrementLikeCount();
        verify(feedLikeRepository).deleteByFeed_IdAndUser_Id(1L, 1L);
    }

    @Test
    @DisplayName("좋아요 토글 - 사용자가 존재하지 않는 경우")
    void toggleLike_userNotFound_throwsException() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedLikeService.toggleLike(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("좋아요 토글 - 피드가 존재하지 않는 경우")
    void toggleLike_feedNotFound_throwsException() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(feedRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedLikeService.toggleLike(1L, 1L))
                .isInstanceOf(FeedNotFoundException.class);
    }

    @Test
    @DisplayName("좋아요 토글 - 삭제된 피드인 경우")
    void toggleLike_deletedFeed_throwsException() {
        // given
        when(feed.isDeleted()).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(feedRepository.findById(anyLong())).thenReturn(Optional.of(feed));

        // when & then
        assertThatThrownBy(() -> feedLikeService.toggleLike(1L, 1L))
                .isInstanceOf(FeedNotFoundException.class);
    }

    @Test
    @DisplayName("좋아요 토글 - userId가 null인 경우")
    void toggleLike_nullUserId_throwsException() {
        // when & then
        assertThatThrownBy(() -> feedLikeService.toggleLike(1L, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("좋아요 사용자 목록 조회 - 성공")
    void getLikedUsers_success() {
        // given
        when(feed.isDeleted()).thenReturn(false);
        when(feedRepository.findById(anyLong())).thenReturn(Optional.of(feed));
        
        Page<FeedLike> feedLikePage = new PageImpl<>(List.of(feedLike));
        when(feedLikeRepository.findByFeedIdWithUser(anyLong(), any(Pageable.class))).thenReturn(feedLikePage);

        // when
        PaginatedResponse<LikeUserResponseDto> result = feedLikeService.getLikedUsers(1L, 0, 20);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(feedLikeRepository).findByFeedIdWithUser(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("좋아요 사용자 목록 조회 - 피드가 존재하지 않는 경우")
    void getLikedUsers_feedNotFound_throwsException() {
        // given
        when(feedRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedLikeService.getLikedUsers(1L, 0, 20))
                .isInstanceOf(FeedNotFoundException.class);
    }

    @Test
    @DisplayName("좋아요 사용자 목록 조회 - 삭제된 피드인 경우")
    void getLikedUsers_deletedFeed_throwsException() {
        // given
        when(feed.isDeleted()).thenReturn(true);
        when(feedRepository.findById(anyLong())).thenReturn(Optional.of(feed));

        // when & then
        assertThatThrownBy(() -> feedLikeService.getLikedUsers(1L, 0, 20))
                .isInstanceOf(FeedNotFoundException.class);
    }

    @Test
    @DisplayName("내가 좋아요한 피드 목록 조회 - 성공")
    void getMyLikedFeeds_success() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        
        // Feed mock 설정
        when(feed.getId()).thenReturn(1L);
        when(feed.getTitle()).thenReturn("테스트 피드");
        when(feed.getContent()).thenReturn("테스트 내용");
        when(feed.getFeedType()).thenReturn(FeedType.DAILY);
        when(feed.getLikeCount()).thenReturn(5);
        when(feed.getCommentCount()).thenReturn(3);
        when(feed.getUser()).thenReturn(user);
        
        // UserProfile mock 설정
        when(user.getUserProfile()).thenReturn(userProfile);
        
        Page<FeedLike> feedLikePage = new PageImpl<>(List.of(feedLike));
        when(feedLikeRepository.findByUserIdWithFeed(anyLong(), any(Pageable.class))).thenReturn(feedLikePage);

        // when
        MyLikedFeedsResponseDto result = feedLikeService.getMyLikedFeeds(1L, 0, 20);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFeedId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 피드");
        verify(feedLikeRepository).findByUserIdWithFeed(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("내가 좋아요한 피드 목록 조회 - 사용자가 존재하지 않는 경우")
    void getMyLikedFeeds_userNotFound_throwsException() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedLikeService.getMyLikedFeeds(1L, 0, 20))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("내가 좋아요한 피드 ID 목록 조회 - 성공")
    void getMyLikedFeedIds_success() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(feedLikeRepository.findFeedIdsByUserId(anyLong())).thenReturn(List.of(1L, 2L, 3L));

        // when
        List<Long> result = feedLikeService.getMyLikedFeedIds(1L);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("내가 좋아요한 피드 ID 목록 조회 - 사용자가 존재하지 않는 경우")
    void getMyLikedFeedIds_userNotFound_returnsEmptyList() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        List<Long> result = feedLikeService.getMyLikedFeedIds(1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자별 좋아요 상태 확인 - 좋아요한 경우")
    void isLikedByUser_liked_returnsTrue() {
        // given
        when(feedLikeRepository.existsByFeed_IdAndUser_Id(anyLong(), anyLong())).thenReturn(true);

        // when
        boolean result = feedLikeService.isLikedByUser(1L, 1L);

        // then
        assertThat(result).isTrue();
        verify(feedLikeRepository).existsByFeed_IdAndUser_Id(1L, 1L);
    }

    @Test
    @DisplayName("사용자별 좋아요 상태 확인 - 좋아요하지 않은 경우")
    void isLikedByUser_notLiked_returnsFalse() {
        // given
        when(feedLikeRepository.existsByFeed_IdAndUser_Id(anyLong(), anyLong())).thenReturn(false);

        // when
        boolean result = feedLikeService.isLikedByUser(1L, 1L);

        // then
        assertThat(result).isFalse();
        verify(feedLikeRepository).existsByFeed_IdAndUser_Id(1L, 1L);
    }

    @Test
    @DisplayName("사용자별 좋아요 상태 확인 - 예외 발생 시 false 반환")
    void isLikedByUser_exception_returnsFalse() {
        // given
        when(feedLikeRepository.existsByFeed_IdAndUser_Id(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        // when
        boolean result = feedLikeService.isLikedByUser(1L, 1L);

        // then
        assertThat(result).isFalse();
        verify(feedLikeRepository).existsByFeed_IdAndUser_Id(1L, 1L);
    }
}
