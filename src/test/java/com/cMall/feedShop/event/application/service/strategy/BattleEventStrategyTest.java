package com.cMall.feedShop.event.application.service.strategy;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventReward;
import com.cMall.feedShop.event.domain.EventResult;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.feed.domain.repository.FeedVoteRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BattleEventStrategyTest {

    @Mock
    private FeedVoteRepository feedVoteRepository;

    @InjectMocks
    private BattleEventStrategy battleEventStrategy;

    private Event testEvent;
    private User user1, user2;
    private Feed feed1, feed2;

    @BeforeEach
    void setUp() {
        // 테스트 이벤트 설정
        testEvent = Event.builder()
                .type(EventType.BATTLE)
                .status(EventStatus.UPCOMING)
                .maxParticipants(20)
                .build();

        // Event ID 설정 (reflection 사용)
        try {
            java.lang.reflect.Field idField = Event.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testEvent, 1L);
        } catch (Exception e) {
            // reflection 실패 시 테스트 스킵
        }

        // EventReward 설정
        EventReward firstPlaceReward = EventReward.builder()
                .conditionValue("1")
                .rewardValue("포인트:1000, 뱃지점수:50")
                .build();
        testEvent.setRewards(Arrays.asList(firstPlaceReward));

        // 테스트 사용자 설정
        user1 = User.builder().build();
        user2 = User.builder().build();

        // 테스트 피드 설정
        feed1 = Feed.builder()
                .user(user1)
                .title("테스트 피드 1")
                .event(testEvent)
                .build();
        feed2 = Feed.builder()
                .user(user2)
                .title("테스트 피드 2")
                .event(testEvent)
                .build();

        // Feed ID와 생성 시간 설정 (reflection 사용)
        try {
            java.lang.reflect.Field feedIdField = Feed.class.getDeclaredField("id");
            feedIdField.setAccessible(true);
            feedIdField.set(feed1, 1L);
            feedIdField.set(feed2, 2L);

            java.lang.reflect.Field createdAtField = Feed.class.getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(feed1, LocalDateTime.now().minusHours(1)); // feed1이 먼저 생성
            createdAtField.set(feed2, LocalDateTime.now()); // feed2가 나중에 생성
        } catch (Exception e) {
            // reflection 실패 시 테스트 스킵
        }
    }

    @Test
    @DisplayName("배틀 이벤트 타입 반환")
    void getEventType_ReturnsBattle() {
        // when
        EventType eventType = battleEventStrategy.getEventType();

        // then
        assertThat(eventType).isEqualTo(EventType.BATTLE);
    }

    @Test
    @DisplayName("배틀 이벤트 결과 계산 - 성공")
    void calculateResult_Success() {
        // given
        List<Feed> participants = Arrays.asList(feed1, feed2);
        lenient().when(feedVoteRepository.countByFeed_Id(1L)).thenReturn(15L);
        lenient().when(feedVoteRepository.countByFeed_Id(2L)).thenReturn(8L);

        // when
        EventResult result = battleEventStrategy.calculateResult(testEvent, participants);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEvent()).isEqualTo(testEvent);
        assertThat(result.getResultType()).isEqualTo(EventResult.ResultType.BATTLE_WINNER);
        assertThat(result.getTotalParticipants()).isEqualTo(2);
        assertThat(result.getTotalVotes()).isEqualTo(15L); // 우승자의 투표 수
        assertThat(result.getResultDetails()).hasSize(1);

        // 우승자 상세 정보 확인
        var winnerDetail = result.getResultDetails().get(0);
        assertThat(winnerDetail.getUser()).isEqualTo(user1);
        assertThat(winnerDetail.getFeedTitle()).isEqualTo("테스트 피드 1");
        assertThat(winnerDetail.getRankPosition()).isEqualTo(1);
        assertThat(winnerDetail.getVoteCount()).isEqualTo(15L);
        assertThat(winnerDetail.getPointsEarned()).isEqualTo(1000);
        assertThat(winnerDetail.getBadgePointsEarned()).isEqualTo(50);
    }

    @Test
    @DisplayName("배틀 이벤트 결과 계산 - 동점인 경우")
    void calculateResult_Tie() {
        // given
        List<Feed> participants = Arrays.asList(feed1, feed2);
        lenient().when(feedVoteRepository.countByFeed_Id(1L)).thenReturn(10L);
        lenient().when(feedVoteRepository.countByFeed_Id(2L)).thenReturn(10L);

        // when
        EventResult result = battleEventStrategy.calculateResult(testEvent, participants);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getResultDetails()).hasSize(1);
        
        // 동점인 경우 먼저 생성된 피드가 우승 (feed1이 먼저 생성됨)
        var winnerDetail = result.getResultDetails().get(0);
        assertThat(winnerDetail.getUser()).isEqualTo(user1);
        assertThat(winnerDetail.getVoteCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("배틀 이벤트 결과 계산 - 참여자가 없는 경우")
    void calculateResult_NoParticipants() {
        // given
        List<Feed> participants = Arrays.asList();

        // when
        EventResult result = battleEventStrategy.calculateResult(testEvent, participants);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEvent()).isEqualTo(testEvent);
        assertThat(result.getResultType()).isEqualTo(EventResult.ResultType.BATTLE_WINNER);
        assertThat(result.getTotalParticipants()).isEqualTo(0);
        assertThat(result.getTotalVotes()).isEqualTo(0L);
        assertThat(result.getResultDetails()).isEmpty();
    }

    @Test
    @DisplayName("배틀 이벤트 결과 계산 - 참여자가 1명인 경우")
    void calculateResult_SingleParticipant() {
        // given
        List<Feed> participants = Arrays.asList(feed1);
        lenient().when(feedVoteRepository.countByFeed_Id(1L)).thenReturn(5L);

        // when
        EventResult result = battleEventStrategy.calculateResult(testEvent, participants);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEvent()).isEqualTo(testEvent);
        assertThat(result.getResultType()).isEqualTo(EventResult.ResultType.BATTLE_WINNER);
        assertThat(result.getTotalParticipants()).isEqualTo(1);
        assertThat(result.getTotalVotes()).isEqualTo(5L);
        assertThat(result.getResultDetails()).hasSize(1);

        // 단독 우승자 상세 정보 확인
        var winnerDetail = result.getResultDetails().get(0);
        assertThat(winnerDetail.getUser()).isEqualTo(user1);
        assertThat(winnerDetail.getFeedTitle()).isEqualTo("테스트 피드 1");
        assertThat(winnerDetail.getRankPosition()).isEqualTo(1);
        assertThat(winnerDetail.getVoteCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("배틀 이벤트 참여 가능 여부 확인")
    void canParticipate_ReturnsTrue() {
        // when
        boolean canParticipate = battleEventStrategy.canParticipate(testEvent, user1);

        // then
        assertThat(canParticipate).isTrue();
    }

    @Test
    @DisplayName("배틀 이벤트 참여 검증")
    void validateParticipation_Success() {
        // when & then (예외가 발생하지 않아야 함)
        battleEventStrategy.validateParticipation(testEvent, feed1);
    }

    @Test
    @DisplayName("배틀 이벤트 참여자 정보 생성")
    void createParticipant_Success() {
        // when
        EventStrategy.EventParticipantInfo participantInfo = 
                battleEventStrategy.createParticipant(testEvent, user1, feed1);

        // then
        assertThat(participantInfo).isNotNull();
        assertThat(participantInfo.getStatus()).isEqualTo("PARTICIPATING");
        assertThat(participantInfo.getMetadata()).contains("matchGroup");
    }
}
