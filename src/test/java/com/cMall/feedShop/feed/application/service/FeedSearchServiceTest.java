package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.request.FeedSearchRequest;
import com.cMall.feedShop.feed.application.dto.response.FeedListResponseDto;
import com.cMall.feedShop.feed.application.service.FeedLikeService;
import com.cMall.feedShop.feed.application.service.FeedServiceUtils;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.feed.application.service.FeedMapper;
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
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedSearchService 테스트")
class FeedSearchServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private FeedLikeService feedLikeService;

    @Mock
    private FeedServiceUtils feedServiceUtils;

    @Mock
    private UserDetails userDetails;

    @Mock
    private Feed testFeed;

    @Mock
    private FeedListResponseDto testResponseDto;

    @InjectMocks
    private FeedSearchService feedSearchService;

    @BeforeEach
    void setUp() {
        // 기본 테스트 데이터 설정
        when(testFeed.getId()).thenReturn(1L);
        when(testFeed.getTitle()).thenReturn("테스트 피드");
        when(testFeed.getContent()).thenReturn("테스트 내용");
        when(testFeed.getFeedType()).thenReturn(FeedType.DAILY);
        when(testFeed.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(testResponseDto.getFeedId()).thenReturn(1L);
        when(testResponseDto.getTitle()).thenReturn("테스트 피드");
        when(testResponseDto.getContent()).thenReturn("테스트 내용");
        when(testResponseDto.getFeedType()).thenReturn(FeedType.DAILY);
    }

    @Test
    @DisplayName("키워드 검색 성공")
    void searchFeeds_WithKeyword_Success() {
        // given
        FeedSearchRequest request = FeedSearchRequest.builder()
                .keyword("테스트")
                .page(0)
                .size(20)
                .build();

        Page<Feed> feedPage = new PageImpl<>(List.of(testFeed), PageRequest.of(0, 20), 1);
        when(feedRepository.findWithSearchConditions(any(FeedSearchRequest.class), any(Pageable.class)))
                .thenReturn(feedPage);
        when(feedMapper.toFeedListResponseDto(testFeed)).thenReturn(testResponseDto);

        // when
        PaginatedResponse<FeedListResponseDto> result = feedSearchService.searchFeeds(request, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 피드");
    }

    @Test
    @DisplayName("작성자별 검색 성공")
    void searchFeeds_WithAuthorId_Success() {
        // given
        FeedSearchRequest request = FeedSearchRequest.builder()
                .authorId(1L)
                .page(0)
                .size(20)
                .build();

        Page<Feed> feedPage = new PageImpl<>(List.of(testFeed), PageRequest.of(0, 20), 1);
        when(feedRepository.findWithSearchConditions(any(FeedSearchRequest.class), any(Pageable.class)))
                .thenReturn(feedPage);
        when(feedMapper.toFeedListResponseDto(testFeed)).thenReturn(testResponseDto);

        // when
        PaginatedResponse<FeedListResponseDto> result = feedSearchService.searchFeeds(request, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("피드 타입별 검색 성공")
    void searchFeeds_WithFeedType_Success() {
        // given
        FeedSearchRequest request = FeedSearchRequest.builder()
                .feedType(FeedType.EVENT)
                .page(0)
                .size(20)
                .build();

        Page<Feed> feedPage = new PageImpl<>(List.of(testFeed), PageRequest.of(0, 20), 1);
        when(feedRepository.findWithSearchConditions(any(FeedSearchRequest.class), any(Pageable.class)))
                .thenReturn(feedPage);
        when(feedMapper.toFeedListResponseDto(testFeed)).thenReturn(testResponseDto);

        // when
        PaginatedResponse<FeedListResponseDto> result = feedSearchService.searchFeeds(request, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("로그인한 사용자 검색 시 좋아요 상태 포함")
    void searchFeeds_WithUserDetails_IncludeLikeStatus() {
        // given
        FeedSearchRequest request = FeedSearchRequest.builder()
                .keyword("테스트")
                .page(0)
                .size(20)
                .build();

        Page<Feed> feedPage = new PageImpl<>(List.of(testFeed), PageRequest.of(0, 20), 1);
        when(feedRepository.findWithSearchConditions(any(FeedSearchRequest.class), any(Pageable.class)))
                .thenReturn(feedPage);
        when(feedMapper.toFeedListResponseDto(testFeed)).thenReturn(testResponseDto);
        when(feedServiceUtils.getUserIdFromUserDetails(userDetails)).thenReturn(1L);
        when(feedLikeService.isLikedByUser(anyLong(), anyLong())).thenReturn(true);

        // when
        PaginatedResponse<FeedListResponseDto> result = feedSearchService.searchFeeds(request, userDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("페이징 처리 성공")
    void searchFeeds_WithPaging_Success() {
        // given
        FeedSearchRequest request = FeedSearchRequest.builder()
                .page(1)
                .size(6)
                .build();

        Page<Feed> feedPage = new PageImpl<>(List.of(testFeed), PageRequest.of(1, 6), 25);
        when(feedRepository.findWithSearchConditions(any(FeedSearchRequest.class), any(Pageable.class)))
                .thenReturn(feedPage);
        when(feedMapper.toFeedListResponseDto(testFeed)).thenReturn(testResponseDto);

        // when
        PaginatedResponse<FeedListResponseDto> result = feedSearchService.searchFeeds(request, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(6);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.isHasPrevious()).isTrue();
    }

    @Test
    @DisplayName("빈 검색 결과 처리")
    void searchFeeds_EmptyResult_Success() {
        // given
        FeedSearchRequest request = FeedSearchRequest.builder()
                .keyword("존재하지않는키워드")
                .page(0)
                .size(20)
                .build();

        Page<Feed> feedPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(feedRepository.findWithSearchConditions(any(FeedSearchRequest.class), any(Pageable.class)))
                .thenReturn(feedPage);

        // when
        PaginatedResponse<FeedListResponseDto> result = feedSearchService.searchFeeds(request, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }
}
