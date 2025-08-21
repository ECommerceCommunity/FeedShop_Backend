package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.response.FeedListResponseDto;
import com.cMall.feedShop.feed.application.dto.response.MyFeedCountResponse;
import com.cMall.feedShop.feed.application.service.MyFeedReadService;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MyFeedReadController 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class MyFeedReadControllerTest {

    @Mock
    private MyFeedReadService myFeedReadService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private MyFeedReadController myFeedReadController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;
    private FeedListResponseDto testFeedDto;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(myFeedReadController).build();
        objectMapper = new ObjectMapper();
        
        testPageable = PageRequest.of(0, 10);
        
        testUser = new User(1L, "testuser", "password", "test@test.com", com.cMall.feedShop.user.domain.enums.UserRole.USER);

        testFeedDto = FeedListResponseDto.builder()
                .feedId(1L)
                .title("테스트 피드")
                .content("테스트 내용")
                .feedType(FeedType.DAILY)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("마이피드 목록 조회 - 성공")
    void getMyFeeds_Success() throws Exception {
        // given
        List<FeedListResponseDto> feeds = List.of(testFeedDto);
        Page<FeedListResponseDto> feedPage = new PageImpl<>(feeds, testPageable, 1);
        
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(myFeedReadService.getMyFeeds(1L, any(Pageable.class), userDetails)).thenReturn(feedPage);

        // when & then
        mockMvc.perform(get("/api/feeds/my")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "latest")
                        .requestAttr("userDetails", userDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].feedId").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("테스트 피드"));

        verify(myFeedReadService, times(1)).getMyFeeds(1L, any(Pageable.class), userDetails);
    }

    @Test
    @DisplayName("마이피드 타입별 조회 - 성공")
    void getMyFeedsByType_Success() throws Exception {
        // given
        List<FeedListResponseDto> feeds = List.of(testFeedDto);
        Page<FeedListResponseDto> feedPage = new PageImpl<>(feeds, testPageable, 1);
        
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(myFeedReadService.getMyFeedsByType(1L, FeedType.EVENT, any(Pageable.class), userDetails)).thenReturn(feedPage);

        // when & then
        mockMvc.perform(get("/api/feeds/my/type/EVENT")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "latest")
                        .requestAttr("userDetails", userDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].feedId").value(1));

        verify(myFeedReadService, times(1)).getMyFeedsByType(1L, FeedType.EVENT, any(Pageable.class), userDetails);
    }

    @Test
    @DisplayName("마이피드 개수 조회 - 성공")
    void getMyFeedCounts_Success() throws Exception {
        // given
        MyFeedCountResponse counts = MyFeedCountResponse.builder()
                .totalCount(10L)
                .dailyCount(5L)
                .eventCount(3L)
                .rankingCount(2L)
                .build();
        
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(myFeedReadService.getMyFeedCounts(1L)).thenReturn(counts);

        // when & then
        mockMvc.perform(get("/api/feeds/my/count")
                        .requestAttr("userDetails", userDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.dailyCount").value(5))
                .andExpect(jsonPath("$.data.eventCount").value(3))
                .andExpect(jsonPath("$.data.rankingCount").value(2));

        verify(myFeedReadService, times(1)).getMyFeedCounts(1L);
    }

    @Test
    @DisplayName("마이피드 타입별 개수 조회 - 성공")
    void getMyFeedCountByType_Success() throws Exception {
        // given
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(myFeedReadService.getMyFeedCountByType(1L, FeedType.DAILY)).thenReturn(5L);

        // when & then
        mockMvc.perform(get("/api/feeds/my/count/type/DAILY")
                        .requestAttr("userDetails", userDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));

        verify(myFeedReadService, times(1)).getMyFeedCountByType(1L, FeedType.DAILY);
    }

    @Test
    @DisplayName("잘못된 피드 타입 - 에러 응답")
    void getMyFeeds_InvalidFeedType_ReturnsError() throws Exception {
        // given
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));

        // when & then
        mockMvc.perform(get("/api/feeds/my")
                        .param("feedType", "INVALID")
                        .requestAttr("userDetails", userDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("잘못된 피드 타입입니다. (DAILY, EVENT, RANKING)"));

        verify(myFeedReadService, never()).getMyFeeds(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 정보 없음 - 에러 응답")
    void getMyFeeds_NoUserDetails_ReturnsError() throws Exception {
        // when & then
        mockMvc.perform(get("/api/feeds/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("사용자 정보를 찾을 수 없습니다."));

        verify(myFeedReadService, never()).getMyFeeds(any(), any(), any());
    }

    @Test
    @DisplayName("인기순 정렬 - 성공")
    void getMyFeeds_PopularSort_Success() throws Exception {
        // given
        List<FeedListResponseDto> feeds = List.of(testFeedDto);
        Page<FeedListResponseDto> feedPage = new PageImpl<>(feeds, testPageable, 1);
        
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(myFeedReadService.getMyFeeds(1L, any(Pageable.class), userDetails)).thenReturn(feedPage);

        // when & then
        mockMvc.perform(get("/api/feeds/my")
                        .param("sort", "popular")
                        .requestAttr("userDetails", userDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(myFeedReadService, times(1)).getMyFeeds(1L, any(Pageable.class), userDetails);
    }
}
