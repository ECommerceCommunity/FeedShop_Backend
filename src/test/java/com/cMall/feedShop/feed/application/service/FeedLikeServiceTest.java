package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.response.LikeToggleResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedLikeRepository;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedLikeServiceTest {

    @Mock private FeedLikeRepository feedLikeRepository;
    @Mock private FeedRepository feedRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserDetails userDetails;

    @InjectMocks private FeedLikeService feedLikeService;

    private User user;
    private Feed feed;

    @BeforeEach
    void setUp() {
        user = new User(1L, "login", "pwd", "email@test.com", UserRole.USER);
        OrderItem orderItem = OrderItem.builder()
                .quantity(1)
                .totalPrice(java.math.BigDecimal.valueOf(10000))
                .finalPrice(java.math.BigDecimal.valueOf(9000))
                .build();
        feed = Feed.builder()
                .user(user)
                .orderItem(orderItem)
                .title("t")
                .content("c")
                .instagramId("i")
                .build();
    }

    @Test
    @DisplayName("좋아요 추가 - 존재하지 않으면 생성하고 likeCount 증가")
    void toggleLike_add() {
        Long feedId = 10L;
        when(userDetails.getUsername()).thenReturn("login");
        when(userRepository.findByLoginId("login")).thenReturn(Optional.of(user));
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feedLikeRepository.existsByFeed_IdAndUser_Id(feedId, user.getId())).thenReturn(false);

        LikeToggleResponseDto res = feedLikeService.toggleLike(feedId, userDetails);

        assertThat(res.isLiked()).isTrue();
        assertThat(res.getLikeCount()).isEqualTo(feed.getLikeCount());
        verify(feedLikeRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("좋아요 취소 - 존재하면 삭제하고 likeCount 감소")
    void toggleLike_remove() {
        Long feedId = 11L;
        // 초기 likeCount를 1로 만들어서 감소 확인
        feed.incrementLikeCount();
        when(userDetails.getUsername()).thenReturn("login");
        when(userRepository.findByLoginId("login")).thenReturn(Optional.of(user));
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feedLikeRepository.existsByFeed_IdAndUser_Id(feedId, user.getId())).thenReturn(true);

        LikeToggleResponseDto res = feedLikeService.toggleLike(feedId, userDetails);

        assertThat(res.isLiked()).isFalse();
        assertThat(res.getLikeCount()).isEqualTo(feed.getLikeCount());
        verify(feedLikeRepository, times(1)).deleteByFeed_IdAndUser_Id(feedId, user.getId());
    }

    @Test
    @DisplayName("미인증 401")
    void toggleLike_unauthorized() {
        Long feedId = 12L;
        assertThatThrownBy(() -> feedLikeService.toggleLike(feedId, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    @DisplayName("사용자 없음 USER_NOT_FOUND")
    void toggleLike_userNotFound() {
        Long feedId = 13L;
        when(userDetails.getUsername()).thenReturn("unknown");
        when(userRepository.findByLoginId("unknown")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> feedLikeService.toggleLike(feedId, userDetails))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("피드 없음 404")
    void toggleLike_feedNotFound() {
        Long feedId = 14L;
        when(userDetails.getUsername()).thenReturn("login");
        when(userRepository.findByLoginId("login")).thenReturn(Optional.of(user));
        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> feedLikeService.toggleLike(feedId, userDetails))
                .isInstanceOf(FeedNotFoundException.class);
    }

    @Test
    @DisplayName("삭제된 피드 404")
    void toggleLike_deletedFeed() {
        Long feedId = 15L;
        when(userDetails.getUsername()).thenReturn("login");
        when(userRepository.findByLoginId("login")).thenReturn(Optional.of(user));
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        feed.softDelete();
        assertThatThrownBy(() -> feedLikeService.toggleLike(feedId, userDetails))
                .isInstanceOf(FeedNotFoundException.class);
    }
}
