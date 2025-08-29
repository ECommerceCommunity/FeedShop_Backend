package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventResult;
import com.cMall.feedShop.event.domain.EventResultDetail;
import com.cMall.feedShop.event.domain.repository.EventResultRepository;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.user.domain.model.User;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedClassificationService 테스트")
class FeedClassificationServiceTest {

    @Mock
    private EventResultRepository eventResultRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private Event event;

    @Mock
    private EventResult eventResult;

    @Mock
    private User user1, user2, user3;

    @Mock
    private Feed feed1, feed2, feed3;

    @InjectMocks
    private FeedClassificationService feedClassificationService;

    @BeforeEach
    void setUp() {
        // 기본 Mock 설정은 각 테스트에서 필요한 것만 설정
    }

    @Test
    @DisplayName("이벤트 결과에 따른 피드 분류 성공 - 랭킹 이벤트")
    void classifyFeedsAfterEventResult_rankingEvent() {
        // given
        Long eventId = 1L;
        
        // Mock 설정
        when(feed1.getId()).thenReturn(1L);
        when(feed2.getId()).thenReturn(2L);
        when(feed3.getId()).thenReturn(3L);
        
        // 이벤트 결과 상세 정보 생성 (TOP 3)
        EventResultDetail detail1 = createEventResultDetail(1L, 1L, 1, 100L);
        EventResultDetail detail2 = createEventResultDetail(2L, 2L, 2, 80L);
        EventResultDetail detail3 = createEventResultDetail(3L, 3L, 3, 60L);
        
        when(eventResultRepository.findByEventId(eventId))
                .thenReturn(Optional.of(eventResult));
        when(eventResult.getResultDetails())
                .thenReturn(Arrays.asList(detail1, detail2, detail3));
        when(feedRepository.findByEventId(eventId))
                .thenReturn(Arrays.asList(feed1, feed2, feed3));

        // when
        feedClassificationService.classifyFeedsAfterEventResult(eventId);

        // then
        // TOP 3는 RANKING으로, 나머지는 DAILY로 분류되어야 함
        verify(feed1).updateFeedType(FeedType.RANKING);
        verify(feed2).updateFeedType(FeedType.RANKING);
        verify(feed3).updateFeedType(FeedType.RANKING);
    }

    @Test
    @DisplayName("이벤트 결과에 따른 피드 분류 성공 - 배틀 이벤트")
    void classifyFeedsAfterEventResult_battleEvent() {
        // given
        Long eventId = 1L;
        
        // Mock 설정
        when(feed1.getId()).thenReturn(1L);
        when(feed2.getId()).thenReturn(2L);
        when(feed3.getId()).thenReturn(3L);
        
        // 배틀 이벤트 결과 (우승자만)
        EventResultDetail winnerDetail = createEventResultDetail(1L, 1L, 1, 100L);
        
        when(eventResultRepository.findByEventId(eventId))
                .thenReturn(Optional.of(eventResult));
        when(eventResult.getResultDetails())
                .thenReturn(Arrays.asList(winnerDetail));
        when(feedRepository.findByEventId(eventId))
                .thenReturn(Arrays.asList(feed1, feed2, feed3));

        // when
        feedClassificationService.classifyFeedsAfterEventResult(eventId);

        // then
        // 우승자는 RANKING으로, 참여자는 DAILY로 분류
        verify(feed1).updateFeedType(FeedType.RANKING);
        verify(feed2).updateFeedType(FeedType.DAILY);
        verify(feed3).updateFeedType(FeedType.DAILY);
    }

    @Test
    @DisplayName("이벤트 결과에 따른 피드 분류 실패 - 이벤트 결과가 없음")
    void classifyFeedsAfterEventResult_eventResultNotFound() {
        // given
        Long eventId = 999L;
        when(eventResultRepository.findByEventId(eventId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedClassificationService.classifyFeedsAfterEventResult(eventId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이벤트 결과를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("피드 분류 상태 확인 성공")
    void getFeedClassification_success() {
        // given
        Long feedId = 1L;
        when(feedRepository.findById(feedId))
                .thenReturn(Optional.of(feed1));
        when(feed1.getFeedType())
                .thenReturn(FeedType.RANKING);

        // when
        FeedType result = feedClassificationService.getFeedClassification(feedId);

        // then
        assertThat(result).isEqualTo(FeedType.RANKING);
    }

    @Test
    @DisplayName("피드 분류 상태 확인 실패 - 피드가 없음")
    void getFeedClassification_feedNotFound() {
        // given
        Long feedId = 999L;
        when(feedRepository.findById(feedId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedClassificationService.getFeedClassification(feedId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("피드를 찾을 수 없습니다");
    }

    /**
     * 이벤트 결과 상세 정보 생성 헬퍼 메서드
     */
    private EventResultDetail createEventResultDetail(Long userId, Long feedId, Integer rank, Long voteCount) {
        return EventResultDetail.builder()
                .user(mock(User.class))
                .feedId(feedId)
                .feedTitle("테스트 피드 " + feedId)
                .rankPosition(rank)
                .voteCount(voteCount)
                .pointsEarned(100)
                .badgePointsEarned(10)
                .rewardProcessed(false)
                .build();
    }
}
