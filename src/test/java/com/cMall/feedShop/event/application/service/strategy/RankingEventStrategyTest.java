package com.cMall.feedShop.event.application.service.strategy;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventReward;
import com.cMall.feedShop.event.domain.EventResult;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.feed.domain.repository.FeedVoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingEventStrategyTest {

    @Mock
    private FeedVoteRepository feedVoteRepository;

    @InjectMocks
    private RankingEventStrategy rankingEventStrategy;

    private Event testEvent;
    private User user1, user2, user3;
    private Feed feed1, feed2, feed3;

    @BeforeEach
    void setUp() {
        // 테스트 이벤트 설정
        testEvent = Event.builder()
                .type(EventType.RANKING)
                .status(EventStatus.UPCOMING)
                .maxParticipants(20)
                .build();

        // EventReward 설정 (1등, 2등, 3등)
        EventReward firstPlaceReward = EventReward.builder()
                .conditionValue("1")
                .rewardValue("포인트:1000, 뱃지점수:50")
                .build();
        EventReward secondPlaceReward = EventReward.builder()
                .conditionValue("2")
                .rewardValue("포인트:500, 뱃지점수:30")
                .build();
        EventReward thirdPlaceReward = EventReward.builder()
                .conditionValue("3")
                .rewardValue("포인트:300, 뱃지점수:20")
                .build();
        testEvent.setRewards(Arrays.asList(firstPlaceReward, secondPlaceReward, thirdPlaceReward));

        // 테스트 사용자 설정
        user1 = User.builder().build();
        user2 = User.builder().build();
        user3 = User.builder().build();

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
        feed3 = Feed.builder()
                .user(user3)
                .title("테스트 피드 3")
                .event(testEvent)
                .build();
    }

    @Test
    @DisplayName("랭킹 이벤트 타입 반환")
    void getEventType_ReturnsRanking() {
        // when
        EventType eventType = rankingEventStrategy.getEventType();

        // then
        assertThat(eventType).isEqualTo(EventType.RANKING);
    }

    @Test
    @DisplayName("랭킹 이벤트 결과 계산 - TOP 3 성공")
    void calculateResult_Top3Success() {
        // given
        List<Feed> participants = Arrays.asList(feed1, feed2, feed3);
        when(feedVoteRepository.countByFeedId(any())).thenReturn(20L, 15L, 10L);

        // when
        EventResult result = rankingEventStrategy.calculateResult(testEvent, participants);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEvent()).isEqualTo(testEvent);
        assertThat(result.getResultType()).isEqualTo(EventResult.ResultType.RANKING_TOP3);
        assertThat(result.getTotalParticipants()).isEqualTo(3);
        assertThat(result.getTotalVotes()).isEqualTo(45L); // 20 + 15 + 10
        assertThat(result.getResultDetails()).hasSize(3);

        // 1등 확인
        var firstPlace = result.getResultDetails().get(0);
        assertThat(firstPlace.getUser()).isEqualTo(user1);
        assertThat(firstPlace.getFeedTitle()).isEqualTo("테스트 피드 1");
        assertThat(firstPlace.getRankPosition()).isEqualTo(1);
        assertThat(firstPlace.getVoteCount()).isEqualTo(20L);
        assertThat(firstPlace.getPointsEarned()).isEqualTo(1000);
        assertThat(firstPlace.getBadgePointsEarned()).isEqualTo(50);

        // 2등 확인
        var secondPlace = result.getResultDetails().get(1);
        assertThat(secondPlace.getUser()).isEqualTo(user2);
        assertThat(secondPlace.getFeedTitle()).isEqualTo("테스트 피드 2");
        assertThat(secondPlace.getRankPosition()).isEqualTo(2);
        assertThat(secondPlace.getVoteCount()).isEqualTo(15L);
        assertThat(secondPlace.getPointsEarned()).isEqualTo(500);
        assertThat(secondPlace.getBadgePointsEarned()).isEqualTo(30);

        // 3등 확인
        var thirdPlace = result.getResultDetails().get(2);
        assertThat(thirdPlace.getUser()).isEqualTo(user3);
        assertThat(thirdPlace.getFeedTitle()).isEqualTo("테스트 피드 3");
        assertThat(thirdPlace.getRankPosition()).isEqualTo(3);
        assertThat(thirdPlace.getVoteCount()).isEqualTo(10L);
        assertThat(thirdPlace.getPointsEarned()).isEqualTo(300);
        assertThat(thirdPlace.getBadgePointsEarned()).isEqualTo(20);
    }

    @Test
    @DisplayName("랭킹 이벤트 결과 계산 - 참여자가 2명인 경우")
    void calculateResult_TwoParticipants() {
        // given
        List<Feed> participants = Arrays.asList(feed1, feed2);
        when(feedVoteRepository.countByFeedId(any())).thenReturn(20L, 15L);

        // when
        EventResult result = rankingEventStrategy.calculateResult(testEvent, participants);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getResultDetails()).hasSize(2);
        assertThat(result.getTotalVotes()).isEqualTo(35L); // 20 + 15

        // 1등과 2등만 존재
        assertThat(result.getResultDetails().get(0).getRankPosition()).isEqualTo(1);
        assertThat(result.getResultDetails().get(1).getRankPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("랭킹 이벤트 결과 계산 - 참여자가 없는 경우")
    void calculateResult_NoParticipants() {
        // given
        List<Feed> participants = Arrays.asList();

        // when & then
        assertThatThrownBy(() -> rankingEventStrategy.calculateResult(testEvent, participants))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("랭킹 이벤트에 참여자가 없습니다");
    }

    @Test
    @DisplayName("랭킹 이벤트 참여 가능 여부 확인")
    void canParticipate_ReturnsTrue() {
        // when
        boolean canParticipate = rankingEventStrategy.canParticipate(testEvent, user1);

        // then
        assertThat(canParticipate).isTrue();
    }

    @Test
    @DisplayName("랭킹 이벤트 참여 검증")
    void validateParticipation_Success() {
        // when & then (예외가 발생하지 않아야 함)
        rankingEventStrategy.validateParticipation(testEvent, feed1);
    }

    @Test
    @DisplayName("랭킹 이벤트 참여자 정보 생성")
    void createParticipant_Success() {
        // when
        EventStrategy.EventParticipantInfo participantInfo = 
                rankingEventStrategy.createParticipant(testEvent, user1, feed1);

        // then
        assertThat(participantInfo).isNotNull();
        assertThat(participantInfo.getUserId()).isNotNull();
        assertThat(participantInfo.getFeedId()).isNotNull();
        assertThat(participantInfo.getStatus()).isEqualTo("PARTICIPATING");
        assertThat(participantInfo.getMetadata()).contains("currentRank");
    }
}
