package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.MyFeedListResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedAccessDeniedException;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.enums.FeedType;
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

    @Mock
    private FeedLikeService feedLikeService;

    @InjectMocks
    private MyFeedReadService myFeedReadService;

    private User testUser;
    private Feed testFeed;
    private MyFeedListResponseDto testFeedDto;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testPageable = PageRequest.of(0, 10);
        
        testUser = new User(1L, "testuser", "password", "test@test.com", com.cMall.feedShop.user.domain.enums.UserRole.USER);

        testFeed = Feed.builder()
                .title("테스트 피드")
                .content("테스트 내용")
                .build();

        testFeedDto = MyFeedListResponseDto.builder()
                .feedId(1L)
                .title("테스트 피드")
                .content("테스트 내용")
                .feedType(FeedType.DAILY)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("마이피드 목록 조회 - 성공")
    void getMyFeeds_Success() {
        // given
        Long userId = 1L;
        List<Feed> feeds = List.of(testFeed);
        Page<Feed> feedPage = new PageImpl<>(feeds, testPageable, 1);
        
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(testUser));
        when(feedRepository.findByUserId(userId, testPageable)).thenReturn(feedPage);
        when(feedMapper.toMyFeedListResponseDto(any(Feed.class))).thenReturn(testFeedDto);

        // when
        Page<MyFeedListResponseDto> result = myFeedReadService.getMyFeeds(userId, testPageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFeedId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 피드");

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
        
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(testUser));
        when(feedRepository.findByUserIdAndFeedType(userId, feedType.name(), testPageable)).thenReturn(feedPage);
        when(feedMapper.toMyFeedListResponseDto(any(Feed.class))).thenReturn(testFeedDto);

        // when
        Page<MyFeedListResponseDto> result = myFeedReadService.getMyFeedsByType(userId, feedType, testPageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFeedType()).isEqualTo(FeedType.DAILY);

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, times(1)).findByUserIdAndFeedType(userId, feedType.name(), testPageable);
        verify(feedMapper, times(1)).toMyFeedListResponseDto(testFeed);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 - 예외 발생")
    void getMyFeeds_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> myFeedReadService.getMyFeeds(userId, testPageable, null))
                .isInstanceOf(FeedAccessDeniedException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, never()).findByUserId(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 타입별 조회 - 예외 발생")
    void getMyFeedsByType_UserNotFound_ThrowsException() {
        // given
        Long userId = 999L;
        FeedType feedType = FeedType.DAILY;
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> myFeedReadService.getMyFeedsByType(userId, feedType, testPageable, null))
                .isInstanceOf(FeedAccessDeniedException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, never()).findByUserIdAndFeedType(any(), any(), any());
    }

    @Test
    @DisplayName("빈 마이피드 목록 조회 - 성공")
    void getMyFeeds_EmptyList_Success() {
        // given
        Long userId = 1L;
        Page<Feed> emptyPage = new PageImpl<>(List.of(), testPageable, 0);
        
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(testUser));
        when(feedRepository.findByUserId(userId, testPageable)).thenReturn(emptyPage);

        // when
        Page<MyFeedListResponseDto> result = myFeedReadService.getMyFeeds(userId, testPageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, times(1)).findByUserId(userId, testPageable);
        verify(feedMapper, never()).toMyFeedListResponseDto(any(Feed.class));
    }

    @Test
    @DisplayName("여러 마이피드 목록 조회 - 성공")
    void getMyFeeds_MultipleFeeds_Success() {
        // given
        Long userId = 1L;
        Feed feed1 = Feed.builder().title("피드1").build();
        Feed feed2 = Feed.builder().title("피드2").build();
        List<Feed> feeds = List.of(feed1, feed2);
        Page<Feed> feedPage = new PageImpl<>(feeds, testPageable, 2);
        
        MyFeedListResponseDto dto1 = MyFeedListResponseDto.builder().feedId(1L).title("피드1").build();
        MyFeedListResponseDto dto2 = MyFeedListResponseDto.builder().feedId(2L).title("피드2").build();
        
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(testUser));
        when(feedRepository.findByUserId(userId, testPageable)).thenReturn(feedPage);
        when(feedMapper.toMyFeedListResponseDto(feed1)).thenReturn(dto1);
        when(feedMapper.toMyFeedListResponseDto(feed2)).thenReturn(dto2);

        // when
        Page<MyFeedListResponseDto> result = myFeedReadService.getMyFeeds(userId, testPageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("피드1");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("피드2");

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, times(1)).findByUserId(userId, testPageable);
        verify(feedMapper, times(2)).toMyFeedListResponseDto(any(Feed.class));
    }

    @Test
    @DisplayName("마이피드 개수 조회 - 성공")
    void getMyFeedCount_Success() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(testUser));
        when(feedRepository.countByUserId(userId)).thenReturn(5L);

        // when
        long count = myFeedReadService.getMyFeedCount(userId);

        // then
        assertThat(count).isEqualTo(5L);

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, times(1)).countByUserId(userId);
    }

    @Test
    @DisplayName("마이피드 타입별 개수 조회 - 성공")
    void getMyFeedCountByType_Success() {
        // given
        Long userId = 1L;
        FeedType feedType = FeedType.EVENT;
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(testUser));
        when(feedRepository.countByUserIdAndFeedType(userId, feedType.name())).thenReturn(3L);

        // when
        long count = myFeedReadService.getMyFeedCountByType(userId, feedType);

        // then
        assertThat(count).isEqualTo(3L);

        verify(userRepository, times(1)).findById(userId);
        verify(feedRepository, times(1)).countByUserIdAndFeedType(userId, feedType.name());
    }
} 