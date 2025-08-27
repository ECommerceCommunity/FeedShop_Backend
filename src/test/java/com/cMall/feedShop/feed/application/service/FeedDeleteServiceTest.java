package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.exception.FeedAccessDeniedException;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.enums.UserRole;
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
class FeedDeleteServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FeedImageService feedImageService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private FeedDeleteService feedDeleteService;

    private User owner;
    private User anotherUser;
    private Feed feed;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "owner_login", "password", "owner@test.com", UserRole.USER);
        anotherUser = new User(2L, "another_login", "password", "another@test.com", UserRole.USER);

        OrderItem orderItem = OrderItem.builder()
                .quantity(1)
                .totalPrice(java.math.BigDecimal.valueOf(10000))
                .finalPrice(java.math.BigDecimal.valueOf(9000))
                .build();

        feed = Feed.builder()
                .user(owner)
                .orderItem(orderItem)
                .title("제목")
                .content("내용")
                .instagramId("insta")
                .build();
    }

    @Test
    @DisplayName("본인 피드 삭제 성공")
    void deleteFeed_Success() {
        // given
        Long feedId = 10L;
        when(userDetails.getUsername()).thenReturn("owner_login");
        when(userRepository.findByLoginId("owner_login")).thenReturn(Optional.of(owner));
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));

        // when
        feedDeleteService.deleteFeed(feedId, userDetails);

        // then
        assertThat(feed.isDeleted()).isTrue();
        verify(feedRepository, times(1)).findById(feedId);
        verify(userRepository, times(1)).findByLoginId("owner_login");
    }

    @Test
    @DisplayName("비소유자 삭제 시 403 예외")
    void deleteFeed_Forbidden_NotOwner() {
        // given
        Long feedId = 11L;
        when(userDetails.getUsername()).thenReturn("another_login");
        when(userRepository.findByLoginId("another_login")).thenReturn(Optional.of(anotherUser));
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));

        // when & then
        assertThatThrownBy(() -> feedDeleteService.deleteFeed(feedId, userDetails))
                .isInstanceOf(FeedAccessDeniedException.class)
                .hasMessageContaining("본인의 피드만 삭제할 수 있습니다");
    }

    @Test
    @DisplayName("피드 미존재 시 404 예외")
    void deleteFeed_NotFound() {
        // given
        Long feedId = 12L;
        when(userDetails.getUsername()).thenReturn("owner_login");
        when(userRepository.findByLoginId("owner_login")).thenReturn(Optional.of(owner));
        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedDeleteService.deleteFeed(feedId, userDetails))
                .isInstanceOf(FeedNotFoundException.class);
    }

    @Test
    @DisplayName("이미 삭제된 피드 삭제 시 404 예외")
    void deleteFeed_AlreadyDeleted() {
        // given
        Long feedId = 13L;
        feed.softDelete();
        when(userDetails.getUsername()).thenReturn("owner_login");
        when(userRepository.findByLoginId("owner_login")).thenReturn(Optional.of(owner));
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));

        // when & then
        assertThatThrownBy(() -> feedDeleteService.deleteFeed(feedId, userDetails))
                .isInstanceOf(FeedNotFoundException.class);
    }

    @Test
    @DisplayName("미인증(credential 없음) 시 401 예외")
    void deleteFeed_Unauthorized_WhenNoUserDetails() {
        // given
        Long feedId = 14L;

        // when & then
        assertThatThrownBy(() -> feedDeleteService.deleteFeed(feedId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("인증 정보가 없습니다.")
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    @DisplayName("사용자 조회 실패 시 USER_NOT_FOUND 예외")
    void deleteFeed_UserNotFound() {
        // given
        Long feedId = 15L;
        when(userDetails.getUsername()).thenReturn("unknown_login");
        when(userRepository.findByLoginId("unknown_login")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedDeleteService.deleteFeed(feedId, userDetails))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("피드 삭제 시 이미지 파일도 함께 삭제")
    void deleteFeed_WithImages_Success() {
        // given
        Long feedId = 16L;
        
        // 피드에 이미지 추가
        feed.addImage("image1.jpg", 1);
        feed.addImage("image2.jpg", 2);

        when(userDetails.getUsername()).thenReturn("owner_login");
        when(userRepository.findByLoginId("owner_login")).thenReturn(Optional.of(owner));
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));

        // when
        feedDeleteService.deleteFeed(feedId, userDetails);

        // then
        assertThat(feed.isDeleted()).isTrue();
        verify(feedImageService, times(1)).deleteImages(eq(feed), any());
    }
}
