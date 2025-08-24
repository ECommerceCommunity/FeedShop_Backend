package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.request.FeedRewardEventSearchRequest;
import com.cMall.feedShop.feed.application.dto.response.FeedRewardEventResponseDto;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.model.FeedRewardEvent;
import com.cMall.feedShop.feed.domain.repository.FeedRewardEventRepository;
import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FeedRewardEventManagementService 테스트")
class FeedRewardEventManagementServiceTest {

    @Mock
    private FeedRewardEventRepository feedRewardEventRepository;

    @Mock
    private FeedRewardEventProcessor feedRewardEventProcessor;

    @InjectMocks
    private FeedRewardEventManagementService feedRewardEventManagementService;

    private User testUser;
    private UserProfile testUserProfile;
    private Feed testFeed;
    private FeedRewardEvent testEvent;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 프로필 생성 (Mock 활용)
        testUserProfile = mock(UserProfile.class);
        when(testUserProfile.getNickname()).thenReturn("테스트 사용자");

        // 테스트 사용자 생성 (Mock 활용)
        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(1L);
        when(testUser.getUserProfile()).thenReturn(testUserProfile);

        // 테스트 피드 생성 (Mock 활용)
        testFeed = mock(Feed.class);
        when(testFeed.getId()).thenReturn(1L);
        when(testFeed.getTitle()).thenReturn("테스트 피드");

        // 테스트 이벤트 생성 (Mock 활용)
        testEvent = mock(FeedRewardEvent.class);
        when(testEvent.getEventId()).thenReturn(1L);
        when(testEvent.getFeed()).thenReturn(testFeed);
        when(testEvent.getUser()).thenReturn(testUser);
        when(testEvent.getRewardType()).thenReturn(RewardType.FEED_CREATION);
        when(testEvent.getEventStatus()).thenReturn(FeedRewardEvent.EventStatus.PENDING);
        when(testEvent.getPoints()).thenReturn(100);
        when(testEvent.getDescription()).thenReturn("테스트 설명");
        when(testEvent.getRelatedData()).thenReturn("{}");
        when(testEvent.getRetryCount()).thenReturn(0);
        when(testEvent.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(testEvent.getUpdatedAt()).thenReturn(LocalDateTime.now());
    }

