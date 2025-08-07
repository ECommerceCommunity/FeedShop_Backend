package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.user.domain.model.User;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedDetailServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedMapper feedMapper;

    @InjectMocks
    private FeedDetailService feedDetailService;

    private Feed mockFeed;
    private FeedDetailResponseDto mockResponseDto;

    @BeforeEach
    void setUp() {
        // Mock Feed 엔티티 생성
        mockFeed = Feed.builder()
                .orderItem(OrderItem.builder().orderItemId(1L).build())
                .user(User.builder().id(1L).build())
                .title("테스트 피드")
                .content("테스트 내용")
                .instagramId("test_instagram")
                .build();
        mockFeed.setId(1L);
        mockFeed.setFeedType(FeedType.DAILY);
        mockFeed.setLikeCount(10);
        mockFeed.setCommentCount(5);
        mockFeed.setParticipantVoteCount(3);
        mockFeed.setCreatedAt(LocalDateTime.now());
        mockFeed.setUpdatedAt(LocalDateTime.now());

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
    @DisplayName("피드 상세 조회 성공")
    void getFeedDetail_Success() {
        // given
        Long feedId = 1L;
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.of(mockFeed));
        when(feedMapper.toDetailResponseDto(mockFeed)).thenReturn(mockResponseDto);

        // when
        FeedDetailResponseDto result = feedDetailService.getFeedDetail(feedId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFeedId()).isEqualTo(feedId);
        assertThat(result.getTitle()).isEqualTo("테스트 피드");
        assertThat(result.getContent()).isEqualTo("테스트 내용");
        assertThat(result.getFeedType()).isEqualTo(FeedType.DAILY);
        assertThat(result.getLikeCount()).isEqualTo(10);
        assertThat(result.getCommentCount()).isEqualTo(5);
        assertThat(result.getParticipantVoteCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("존재하지 않는 피드 조회 시 예외 발생")
    void getFeedDetail_NotFound_ThrowsException() {
        // given
        Long feedId = 999L;
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedDetailService.getFeedDetail(feedId))
                .isInstanceOf(FeedNotFoundException.class)
                .hasMessageContaining("피드를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("삭제된 피드 조회 시 예외 발생")
    void getFeedDetail_DeletedFeed_ThrowsException() {
        // given
        Long feedId = 1L;
        mockFeed.setDeletedAt(LocalDateTime.now()); // 삭제된 피드로 설정
        when(feedRepository.findDetailById(feedId)).thenReturn(Optional.of(mockFeed));

        // when & then
        assertThatThrownBy(() -> feedDetailService.getFeedDetail(feedId))
                .isInstanceOf(FeedNotFoundException.class)
                .hasMessageContaining("삭제된 피드입니다");
    }
}
