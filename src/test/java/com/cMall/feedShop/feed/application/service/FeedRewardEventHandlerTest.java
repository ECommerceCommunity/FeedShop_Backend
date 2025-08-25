package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.feed.domain.model.FeedRewardEvent;
import com.cMall.feedShop.feed.domain.repository.FeedRewardEventRepository;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.model.RewardPolicy;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.RewardPolicyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FeedRewardEventHandler 테스트")
class FeedRewardEventHandlerTest {

    @Mock
    private FeedRewardEventRepository feedRewardEventRepository;

    @Mock
    private RewardPolicyRepository rewardPolicyRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FeedRewardEventHandler feedRewardEventHandler;

    private User testUser;
    private Feed testFeed;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성 (Mock 활용)
        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(1L);

        // 테스트 피드 생성 (Mock 활용)
        testFeed = mock(Feed.class);
        when(testFeed.getId()).thenReturn(1L);

        // 테스트 이벤트 생성 (Mock 활용)
        testEvent = mock(Event.class);
        when(testEvent.getId()).thenReturn(1L);
    }

    @Test
    @DisplayName("피드 생성 리워드 이벤트 생성 성공")
    void createFeedCreationEvent_Success() throws Exception {
        // given
        RewardPolicy testPolicy = mock(RewardPolicy.class);
        when(testPolicy.getRewardType()).thenReturn(RewardType.FEED_CREATION);
        when(testPolicy.getPoints()).thenReturn(100);
        when(testPolicy.getDailyLimit()).thenReturn(5);
        
        when(rewardPolicyRepository.findByRewardType(RewardType.FEED_CREATION))
                .thenReturn(Optional.of(testPolicy));
        when(feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(
                any(User.class), any(Feed.class), any(RewardType.class)))
                .thenReturn(false);
        when(feedRewardEventRepository.countDailyEventsByUserAndType(
                any(User.class), any(RewardType.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"feedType\":\"DAILY\",\"hasEvent\":false}");
        when(feedRewardEventRepository.save(any(FeedRewardEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        feedRewardEventHandler.createFeedCreationEvent(testUser, testFeed);

        // then
        verify(feedRewardEventRepository).save(any(FeedRewardEvent.class));
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    @DisplayName("피드 생성 리워드 이벤트 생성 - 정책 없음")
    void createFeedCreationEvent_NoPolicy() {
        // given
        when(rewardPolicyRepository.findByRewardType(RewardType.FEED_CREATION))
                .thenReturn(Optional.empty());

        // when
        feedRewardEventHandler.createFeedCreationEvent(testUser, testFeed);

        // then
        verify(feedRewardEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("피드 생성 리워드 이벤트 생성 - 이미 존재")
    void createFeedCreationEvent_AlreadyExists() {
        // given
        RewardPolicy testPolicy = mock(RewardPolicy.class);
        when(testPolicy.getRewardType()).thenReturn(RewardType.FEED_CREATION);
        when(testPolicy.getPoints()).thenReturn(100);
        when(testPolicy.getDailyLimit()).thenReturn(5);
        
        when(rewardPolicyRepository.findByRewardType(RewardType.FEED_CREATION))
                .thenReturn(Optional.of(testPolicy));
        when(feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(
                any(User.class), any(Feed.class), any(RewardType.class)))
                .thenReturn(true);

        // when
        feedRewardEventHandler.createFeedCreationEvent(testUser, testFeed);

        // then
        verify(feedRewardEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("이벤트 피드 참여 리워드 이벤트 생성 성공")
    void createEventFeedParticipationEvent_Success() throws Exception {
        // given
        RewardPolicy testPolicy = mock(RewardPolicy.class);
        when(testPolicy.getRewardType()).thenReturn(RewardType.EVENT_FEED_PARTICIPATION);
        when(testPolicy.getPoints()).thenReturn(100);
        when(testPolicy.getDailyLimit()).thenReturn(5);
        
        when(rewardPolicyRepository.findByRewardType(RewardType.EVENT_FEED_PARTICIPATION))
                .thenReturn(Optional.of(testPolicy));
        when(feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(
                any(User.class), any(Feed.class), any(RewardType.class)))
                .thenReturn(false);
        when(feedRewardEventRepository.countDailyEventsByUserAndType(
                any(User.class), any(RewardType.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventId\":1,\"eventType\":\"EVENT_PARTICIPATION\"}");
        when(feedRewardEventRepository.save(any(FeedRewardEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        feedRewardEventHandler.createEventFeedParticipationEvent(testUser, testFeed, testEvent.getId());

        // then
        verify(feedRewardEventRepository).save(any(FeedRewardEvent.class));
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    @DisplayName("댓글 일일 달성 리워드 이벤트 생성 성공")
    void createCommentDailyAchievementEvent_Success() throws Exception {
        // given
        RewardPolicy testPolicy = mock(RewardPolicy.class);
        when(testPolicy.getRewardType()).thenReturn(RewardType.COMMENT_DAILY_ACHIEVEMENT);
        when(testPolicy.getPoints()).thenReturn(100);
        when(testPolicy.getDailyLimit()).thenReturn(5);
        
        when(rewardPolicyRepository.findByRewardType(RewardType.COMMENT_DAILY_ACHIEVEMENT))
                .thenReturn(Optional.of(testPolicy));
        when(feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(
                any(User.class), any(Feed.class), any(RewardType.class)))
                .thenReturn(false);
        when(feedRewardEventRepository.countDailyEventsByUserAndType(
                any(User.class), any(RewardType.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"commentCount\":1,\"eventType\":\"COMMENT_DAILY\"}");
        when(feedRewardEventRepository.save(any(FeedRewardEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        feedRewardEventHandler.createCommentDailyAchievementEvent(testUser, testFeed);

        // then
        verify(feedRewardEventRepository).save(any(FeedRewardEvent.class));
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    @DisplayName("댓글 일일 달성 리워드 이벤트 생성 - 일일 제한 도달")
    void createCommentDailyAchievementEvent_DailyLimitReached() {
        // given
        RewardPolicy testPolicy = mock(RewardPolicy.class);
        when(testPolicy.getRewardType()).thenReturn(RewardType.COMMENT_DAILY_ACHIEVEMENT);
        when(testPolicy.getPoints()).thenReturn(100);
        when(testPolicy.getDailyLimit()).thenReturn(5);
        
        when(rewardPolicyRepository.findByRewardType(RewardType.COMMENT_DAILY_ACHIEVEMENT))
                .thenReturn(Optional.of(testPolicy));
        when(feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(
                any(User.class), any(Feed.class), any(RewardType.class)))
                .thenReturn(false);
        when(feedRewardEventRepository.countDailyEventsByUserAndType(
                any(User.class), any(RewardType.class), any(LocalDateTime.class)))
                .thenReturn(5L); // 일일 제한 도달

        // when
        feedRewardEventHandler.createCommentDailyAchievementEvent(testUser, testFeed);

        // then
        verify(feedRewardEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("좋아요 마일스톤 리워드 이벤트 생성 성공")
    void createFeedLikesMilestoneEvent_Success() throws Exception {
        // given
        RewardPolicy testPolicy = mock(RewardPolicy.class);
        when(testPolicy.getRewardType()).thenReturn(RewardType.FEED_LIKES_MILESTONE);
        when(testPolicy.getPoints()).thenReturn(100);
        when(testPolicy.getDailyLimit()).thenReturn(5);
        
        when(rewardPolicyRepository.findByRewardType(RewardType.FEED_LIKES_MILESTONE))
                .thenReturn(Optional.of(testPolicy));
        when(feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(
                any(User.class), any(Feed.class), any(RewardType.class)))
                .thenReturn(false);
        when(feedRewardEventRepository.countDailyEventsByUserAndType(
                any(User.class), any(RewardType.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"likeCount\":100,\"milestone\":\"100\"}");
        when(feedRewardEventRepository.save(any(FeedRewardEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        feedRewardEventHandler.createFeedLikesMilestoneEvent(testUser, testFeed, 100);

        // then
        verify(feedRewardEventRepository).save(any(FeedRewardEvent.class));
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    @DisplayName("좋아요 마일스톤 리워드 이벤트 생성 - 마일스톤 미달성")
    void createFeedLikesMilestoneEvent_NotMilestone() {
        // given
        RewardPolicy testPolicy = mock(RewardPolicy.class);
        when(testPolicy.getRewardType()).thenReturn(RewardType.FEED_LIKES_MILESTONE);
        when(testPolicy.getPoints()).thenReturn(100);
        when(testPolicy.getDailyLimit()).thenReturn(5);

        // when
        feedRewardEventHandler.createFeedLikesMilestoneEvent(testUser, testFeed, 25);

        // then
        verify(feedRewardEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("다양한 상품 피드 리워드 이벤트 생성 성공")
    void createDiverseProductFeedEvent_Success() throws Exception {
        // given
        RewardPolicy testPolicy = mock(RewardPolicy.class);
        when(testPolicy.getRewardType()).thenReturn(RewardType.DIVERSE_PRODUCT_FEED);
        when(testPolicy.getPoints()).thenReturn(100);
        when(testPolicy.getDailyLimit()).thenReturn(5);
        
        when(rewardPolicyRepository.findByRewardType(RewardType.DIVERSE_PRODUCT_FEED))
                .thenReturn(Optional.of(testPolicy));
        when(feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(
                any(User.class), any(Feed.class), any(RewardType.class)))
                .thenReturn(false);
        when(feedRewardEventRepository.countDailyEventsByUserAndType(
                any(User.class), any(RewardType.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"productCount\":5,\"eventType\":\"DIVERSE_PRODUCT_FEED\"}");
        when(feedRewardEventRepository.save(any(FeedRewardEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        feedRewardEventHandler.createDiverseProductFeedEvent(testUser, testFeed, 5);

        // then
        verify(feedRewardEventRepository).save(any(FeedRewardEvent.class));
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    @DisplayName("다양한 상품 피드 리워드 이벤트 생성 - 상품 수 부족")
    void createDiverseProductFeedEvent_InsufficientProducts() {
        // given
        RewardPolicy testPolicy = mock(RewardPolicy.class);
        when(testPolicy.getRewardType()).thenReturn(RewardType.DIVERSE_PRODUCT_FEED);
        when(testPolicy.getPoints()).thenReturn(100);
        when(testPolicy.getDailyLimit()).thenReturn(5);

        // when
        feedRewardEventHandler.createDiverseProductFeedEvent(testUser, testFeed, 2);

        // then
        verify(feedRewardEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("ObjectMapper 예외 발생 시 처리")
    void createFeedCreationEvent_ObjectMapperException() throws Exception {
        // given
        RewardPolicy testPolicy = mock(RewardPolicy.class);
        when(testPolicy.getRewardType()).thenReturn(RewardType.FEED_CREATION);
        when(testPolicy.getPoints()).thenReturn(100);
        when(testPolicy.getDailyLimit()).thenReturn(5);
        
        when(rewardPolicyRepository.findByRewardType(RewardType.FEED_CREATION))
                .thenReturn(Optional.of(testPolicy));
        when(feedRewardEventRepository.existsByUserAndFeedAndRewardTypeAndActiveStatus(
                any(User.class), any(Feed.class), any(RewardType.class)))
                .thenReturn(false);
        when(feedRewardEventRepository.countDailyEventsByUserAndType(
                any(User.class), any(RewardType.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON 변환 실패"));

        // when
        feedRewardEventHandler.createFeedCreationEvent(testUser, testFeed);

        // then
        verify(feedRewardEventRepository, never()).save(any());
    }
}
