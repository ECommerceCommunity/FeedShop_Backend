package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.MyFeedListResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedAccessDeniedException;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MyFeedReadService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class MyFeedReadServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FeedMapper feedMapper;

    @InjectMocks
    private MyFeedReadService myFeedReadService;

    private User testUser;
    private Feed testFeed;
    private MyFeedListResponseDto testFeedDto;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        // 테스트용 User 엔티티 생성
        testUser = new User(1L, "test_user", "password", "test@example.com", com.cMall.feedShop.user.domain.enums.UserRole.USER);

        // 테스트용 Feed 엔티티 생성
        testFeed = Feed.builder()
                .title("테스트 마이피드")
                .content("테스트 내용")
                .instagramId("test_user")
                .user(testUser)
                .build();

        // 테스트용 DTO 생성
        testFeedDto = MyFeedListResponseDto.builder()
                .feedId(1L)
                .title("테스트 마이피드")
                .content("테스트 내용")
                .feedType(FeedType.DAILY)
                .instagramId("test_user")
                .likeCount(10)
                .commentCount(5)
                .participantVoteCount(2)
                .createdAt(LocalDateTime.now())
                .userId(1L)
                .userNickname("테스트 사용자")
                .build();

        // 테스트용 Pageable 생성
        testPageable = PageRequest.of(0, 20);
    }

    @Test
    @DisplayName("마이피드 목록 조회 - 성공")
    void getMyFeeds_Success() {
        // given
        Long userId = 1L;
        List<Feed> feeds = List.of(testFeed);
        Page<Feed> feedPage = new PageImpl<>(feeds, testPageable, 1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(feedRepository.findByUserId(userId, testPageable)).thenReturn(feedPage);
        when(feedMapper.toMyFeedListResponseDto(any(Feed.class))).thenReturn(testFeedDto);

        // when
        Page<MyFeedListResponseDto> result = myFeedReadService.getMyFeeds(userId, testPageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFeedId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 마이피드");

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, times(1)).findByUserId(userId, testPageable);
        verify(feedMapper, times(1)).toMyFeedListResponseDto(testFeed);
    }

    @Test
    @DisplayName("마이피드 타입별 조회 - 성공")
    void getMyFeedsByType_Success() {
        // given
        Long userId = 1L;
        FeedType feedType = FeedType.EVENT;
        List<Feed> feeds = List.of(testFeed);
        Page<Feed> feedPage = new PageImpl<>(feeds, testPageable, 1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(feedRepository.findByUserIdAndFeedType(userId, feedType.name(), testPageable)).thenReturn(feedPage);
        when(feedMapper.toMyFeedListResponseDto(any(Feed.class))).thenReturn(testFeedDto);

        // when
        Page<MyFeedListResponseDto> result = myFeedReadService.getMyFeedsByType(userId, feedType, testPageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, times(1)).findByUserIdAndFeedType(userId, feedType.name(), testPageable);
        verify(feedMapper, times(1)).toMyFeedListResponseDto(testFeed);
    }

    @Test
    @DisplayName("마이피드 목록 조회 - 사용자가 존재하지 않는 경우")
    void getMyFeeds_UserNotFound() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myFeedReadService.getMyFeeds(userId, testPageable))
                .isInstanceOf(FeedAccessDeniedException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, never()).findByUserId(any(), any());
        verify(feedMapper, never()).toMyFeedListResponseDto(any());
    }

    @Test
    @DisplayName("마이피드 타입별 조회 - 사용자가 존재하지 않는 경우")
    void getMyFeedsByType_UserNotFound() {
        // given
        Long userId = 999L;
        FeedType feedType = FeedType.DAILY;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myFeedReadService.getMyFeedsByType(userId, feedType, testPageable))
                .isInstanceOf(FeedAccessDeniedException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, never()).findByUserIdAndFeedType(any(), any(), any());
        verify(feedMapper, never()).toMyFeedListResponseDto(any());
    }

    @Test
    @DisplayName("빈 마이피드 목록 조회 - 성공")
    void getMyFeeds_EmptyList_Success() {
        // given
        Long userId = 1L;
        Page<Feed> emptyPage = new PageImpl<>(List.of(), testPageable, 0);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(feedRepository.findByUserId(userId, testPageable)).thenReturn(emptyPage);

        // when
        Page<MyFeedListResponseDto> result = myFeedReadService.getMyFeeds(userId, testPageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, times(1)).findByUserId(userId, testPageable);
        verify(feedMapper, never()).toMyFeedListResponseDto(any());
    }

    @Test
    @DisplayName("여러 마이피드 목록 조회 - 성공")
    void getMyFeeds_MultipleFeeds_Success() {
        // given
        Long userId = 1L;
        Feed feed1 = Feed.builder().title("마이피드1").user(testUser).build();
        Feed feed2 = Feed.builder().title("마이피드2").user(testUser).build();
        List<Feed> feeds = List.of(feed1, feed2);
        Page<Feed> feedPage = new PageImpl<>(feeds, testPageable, 2);

        MyFeedListResponseDto dto1 = MyFeedListResponseDto.builder().feedId(1L).title("마이피드1").build();
        MyFeedListResponseDto dto2 = MyFeedListResponseDto.builder().feedId(2L).title("마이피드2").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(feedRepository.findByUserId(userId, testPageable)).thenReturn(feedPage);
        when(feedMapper.toMyFeedListResponseDto(feed1)).thenReturn(dto1);
        when(feedMapper.toMyFeedListResponseDto(feed2)).thenReturn(dto2);

        // when
        Page<MyFeedListResponseDto> result = myFeedReadService.getMyFeeds(userId, testPageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getFeedId()).isEqualTo(1L);
        assertThat(result.getContent().get(1).getFeedId()).isEqualTo(2L);

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, times(1)).findByUserId(userId, testPageable);
        verify(feedMapper, times(2)).toMyFeedListResponseDto(any());
    }

    @Test
    @DisplayName("마이피드 개수 조회 - 성공")
    void getMyFeedCount_Success() {
        // given
        Long userId = 1L;
        long expectedCount = 5L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(feedRepository.countByUserId(userId)).thenReturn(expectedCount);

        // when
        long result = myFeedReadService.getMyFeedCount(userId);

        // then
        assertThat(result).isEqualTo(expectedCount);

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, times(1)).countByUserId(userId);
    }

    @Test
    @DisplayName("마이피드 타입별 개수 조회 - 성공")
    void getMyFeedCountByType_Success() {
        // given
        Long userId = 1L;
        FeedType feedType = FeedType.EVENT;
        long expectedCount = 3L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(feedRepository.countByUserIdAndFeedType(userId, feedType.name())).thenReturn(expectedCount);

        // when
        long result = myFeedReadService.getMyFeedCountByType(userId, feedType);

        // then
        assertThat(result).isEqualTo(expectedCount);

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, times(1)).countByUserIdAndFeedType(userId, feedType.name());
    }

    @Test
    @DisplayName("마이피드 개수 조회 - 사용자가 존재하지 않는 경우")
    void getMyFeedCount_UserNotFound() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myFeedReadService.getMyFeedCount(userId))
                .isInstanceOf(FeedAccessDeniedException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, never()).countByUserId(any());
    }

    @Test
    @DisplayName("마이피드 타입별 개수 조회 - 사용자가 존재하지 않는 경우")
    void getMyFeedCountByType_UserNotFound() {
        // given
        Long userId = 999L;
        FeedType feedType = FeedType.RANKING;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myFeedReadService.getMyFeedCountByType(userId, feedType))
                .isInstanceOf(FeedAccessDeniedException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, never()).countByUserIdAndFeedType(any(), any());
    }
} 