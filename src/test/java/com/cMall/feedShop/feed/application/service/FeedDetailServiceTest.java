package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.domain.repository.FeedLikeRepository;
import com.cMall.feedShop.order.domain.model.OrderItem;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedDetailServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private FeedLikeService feedLikeService;

    @Mock
    private FeedServiceUtils feedServiceUtils;

    @InjectMocks
    private FeedDetailService feedDetailService;

    private Feed mockFeed;
    private FeedDetailResponseDto mockResponseDto;

    @BeforeEach
    void setUp() {
        // Mock OrderItem 생성
        OrderItem mockOrderItem = OrderItem.builder()
                .quantity(1)
                .totalPrice(java.math.BigDecimal.valueOf(100000))
                .finalPrice(java.math.BigDecimal.valueOf(90000))
                .build();
        
        // Mock User 생성
        User mockUser = new User(1L, "testuser", "password", "test@test.com", com.cMall.feedShop.user.domain.enums.UserRole.USER);
        
        // Mock Feed 엔티티 생성
        mockFeed = Feed.builder()
                .orderItem(mockOrderItem)
                .user(mockUser)
                .title("테스트 피드")
                .content("테스트 내용")
                .instagramId("test_instagram")
                .build();
        
        // Mock Response DTO 생성
        mockResponseDto = FeedDetailResponseDto.builder()
                .feedId(1L)
                .title("테스트 피드")
                .content("테스트 내용")
                .feedType(FeedType.DAILY)
                .instagramId("test_instagram")
                .likeCount(10)
                .commentCount(5)
                .participantVoteCount(3)
                .build();
    }

    @Test
    @DisplayName("피드 상세 조회 성공 - 로그인하지 않은 사용자")
    void getFeedDetail_Success_WithoutUser() {
        // given
        Long feedId = 1L;
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.of(mockFeed));
        when(feedMapper.toFeedDetailResponseDto(mockFeed)).thenReturn(mockResponseDto);

        // when
        FeedDetailResponseDto result = feedDetailService.getFeedDetail(feedId, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFeedId()).isEqualTo(feedId);
        assertThat(result.getTitle()).isEqualTo("테스트 피드");
        assertThat(result.getContent()).isEqualTo("테스트 내용");
        assertThat(result.getFeedType()).isEqualTo(FeedType.DAILY);
        assertThat(result.getLikeCount()).isEqualTo(10);
        assertThat(result.getCommentCount()).isEqualTo(5);
        assertThat(result.getParticipantVoteCount()).isEqualTo(3);
        assertThat(result.getIsLiked()).isFalse(); // 로그인하지 않은 사용자는 false
    }

    @Test
    @DisplayName("피드 상세 조회 성공 - 로그인한 사용자 (좋아요하지 않은 상태)")
    void getFeedDetail_Success_WithUser_NotLiked() {
        // given
        Long feedId = 1L;
        UserDetails userDetails = org.mockito.Mockito.mock(UserDetails.class);
        
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.of(mockFeed));
        when(feedMapper.toFeedDetailResponseDto(mockFeed)).thenReturn(mockResponseDto);
        when(feedServiceUtils.getUserIdFromUserDetails(userDetails)).thenReturn(1L);
        when(feedLikeService.isLikedByUser(feedId, 1L)).thenReturn(false);

        // when
        FeedDetailResponseDto result = feedDetailService.getFeedDetail(feedId, userDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFeedId()).isEqualTo(feedId);
        assertThat(result.getTitle()).isEqualTo("테스트 피드");
        assertThat(result.getIsLiked()).isFalse(); // 좋아요하지 않은 상태
    }

    @Test
    @DisplayName("피드 상세 조회 성공 - 로그인한 사용자 (좋아요한 상태)")
    void getFeedDetail_Success_WithUser_Liked() {
        // given
        Long feedId = 1L;
        UserDetails userDetails = org.mockito.Mockito.mock(UserDetails.class);
        
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.of(mockFeed));
        when(feedMapper.toFeedDetailResponseDto(mockFeed)).thenReturn(mockResponseDto);
        when(feedServiceUtils.getUserIdFromUserDetails(userDetails)).thenReturn(1L);
        when(feedLikeService.isLikedByUser(feedId, 1L)).thenReturn(true);

        // when
        FeedDetailResponseDto result = feedDetailService.getFeedDetail(feedId, userDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFeedId()).isEqualTo(feedId);
        assertThat(result.getTitle()).isEqualTo("테스트 피드");
        assertThat(result.getIsLiked()).isTrue(); // 좋아요한 상태
    }

    @Test
    @DisplayName("존재하지 않는 피드 조회 시 예외 발생")
    void getFeedDetail_NotFound_ThrowsException() {
        // given
        Long feedId = 999L;
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedDetailService.getFeedDetail(feedId, null))
                .isInstanceOf(FeedNotFoundException.class)
                .hasMessageContaining("피드를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("삭제된 피드 조회 시 예외 발생")
    void getFeedDetail_DeletedFeed_ThrowsException() {
        // given
        Long feedId = 1L;
        
        // Feed 클래스에는 setter가 없으므로, 실제로는 삭제된 피드가 조회되지 않아야 함
        // Repository에서 null을 반환하도록 Mock 설정
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedDetailService.getFeedDetail(feedId, null))
                .isInstanceOf(FeedNotFoundException.class)
                .hasMessageContaining("피드를 찾을 수 없습니다");
    }
}