    @Test
    @DisplayName("리워드 이벤트 목록 조회 성공 - 기본 조회")
    void getRewardEvents_Success_Default() {
        // given
        FeedRewardEventSearchRequest request = FeedRewardEventSearchRequest.builder()
                .page(0)
                .size(20)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        Page<FeedRewardEvent> eventPage = new PageImpl<>(List.of(testEvent));
        when(feedRewardEventRepository.findAll(any(Pageable.class))).thenReturn(eventPage);

        // when
        Page<FeedRewardEventResponseDto> result = feedRewardEventManagementService.getRewardEvents(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(feedRewardEventRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("리워드 이벤트 목록 조회 성공 - 사용자별 조회")
    void getRewardEvents_Success_ByUserId() {
        // given
        FeedRewardEventSearchRequest request = FeedRewardEventSearchRequest.builder()
                .page(0)
                .size(20)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .userId(1L)
                .build();

        Page<FeedRewardEvent> eventPage = new PageImpl<>(List.of(testEvent));
        when(feedRewardEventRepository.findByUserOrderByCreatedAtDesc(anyLong(), any(Pageable.class)))
                .thenReturn(eventPage);

        // when
        Page<FeedRewardEventResponseDto> result = feedRewardEventManagementService.getRewardEvents(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(feedRewardEventRepository).findByUserOrderByCreatedAtDesc(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("리워드 이벤트 목록 조회 성공 - 피드별 조회")
    void getRewardEvents_Success_ByFeedId() {
        // given
        FeedRewardEventSearchRequest request = FeedRewardEventSearchRequest.builder()
                .page(0)
                .size(20)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .feedId(1L)
                .build();

        Page<FeedRewardEvent> eventPage = new PageImpl<>(List.of(testEvent));
        when(feedRewardEventRepository.findByFeedOrderByCreatedAtDesc(anyLong(), any(Pageable.class)))
                .thenReturn(eventPage);

        // when
        Page<FeedRewardEventResponseDto> result = feedRewardEventManagementService.getRewardEvents(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(feedRewardEventRepository).findByFeedOrderByCreatedAtDesc(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("리워드 이벤트 목록 조회 성공 - 리워드 타입별 조회")
    void getRewardEvents_Success_ByRewardType() {
        // given
        FeedRewardEventSearchRequest request = FeedRewardEventSearchRequest.builder()
                .page(0)
                .size(20)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .rewardType(RewardType.FEED_CREATION)
                .build();

        Page<FeedRewardEvent> eventPage = new PageImpl<>(List.of(testEvent));
        when(feedRewardEventRepository.findByRewardTypeOrderByCreatedAtDesc(any(RewardType.class), any(Pageable.class)))
                .thenReturn(eventPage);

        // when
        Page<FeedRewardEventResponseDto> result = feedRewardEventManagementService.getRewardEvents(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(feedRewardEventRepository).findByRewardTypeOrderByCreatedAtDesc(any(RewardType.class), any(Pageable.class));
    }

    @Test
    @DisplayName("리워드 이벤트 목록 조회 성공 - 이벤트 상태별 조회")
    void getRewardEvents_Success_ByEventStatus() {
        // given
        FeedRewardEventSearchRequest request = FeedRewardEventSearchRequest.builder()
                .page(0)
                .size(20)
                .sortBy("createdAt")
                .sortDirection("ASC")
                .eventStatus(FeedRewardEvent.EventStatus.PENDING)
                .build();

        Page<FeedRewardEvent> eventPage = new PageImpl<>(List.of(testEvent));
        when(feedRewardEventRepository.findByEventStatusOrderByCreatedAtAsc(any(FeedRewardEvent.EventStatus.class), any(Pageable.class)))
                .thenReturn(eventPage);

        // when
        Page<FeedRewardEventResponseDto> result = feedRewardEventManagementService.getRewardEvents(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(feedRewardEventRepository).findByEventStatusOrderByCreatedAtAsc(any(FeedRewardEvent.EventStatus.class), any(Pageable.class));
    }

    @Test
    @DisplayName("리워드 이벤트 목록 조회 실패 - 잘못된 요청 파라미터")
    void getRewardEvents_Failure_InvalidRequest() {
        // given
        FeedRewardEventSearchRequest request = FeedRewardEventSearchRequest.builder()
                .page(-1) // 잘못된 페이지 번호
                .size(20)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        // when & then
        assertThatThrownBy(() -> feedRewardEventManagementService.getRewardEvents(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잘못된 요청 파라미터입니다");
    }

    @Test
    @DisplayName("사용자별 리워드 이벤트 조회 성공")
    void getRewardEventsByUser_Success() {
        // given
        when(feedRewardEventRepository.findByUserOrderByCreatedAtDesc(anyLong()))
                .thenReturn(List.of(testEvent));

        // when
        List<FeedRewardEventResponseDto> result = feedRewardEventManagementService.getRewardEventsByUser(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(feedRewardEventRepository).findByUserOrderByCreatedAtDesc(anyLong());
    }

    @Test
    @DisplayName("피드별 리워드 이벤트 조회 성공")
    void getRewardEventsByFeed_Success() {
        // given
        when(feedRewardEventRepository.findByFeedIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(List.of(testEvent));

        // when
        List<FeedRewardEventResponseDto> result = feedRewardEventManagementService.getRewardEventsByFeed(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(feedRewardEventRepository).findByFeedIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    @DisplayName("리워드 이벤트 통계 조회 성공")
    void getRewardEventStatistics_Success() {
        // given
        when(feedRewardEventRepository.count()).thenReturn(10L);
        when(feedRewardEventRepository.countByEventStatus(FeedRewardEvent.EventStatus.PENDING)).thenReturn(5L);
        when(feedRewardEventRepository.countByEventStatus(FeedRewardEvent.EventStatus.PROCESSING)).thenReturn(2L);
        when(feedRewardEventRepository.countByEventStatus(FeedRewardEvent.EventStatus.PROCESSED)).thenReturn(3L);
        when(feedRewardEventRepository.countByEventStatus(FeedRewardEvent.EventStatus.FAILED)).thenReturn(0L);
        when(feedRewardEventRepository.countByRewardType()).thenReturn(Map.of(RewardType.FEED_CREATION, 10L));
        when(feedRewardEventRepository.sumPointsByEventStatus(FeedRewardEvent.EventStatus.PROCESSED)).thenReturn(500);

        // when
        Map<String, Object> result = feedRewardEventManagementService.getRewardEventStatistics();

        // then
        assertThat(result).isNotNull();
        assertThat(result.get("totalEvents")).isEqualTo(10L);
        assertThat(result.get("pendingEvents")).isEqualTo(5L);
        assertThat(result.get("totalPoints")).isEqualTo(500);
        verify(feedRewardEventRepository).count();
        verify(feedRewardEventRepository).countByEventStatus(FeedRewardEvent.EventStatus.PENDING);
        verify(feedRewardEventRepository).countByEventStatus(FeedRewardEvent.EventStatus.PROCESSING);
        verify(feedRewardEventRepository).countByEventStatus(FeedRewardEvent.EventStatus.PROCESSED);
        verify(feedRewardEventRepository).countByEventStatus(FeedRewardEvent.EventStatus.FAILED);
        verify(feedRewardEventRepository).countByRewardType();
        verify(feedRewardEventRepository).sumPointsByEventStatus(FeedRewardEvent.EventStatus.PROCESSED);
    }

    @Test
    @DisplayName("일별 리워드 이벤트 통계 조회 성공")
    void getDailyRewardEventStatistics_Success() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        when(feedRewardEventRepository.countDailyCreatedEvents(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Map.of("2025-08-24", 5L));
        when(feedRewardEventRepository.countDailyProcessedEvents(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Map.of("2025-08-24", 3L));
        when(feedRewardEventRepository.sumDailyPoints(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Map.of("2025-08-24", 300));

        // when
        Map<String, Object> result = feedRewardEventManagementService.getDailyRewardEventStatistics(startDate, endDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.get("dailyCreatedEvents")).isNotNull();
        assertThat(result.get("dailyProcessedEvents")).isNotNull();
        assertThat(result.get("dailyPoints")).isNotNull();
        verify(feedRewardEventRepository).countDailyCreatedEvents(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(feedRewardEventRepository).countDailyProcessedEvents(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(feedRewardEventRepository).sumDailyPoints(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("수동 이벤트 처리 성공")
    void processEventManually_Success() {
        // given
        doNothing().when(feedRewardEventProcessor).processSpecificEvent(anyLong());

        // when
        feedRewardEventManagementService.processEventManually(1L);

        // then
        verify(feedRewardEventProcessor).processSpecificEvent(1L);
    }

    @Test
    @DisplayName("실패한 이벤트 재처리 성공")
    void retryFailedEvents_Success() {
        // given
        doNothing().when(feedRewardEventProcessor).retryFailedRewardEvents();

        // when
        feedRewardEventManagementService.retryFailedEvents();

        // then
        verify(feedRewardEventProcessor).retryFailedRewardEvents();
    }

    @Test
    @DisplayName("리워드 이벤트 상세 조회 성공")
    void getRewardEventDetail_Success() {
        // given
        when(feedRewardEventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));

        // when
        FeedRewardEventResponseDto result = feedRewardEventManagementService.getRewardEventDetail(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEventId()).isEqualTo(1L);
        verify(feedRewardEventRepository).findById(1L);
    }

    @Test
    @DisplayName("리워드 이벤트 상세 조회 실패 - 이벤트 없음")
    void getRewardEventDetail_Failure_EventNotFound() {
        // given
        when(feedRewardEventRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedRewardEventManagementService.getRewardEventDetail(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("리워드 이벤트를 찾을 수 없습니다: 1");
    }
}
