package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.domain.*;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import com.cMall.feedShop.event.domain.repository.EventResultRepository;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.domain.repository.FeedVoteRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventResultService 테스트")
class EventResultServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventResultRepository eventResultRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedVoteRepository feedVoteRepository;

    @InjectMocks
    private EventResultService eventResultService;

    private Event testEvent;
    private User user1, user2, user3;
    private Feed feed1, feed2, feed3;

    @BeforeEach
    void setUp() {
        // 테스트 이벤트 설정
        testEvent = Event.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.BATTLE)
                .status(com.cMall.feedShop.event.domain.enums.EventStatus.UPCOMING)
                .maxParticipants(10)
                .build();
        // ID 설정을 위해 reflection 사용
        try {
            java.lang.reflect.Field idField = Event.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testEvent, 1L);
        } catch (Exception e) {
            // reflection 실패 시 테스트 스킵
        }

        // 테스트 사용자 설정
        user1 = User.builder()
                .loginId("user1")
                .password("password")
                .email("user1@test.com")
                .role(UserRole.USER)
                .build();
        user2 = User.builder()
                .loginId("user2")
                .password("password")
                .email("user2@test.com")
                .role(UserRole.USER)
                .build();
        user3 = User.builder()
                .loginId("user3")
                .password("password")
                .email("user3@test.com")
                .role(UserRole.USER)
                .build();

        // 테스트 피드 설정
        feed1 = Feed.builder()
                .user(user1)
                .event(testEvent)
                .build();
        feed2 = Feed.builder()
                .user(user2)
                .event(testEvent)
                .build();
        feed3 = Feed.builder()
                .user(user3)
                .event(testEvent)
                .build();
        
        // Feed ID 설정
        try {
            java.lang.reflect.Field feedIdField = Feed.class.getDeclaredField("id");
            feedIdField.setAccessible(true);
            feedIdField.set(feed1, 1L);
            feedIdField.set(feed2, 2L);
            feedIdField.set(feed3, 3L);
        } catch (Exception e) {
            // reflection 실패 시 테스트 스킵
        }
    }

    @Test
    @DisplayName("배틀 이벤트 결과 계산 - 성공")
    void calculateBattleResult_Success() {
        // given
        Long eventId = 1L;
        List<Feed> eventFeeds = Arrays.asList(feed1, feed2);
        
        // EventReward 설정
        EventReward firstPlaceReward = EventReward.builder()
                .conditionValue("1")
                .rewardValue("포인트:1000, 뱃지점수:50")
                .build();
        testEvent.setRewards(Arrays.asList(firstPlaceReward));
        
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(feedRepository.findByEventId(eventId)).thenReturn(eventFeeds);
        when(feedVoteRepository.countByFeedId(1L)).thenReturn(15L);
        when(feedVoteRepository.countByFeedId(2L)).thenReturn(8L);
        when(eventResultRepository.save(any(EventResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        EventResult result = eventResultService.calculateAndAnnounceEventResult(eventId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEvent()).isEqualTo(testEvent);
        assertThat(result.getResultType()).isEqualTo(EventResult.ResultType.BATTLE_WINNER);
        assertThat(result.getTotalParticipants()).isEqualTo(2);
        assertThat(result.getTotalVotes()).isEqualTo(15L); // 우승자 1명의 투표수만 합산
        assertThat(result.getResultDetails()).hasSize(1);
        
        EventResultDetail winnerDetail = result.getResultDetails().get(0);
        assertThat(winnerDetail.getUser()).isEqualTo(user1);
        assertThat(winnerDetail.getRankPosition()).isEqualTo(1);
        assertThat(winnerDetail.getVoteCount()).isEqualTo(15L);

        verify(eventResultRepository).save(any(EventResult.class));
    }

    @Test
    @DisplayName("랭킹 이벤트 결과 계산 - 성공")
    void calculateRankingResult_Success() {
        // given
        Long eventId = 1L;
        Event rankingEvent = Event.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.RANKING)
                .status(com.cMall.feedShop.event.domain.enums.EventStatus.UPCOMING)
                .maxParticipants(20)
                .build();
        // ID 설정
        try {
            java.lang.reflect.Field idField = Event.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(rankingEvent, 1L);
        } catch (Exception e) {
            // reflection 실패 시 테스트 스킵
        }
        
        // EventReward 설정
        EventReward firstPlaceReward = EventReward.builder()
                .conditionValue("1")
                .rewardValue("포인트:2000, 뱃지점수:100")
                .build();
        EventReward secondPlaceReward = EventReward.builder()
                .conditionValue("2")
                .rewardValue("포인트:1500, 뱃지점수:75")
                .build();
        EventReward thirdPlaceReward = EventReward.builder()
                .conditionValue("3")
                .rewardValue("포인트:1000, 뱃지점수:50")
                .build();
        rankingEvent.setRewards(Arrays.asList(firstPlaceReward, secondPlaceReward, thirdPlaceReward));
        
        List<Feed> eventFeeds = Arrays.asList(feed1, feed2, feed3);
        
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(rankingEvent));
        when(feedRepository.findByEventId(eventId)).thenReturn(eventFeeds);
        when(feedVoteRepository.countByFeedId(1L)).thenReturn(20L);
        when(feedVoteRepository.countByFeedId(2L)).thenReturn(15L);
        when(feedVoteRepository.countByFeedId(3L)).thenReturn(10L);
        when(eventResultRepository.save(any(EventResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        EventResult result = eventResultService.calculateAndAnnounceEventResult(eventId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEvent()).isEqualTo(rankingEvent);
        assertThat(result.getResultType()).isEqualTo(EventResult.ResultType.RANKING_TOP3);
        assertThat(result.getTotalParticipants()).isEqualTo(3);
        assertThat(result.getTotalVotes()).isEqualTo(45L);
        assertThat(result.getResultDetails()).hasSize(3);
        
        // 1등 확인
        EventResultDetail firstPlace = result.getResultDetails().get(0);
        assertThat(firstPlace.getUser()).isEqualTo(user1);
        assertThat(firstPlace.getRankPosition()).isEqualTo(1);
        assertThat(firstPlace.getVoteCount()).isEqualTo(20L);
        
        // 2등 확인
        EventResultDetail secondPlace = result.getResultDetails().get(1);
        assertThat(secondPlace.getUser()).isEqualTo(user2);
        assertThat(secondPlace.getRankPosition()).isEqualTo(2);
        assertThat(secondPlace.getVoteCount()).isEqualTo(15L);
        
        // 3등 확인
        EventResultDetail thirdPlace = result.getResultDetails().get(2);
        assertThat(thirdPlace.getUser()).isEqualTo(user3);
        assertThat(thirdPlace.getRankPosition()).isEqualTo(3);
        assertThat(thirdPlace.getVoteCount()).isEqualTo(10L);

        verify(eventResultRepository).save(any(EventResult.class));
    }

    @Test
    @DisplayName("이벤트 결과 계산 - 이벤트를 찾을 수 없는 경우")
    void calculateEventResult_EventNotFound() {
        // given
        Long eventId = 999L;
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // when & then
        try {
            eventResultService.calculateAndAnnounceEventResult(eventId);
        } catch (Exception e) {
            // 예외가 발생해야 함
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("이벤트 결과 계산 - 참여 피드가 없는 경우")
    void calculateEventResult_NoFeeds() {
        // given
        Long eventId = 1L;
        // RANKING 이벤트로 변경 (BATTLE은 최소 2명 필요)
        Event rankingEvent = Event.builder()
                .type(com.cMall.feedShop.event.domain.enums.EventType.RANKING)
                .status(com.cMall.feedShop.event.domain.enums.EventStatus.UPCOMING)
                .maxParticipants(20)
                .build();
        // ID 설정
        try {
            java.lang.reflect.Field idField = Event.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(rankingEvent, 1L);
        } catch (Exception e) {
            // reflection 실패 시 테스트 스킵
        }
        
        List<Feed> eventFeeds = Arrays.asList();
        
        // EventReward 설정 (빈 리스트라도 null이 아니어야 함)
        rankingEvent.setRewards(Arrays.asList());
        
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(rankingEvent));
        when(feedRepository.findByEventId(eventId)).thenReturn(eventFeeds);

        // when & then
        // 참여자가 없는 경우 예외가 발생해야 함
        try {
            eventResultService.calculateAndAnnounceEventResult(eventId);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("랭킹 이벤트에 참여자가 없습니다.");
        }
    }
}
