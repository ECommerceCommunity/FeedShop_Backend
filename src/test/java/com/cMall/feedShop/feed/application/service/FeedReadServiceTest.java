package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.FeedListResponseDto;
import com.cMall.feedShop.feed.application.service.FeedMapper;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FeedReadService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class FeedReadServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private FeedLikeService feedLikeService;

    @InjectMocks
    private FeedReadService feedReadService;

    private Feed testFeed;
    private FeedListResponseDto testFeedDto;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testPageable = PageRequest.of(0, 10);
        
        testFeed = Feed.builder()
                .title("테스트 피드")
                .content("테스트 내용")
                .build();

        testFeedDto = FeedListResponseDto.builder()
                .feedId(1L)
                .title("테스트 피드")
                .content("테스트 내용")
                .feedType(FeedType.DAILY)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("전체 피드 목록 조회 - 성공")
    void getFeeds_AllFeeds_Success() {
        // given
        List<Feed> feeds = List.of(testFeed);
        Page<Feed> feedPage = new PageImpl<>(feeds, testPageable, 1);
        
        when(feedRepository.findAll(any(Pageable.class))).thenReturn(feedPage);
        when(feedMapper.toFeedListResponseDto(any(Feed.class))).thenReturn(testFeedDto);

        // when
        Page<FeedListResponseDto> result = feedReadService.getFeeds(null, testPageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFeedId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 피드");

        verify(feedRepository, times(1)).findAll(testPageable);
        verify(feedMapper, times(1)).toFeedListResponseDto(testFeed);
    }

    @Test
    @DisplayName("특정 타입 피드 목록 조회 - 성공")
    void getFeeds_WithFeedType_Success() {
        // given
        List<Feed> feeds = List.of(testFeed);
        Page<Feed> feedPage = new PageImpl<>(feeds, testPageable, 1);
        
        when(feedRepository.findByFeedType(FeedType.DAILY.name(), testPageable)).thenReturn(feedPage);
        when(feedMapper.toFeedListResponseDto(any(Feed.class))).thenReturn(testFeedDto);

        // when
        Page<FeedListResponseDto> result = feedReadService.getFeeds(FeedType.DAILY, testPageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFeedType()).isEqualTo(FeedType.DAILY);

        verify(feedRepository, times(1)).findByFeedType(FeedType.DAILY.name(), testPageable);
        verify(feedMapper, times(1)).toFeedListResponseDto(testFeed);
    }

    @Test
    @DisplayName("피드 타입별 조회 - 성공")
    void getFeedsByType_Success() {
        // given
        List<Feed> feeds = List.of(testFeed);
        Page<Feed> feedPage = new PageImpl<>(feeds, testPageable, 1);
        
        when(feedRepository.findByFeedType(FeedType.EVENT.name(), testPageable)).thenReturn(feedPage);
        when(feedMapper.toFeedListResponseDto(any(Feed.class))).thenReturn(testFeedDto);

        // when
        Page<FeedListResponseDto> result = feedReadService.getFeedsByType(FeedType.EVENT, testPageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(feedRepository, times(1)).findByFeedType(FeedType.EVENT.name(), testPageable);
        verify(feedMapper, times(1)).toFeedListResponseDto(testFeed);
    }

    @Test
    @DisplayName("빈 피드 목록 조회 - 성공")
    void getFeeds_EmptyList_Success() {
        // given
        Page<Feed> emptyPage = new PageImpl<>(List.of(), testPageable, 0);
        
        when(feedRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // when
        Page<FeedListResponseDto> result = feedReadService.getFeeds(null, testPageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(feedRepository, times(1)).findAll(testPageable);
        verify(feedMapper, never()).toFeedListResponseDto(any(Feed.class));
    }

    @Test
    @DisplayName("여러 피드 목록 조회 - 성공")
    void getFeeds_MultipleFeeds_Success() {
        // given
        Feed feed1 = Feed.builder().title("피드1").build();
        Feed feed2 = Feed.builder().title("피드2").build();
        List<Feed> feeds = List.of(feed1, feed2);
        Page<Feed> feedPage = new PageImpl<>(feeds, testPageable, 2);
        
        FeedListResponseDto dto1 = FeedListResponseDto.builder().feedId(1L).title("피드1").build();
        FeedListResponseDto dto2 = FeedListResponseDto.builder().feedId(2L).title("피드2").build();
        
        when(feedRepository.findAll(any(Pageable.class))).thenReturn(feedPage);
        when(feedMapper.toFeedListResponseDto(feed1)).thenReturn(dto1);
        when(feedMapper.toFeedListResponseDto(feed2)).thenReturn(dto2);

        // when
        Page<FeedListResponseDto> result = feedReadService.getFeeds(null, testPageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("피드1");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("피드2");

        verify(feedRepository, times(1)).findAll(testPageable);
        verify(feedMapper, times(2)).toFeedListResponseDto(any(Feed.class));
    }

    @Test
    @DisplayName("피드 타입별 조회 - 여러 피드")
    void getFeedsByType_MultipleFeeds_Success() {
        // given
        Feed feed1 = Feed.builder().title("이벤트 피드1").build();
        Feed feed2 = Feed.builder().title("이벤트 피드2").build();
        List<Feed> feeds = List.of(feed1, feed2);
        Page<Feed> feedPage = new PageImpl<>(feeds, testPageable, 2);
        
        FeedListResponseDto dto1 = FeedListResponseDto.builder().feedId(1L).title("이벤트 피드1").feedType(FeedType.EVENT).build();
        FeedListResponseDto dto2 = FeedListResponseDto.builder().feedId(2L).title("이벤트 피드2").feedType(FeedType.EVENT).build();
        
        when(feedRepository.findByFeedType(FeedType.EVENT.name(), testPageable)).thenReturn(feedPage);
        when(feedMapper.toFeedListResponseDto(feed1)).thenReturn(dto1);
        when(feedMapper.toFeedListResponseDto(feed2)).thenReturn(dto2);

        // when
        Page<FeedListResponseDto> result = feedReadService.getFeedsByType(FeedType.EVENT, testPageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getFeedType()).isEqualTo(FeedType.EVENT);
        assertThat(result.getContent().get(1).getFeedType()).isEqualTo(FeedType.EVENT);

        verify(feedRepository, times(1)).findByFeedType(FeedType.EVENT.name(), testPageable);
        verify(feedMapper, times(2)).toFeedListResponseDto(any(Feed.class));
    }

    @Test
    @DisplayName("페이징 정보 확인 - 성공")
    void getFeeds_PagingInfo_Success() {
        // given
        Pageable customPageable = PageRequest.of(1, 5);
        List<Feed> feeds = List.of(testFeed);
        Page<Feed> feedPage = new PageImpl<>(feeds, customPageable, 1);
        
        when(feedRepository.findAll(customPageable)).thenReturn(feedPage);
        when(feedMapper.toFeedListResponseDto(any(Feed.class))).thenReturn(testFeedDto);

        // when
        Page<FeedListResponseDto> result = feedReadService.getFeeds(null, customPageable, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(6);

        verify(feedRepository, times(1)).findAll(customPageable);
    }
} 