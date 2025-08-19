package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.response.FeedVoteResponseDto;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.event.domain.Event;
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

import java.util.List;
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

    @Mock
    private Event event;

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
        Long eventId = 1L;

        when(feed.getFeedType()).thenReturn(FeedType.EVENT);
        when(feed.getEvent()).thenReturn(event);
        when(event.getId()).thenReturn(eventId);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(feedVoteRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
        when(feed.getParticipantVoteCount()).thenReturn(0, 1); // 초기값 0, 증가 후 1

        // when
        FeedVoteResponseDto result = feedVoteService.voteFeed(feedId, userId);

        // then
        assertThat(result.isVoted()).isTrue();
        assertThat(result.getVoteCount()).isEqualTo(1);
        assertThat(result.getMessage()).isEqualTo("투표가 완료되었습니다!");

        verify(feedVoteRepository).save(any());
        verify(feed).incrementVoteCount();
    }

    @Test
    @DisplayName("피드 투표 실패 - 이미 해당 이벤트에 투표함")
    void voteFeed_alreadyVoted() {
        // given
        Long feedId = 1L;
        Long userId = 1L;
        Long eventId = 1L;

        when(feed.getFeedType()).thenReturn(FeedType.EVENT);
        when(feed.getEvent()).thenReturn(event);
        when(event.getId()).thenReturn(eventId);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(feedVoteRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);
        when(feed.getParticipantVoteCount()).thenReturn(1); // 이미 투표된 상태

        // when
        FeedVoteResponseDto result = feedVoteService.voteFeed(feedId, userId);

        // then
        assertThat(result.isVoted()).isFalse();
        assertThat(result.getVoteCount()).isEqualTo(1);
        assertThat(result.getMessage()).isEqualTo("이미 해당 이벤트에 투표했습니다.");

        verify(feedVoteRepository, never()).save(any());
        verify(feed, never()).incrementVoteCount();
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
        when(feed.getParticipantVoteCount()).thenReturn(5); // Feed 엔티티의 투표 수

        // when
        int result = feedVoteService.getVoteCount(feedId);

        // then
        assertThat(result).isEqualTo(5);
    }

    @Test
    @DisplayName("투표 개수 조회 실패 - 피드가 존재하지 않음")
    void getVoteCount_feedNotFound() {
        // given
        Long feedId = 999L;
        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedVoteService.getVoteCount(feedId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("투표 여부 확인 성공 - 투표함")
    void hasVoted_true() {
        // given
        Long feedId = 1L;
        Long userId = 1L;
        Long eventId = 1L;

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(feed.getEvent()).thenReturn(event);
        when(event.getId()).thenReturn(eventId);
        when(feedVoteRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);

        // when
        boolean result = feedVoteService.hasVoted(feedId, userId);

        // then
        assertThat(result).isTrue();
        verify(feedVoteRepository).existsByEventIdAndUserId(eventId, userId);
    }

    @Test
    @DisplayName("투표 여부 확인 성공 - 투표하지 않음")
    void hasVoted_false() {
        // given
        Long feedId = 1L;
        Long userId = 1L;
        Long eventId = 1L;

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(feed.getEvent()).thenReturn(event);
        when(event.getId()).thenReturn(eventId);
        when(feedVoteRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);

        // when
        boolean result = feedVoteService.hasVoted(feedId, userId);

        // then
        assertThat(result).isFalse();
        verify(feedVoteRepository).existsByEventIdAndUserId(eventId, userId);
    }

    @Test
    @DisplayName("🔧 개선: 특정 피드 투표 수 동기화 성공")
    void syncVoteCount_success() {
        // given
        Long feedId = 1L;
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feed.getParticipantVoteCount()).thenReturn(3); // 현재 Feed 엔티티 값
        when(feedVoteRepository.countByFeedId(feedId)).thenReturn(5L); // 실제 투표 수

        // when
        feedVoteService.syncVoteCount(feedId);

        // then
        verify(feed, times(2)).incrementVoteCount(); // 3 -> 5 (2번 증가)
    }

    @Test
    @DisplayName("🔧 개선: 특정 피드 투표 수 동기화 - 감소 케이스")
    void syncVoteCount_decrease() {
        // given
        Long feedId = 1L;
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feed.getParticipantVoteCount()).thenReturn(5); // 현재 Feed 엔티티 값
        when(feedVoteRepository.countByFeedId(feedId)).thenReturn(3L); // 실제 투표 수

        // when
        feedVoteService.syncVoteCount(feedId);

        // then
        verify(feed, times(2)).decrementVoteCount(); // 5 -> 3 (2번 감소)
    }

    @Test
    @DisplayName("🔧 개선: 특정 피드 투표 수 동기화 - 동일한 경우")
    void syncVoteCount_noChange() {
        // given
        Long feedId = 1L;
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feed.getParticipantVoteCount()).thenReturn(3); // 현재 Feed 엔티티 값
        when(feedVoteRepository.countByFeedId(feedId)).thenReturn(3L); // 실제 투표 수

        // when
        feedVoteService.syncVoteCount(feedId);

        // then
        verify(feed, never()).incrementVoteCount();
        verify(feed, never()).decrementVoteCount();
    }

    @Test
    @DisplayName("🔧 개선: 전체 피드 투표 수 동기화 성공")
    void syncAllVoteCounts_success() {
        // given
        Object[] voteCount1 = {1L, 5L}; // feedId: 1, voteCount: 5
        Object[] voteCount2 = {2L, 3L}; // feedId: 2, voteCount: 3
        List<Object[]> voteCounts = List.of(voteCount1, voteCount2);

        Feed feed1 = mock(Feed.class);
        Feed feed2 = mock(Feed.class);

        when(feedVoteRepository.getAllFeedVoteCounts()).thenReturn(voteCounts);
        when(feedRepository.findById(1L)).thenReturn(Optional.of(feed1));
        when(feedRepository.findById(2L)).thenReturn(Optional.of(feed2));
        when(feed1.getParticipantVoteCount()).thenReturn(3); // 동기화 필요
        when(feed2.getParticipantVoteCount()).thenReturn(3); // 동일함

        // when
        feedVoteService.syncAllVoteCounts();

        // then
        verify(feed1, times(2)).incrementVoteCount(); // 3 -> 5
        verify(feed2, never()).incrementVoteCount(); // 변경 없음
        verify(feed2, never()).decrementVoteCount(); // 변경 없음
    }
}
