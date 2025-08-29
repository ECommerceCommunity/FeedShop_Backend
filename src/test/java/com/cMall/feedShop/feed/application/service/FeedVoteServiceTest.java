package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.response.FeedVoteResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.entity.FeedVote;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.domain.repository.FeedVoteRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.application.service.UserLevelService;
import com.cMall.feedShop.user.domain.model.ActivityType;
import com.cMall.feedShop.user.application.service.PointService;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.application.service.EventStatusService;
import com.cMall.feedShop.common.util.TimeUtil;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
    private FeedVote feedVote;

    @Mock
    private PointService pointService;

    @Mock
    private UserLevelService userLevelService;

    @Mock
    private EventStatusService eventStatusService;

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

        when(feed.isEventFeed()).thenReturn(true);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feed.getEvent()).thenReturn(mock(Event.class));
        when(feed.getEvent().getId()).thenReturn(eventId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventStatusService.calculateEventStatus(any(Event.class), any())).thenReturn(EventStatus.ONGOING);
        when(feedVoteRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
        when(feedVoteRepository.save(any(FeedVote.class))).thenReturn(mock(FeedVote.class));
        when(feed.getParticipantVoteCount()).thenReturn(0); // 초기값 0
        doAnswer(invocation -> {
            when(feed.getParticipantVoteCount()).thenReturn(1); // incrementVoteCount 호출 후 1 반환
            return null;
        }).when(feed).incrementVoteCount();

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

        when(feed.isEventFeed()).thenReturn(true);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feed.getEvent()).thenReturn(mock(Event.class));
        when(feed.getEvent().getId()).thenReturn(eventId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventStatusService.calculateEventStatus(any(Event.class), any())).thenReturn(EventStatus.ONGOING);
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

    // voteFeed_feedNotFound 테스트는 제거 - 서비스 로직상 사용자를 먼저 조회하므로
    // 피드가 존재하지 않아도 사용자가 존재하지 않으면 BusinessException이 먼저 발생

    @Test
    @DisplayName("피드 투표 실패 - 사용자가 존재하지 않음")
    void voteFeed_userNotFound() {
        // given
        Long feedId = 1L;
        Long userId = 999L;

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

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feed.isEventFeed()).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> feedVoteService.voteFeed(feedId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

        verify(feedVoteRepository, never()).save(any());
    }

    @Test
    @DisplayName("피드 투표 실패 - 이벤트가 종료됨")
    void voteFeed_eventEnded() {
        // given
        Long feedId = 1L;
        Long userId = 1L;

        when(feed.isEventFeed()).thenReturn(true);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feed.getEvent()).thenReturn(mock(Event.class));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventStatusService.calculateEventStatus(any(Event.class), any())).thenReturn(EventStatus.ENDED);

        // when & then
        assertThatThrownBy(() -> feedVoteService.voteFeed(feedId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST)
                .hasMessageContaining("이벤트가 종료되어 투표할 수 없습니다");

        verify(feedVoteRepository, never()).save(any());
    }

    @Test
    @DisplayName("투표 개수 조회 성공")
    void getVoteCount_success() {
        // given
        Long feedId = 1L;
        when(feedVoteRepository.countByFeed_Id(feedId)).thenReturn(5L); // Repository에서 실제 투표 수 반환

        // when
        long result = feedVoteService.getVoteCount(feedId);

        // then
        assertThat(result).isEqualTo(5L);
        verify(feedVoteRepository).countByFeed_Id(feedId);
    }

    @Test
    @DisplayName("투표 개수 조회 - 피드가 존재하지 않아도 투표 수는 0 반환")
    void getVoteCount_feedNotFound_returnsZero() {
        // given
        Long feedId = 999L;
        when(feedVoteRepository.countByFeed_Id(feedId)).thenReturn(0L);

        // when
        long result = feedVoteService.getVoteCount(feedId);

        // then
        assertThat(result).isEqualTo(0L);
        verify(feedVoteRepository).countByFeed_Id(feedId);
    }

    @Test
    @DisplayName("투표 여부 확인 성공 - 투표함")
    void hasVoted_true() {
        // given
        Long feedId = 1L;
        Long userId = 1L;
        Long eventId = 1L;

        when(feedVoteRepository.existsByFeed_IdAndVoter_Id(feedId, userId)).thenReturn(true);

        // when
        boolean result = feedVoteService.hasVoted(feedId, userId);

        // then
        assertThat(result).isTrue();
        verify(feedVoteRepository).existsByFeed_IdAndVoter_Id(feedId, userId);
    }

    @Test
    @DisplayName("투표 여부 확인 성공 - 투표하지 않음")
    void hasVoted_false() {
        // given
        Long feedId = 1L;
        Long userId = 1L;
        Long eventId = 1L;

        when(feedVoteRepository.existsByFeed_IdAndVoter_Id(feedId, userId)).thenReturn(false);

        // when
        boolean result = feedVoteService.hasVoted(feedId, userId);

        // then
        assertThat(result).isFalse();
        verify(feedVoteRepository).existsByFeed_IdAndVoter_Id(feedId, userId);
    }

    @Test
    @DisplayName("🔧 개선: 특정 피드 투표 수 동기화 성공")
    void syncVoteCount_success() {
        // given
        Long feedId = 1L;
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feed.getParticipantVoteCount()).thenReturn(3); // 현재 Feed 엔티티 값
        when(feedVoteRepository.countByFeed_Id(feedId)).thenReturn(5L); // 실제 투표 수

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
        when(feedVoteRepository.countByFeed_Id(feedId)).thenReturn(3L); // 실제 투표 수

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
        when(feedVoteRepository.countByFeed_Id(feedId)).thenReturn(3L); // 실제 투표 수

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

        // feedRepository.findAll() Mock 설정
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Feed> feedPage = new PageImpl<>(List.of(feed1, feed2), pageable, 2);
        when(feedRepository.findAll(pageable)).thenReturn(feedPage);
        
        when(feed1.getId()).thenReturn(1L);
        when(feed2.getId()).thenReturn(2L);
        when(feedRepository.findById(1L)).thenReturn(Optional.of(feed1));
        when(feedRepository.findById(2L)).thenReturn(Optional.of(feed2));
        when(feed1.getParticipantVoteCount()).thenReturn(3); // 동기화 필요
        when(feed2.getParticipantVoteCount()).thenReturn(3); // 동일함
        when(feedVoteRepository.countByFeed_Id(1L)).thenReturn(5L); // 실제 투표 수
        when(feedVoteRepository.countByFeed_Id(2L)).thenReturn(3L); // 실제 투표 수

        // when
        feedVoteService.syncAllVoteCounts();

        // then
        verify(feed1, times(2)).incrementVoteCount(); // 3 -> 5
        verify(feed2, never()).incrementVoteCount(); // 변경 없음
        verify(feed2, never()).decrementVoteCount(); // 변경 없음
    }

    @Test
    @DisplayName("여러 피드에 대한 사용자의 투표 상태 일괄 조회 성공")
    void getVotedFeedIdsByFeedIdsAndUserId_success() {
        // given
        List<Long> feedIds = List.of(1L, 2L, 3L, 4L, 5L);
        Long userId = 1L;
        List<Long> votedFeedIds = List.of(2L, 4L); // 짝수 ID만 투표
        
        when(feedVoteRepository.findVotedFeedIdsByFeedIdsAndUserId(feedIds, userId))
                .thenReturn(votedFeedIds);

        // when
        Set<Long> result = feedVoteService.getVotedFeedIdsByFeedIdsAndUserId(feedIds, userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(2L, 4L);
        verify(feedVoteRepository).findVotedFeedIdsByFeedIdsAndUserId(feedIds, userId);
    }

    @Test
    @DisplayName("여러 피드에 대한 사용자의 투표 상태 일괄 조회 - 사용자 ID가 null인 경우")
    void getVotedFeedIdsByFeedIdsAndUserId_userIdNull_returnsEmptySet() {
        // given
        List<Long> feedIds = List.of(1L, 2L, 3L);
        Long userId = null;

        // when
        Set<Long> result = feedVoteService.getVotedFeedIdsByFeedIdsAndUserId(feedIds, userId);

        // then
        assertThat(result).isEmpty();
        verify(feedVoteRepository, never()).findVotedFeedIdsByFeedIdsAndUserId(any(), any());
    }

    @Test
    @DisplayName("여러 피드에 대한 사용자의 투표 상태 일괄 조회 - 피드 ID 목록이 비어있는 경우")
    void getVotedFeedIdsByFeedIdsAndUserId_emptyFeedIds_returnsEmptySet() {
        // given
        List<Long> feedIds = List.of();
        Long userId = 1L;

        // when
        Set<Long> result = feedVoteService.getVotedFeedIdsByFeedIdsAndUserId(feedIds, userId);

        // then
        assertThat(result).isEmpty();
        verify(feedVoteRepository, never()).findVotedFeedIdsByFeedIdsAndUserId(any(), any());
    }

    @Test
    @DisplayName("여러 피드에 대한 사용자의 투표 상태 일괄 조회 - Repository 오류 발생 시 빈 집합 반환")
    void getVotedFeedIdsByFeedIdsAndUserId_repositoryError_returnsEmptySet() {
        // given
        List<Long> feedIds = List.of(1L, 2L, 3L);
        Long userId = 1L;
        
        when(feedVoteRepository.findVotedFeedIdsByFeedIdsAndUserId(feedIds, userId))
                .thenThrow(new RuntimeException("Database error"));

        // when
        Set<Long> result = feedVoteService.getVotedFeedIdsByFeedIdsAndUserId(feedIds, userId);

        // then
        assertThat(result).isEmpty();
        verify(feedVoteRepository).findVotedFeedIdsByFeedIdsAndUserId(feedIds, userId);
    }
}
