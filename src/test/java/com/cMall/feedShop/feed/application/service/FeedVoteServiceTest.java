package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.response.FeedVoteResponseDto;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.domain.repository.FeedVoteRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedVoteService 테스트")
class FeedVoteServiceTest {

    @Mock
    private FeedVoteRepository feedVoteRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Feed feed;

    @Mock
    private User user;

    @InjectMocks
    private FeedVoteService feedVoteService;

    @BeforeEach
    void setUp() {
        // 기본 Mock 설정은 각 테스트에서 필요한 것만 설정
    }

    @Test
    @DisplayName("피드 투표 성공")
    void voteFeed_success() {
        // given
        Long feedId = 1L;
        Long userId = 1L;

        when(feed.getFeedType()).thenReturn(FeedType.EVENT);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(feedVoteRepository.existsByFeedIdAndUserId(feedId, userId)).thenReturn(false);
        when(feedVoteRepository.countByFeedId(feedId)).thenReturn(1L);

        // when
        FeedVoteResponseDto result = feedVoteService.voteFeed(feedId, userId);

        // then
        assertThat(result.isVoted()).isTrue();
        assertThat(result.getVoteCount()).isEqualTo(1);
        assertThat(result.getMessage()).isEqualTo("투표가 완료되었습니다!");

        verify(feedVoteRepository).save(any());
        verify(feedVoteRepository).countByFeedId(feedId);
    }

    @Test
    @DisplayName("피드 투표 실패 - 이미 투표한 피드")
    void voteFeed_alreadyVoted() {
        // given
        Long feedId = 1L;
        Long userId = 1L;

        when(feed.getFeedType()).thenReturn(FeedType.EVENT);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(feedVoteRepository.existsByFeedIdAndUserId(feedId, userId)).thenReturn(true);
        when(feedVoteRepository.countByFeedId(feedId)).thenReturn(1L);

        // when
        FeedVoteResponseDto result = feedVoteService.voteFeed(feedId, userId);

        // then
        assertThat(result.isVoted()).isFalse();
        assertThat(result.getVoteCount()).isEqualTo(1);
        assertThat(result.getMessage()).isEqualTo("이미 투표한 피드입니다.");

        verify(feedVoteRepository, never()).save(any());
        verify(feedVoteRepository).countByFeedId(feedId);
    }

    @Test
    @DisplayName("피드 투표 실패 - 피드가 존재하지 않음")
    void voteFeed_feedNotFound() {
        // given
        Long feedId = 999L;
        Long userId = 1L;

        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedVoteService.voteFeed(feedId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEED_NOT_FOUND);

        verify(feedVoteRepository, never()).save(any());
    }

    @Test
    @DisplayName("피드 투표 실패 - 사용자가 존재하지 않음")
    void voteFeed_userNotFound() {
        // given
        Long feedId = 1L;
        Long userId = 999L;

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedVoteService.voteFeed(feedId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(feedVoteRepository, never()).save(any());
    }

    @Test
    @DisplayName("피드 투표 실패 - 이벤트 피드가 아님")
    void voteFeed_notEventFeed() {
        // given
        Long feedId = 1L;
        Long userId = 1L;

        when(feed.getFeedType()).thenReturn(FeedType.DAILY);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> feedVoteService.voteFeed(feedId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEED_ACCESS_DENIED);

        verify(feedVoteRepository, never()).save(any());
    }

    @Test
    @DisplayName("투표 개수 조회 성공")
    void getVoteCount_success() {
        // given
        Long feedId = 1L;
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feedVoteRepository.countByFeedId(feedId)).thenReturn(5L);

        // when
        int result = feedVoteService.getVoteCount(feedId);

        // then
        assertThat(result).isEqualTo(5);
        verify(feedVoteRepository).countByFeedId(feedId);
    }

    @Test
    @DisplayName("투표 개수 조회 실패 - 피드가 존재하지 않음")
    void getVoteCount_feedNotFound() {
        // given
        Long feedId = 999L;
        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // when
        int result = feedVoteService.getVoteCount(feedId);

        // then - 예외 처리 로직으로 인해 0을 반환
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("투표 여부 확인 성공 - 투표함")
    void hasVoted_true() {
        // given
        Long feedId = 1L;
        Long userId = 1L;

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(feedVoteRepository.existsByFeedIdAndUserId(feedId, userId)).thenReturn(true);

        // when
        boolean result = feedVoteService.hasVoted(feedId, userId);

        // then
        assertThat(result).isTrue();
        verify(feedVoteRepository).existsByFeedIdAndUserId(feedId, userId);
    }

    @Test
    @DisplayName("투표 여부 확인 성공 - 투표하지 않음")
    void hasVoted_false() {
        // given
        Long feedId = 1L;
        Long userId = 1L;

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(feedVoteRepository.existsByFeedIdAndUserId(feedId, userId)).thenReturn(false);

        // when
        boolean result = feedVoteService.hasVoted(feedId, userId);

        // then
        assertThat(result).isFalse();
        verify(feedVoteRepository).existsByFeedIdAndUserId(feedId, userId);
    }
}
