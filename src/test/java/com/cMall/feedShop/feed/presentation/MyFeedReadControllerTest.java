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

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

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
        // Create UserDetails instance
        userDetails = org.springframework.security.core.userdetails.User.withUsername("testuser")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // Create custom ArgumentResolver for @AuthenticationPrincipal
        HandlerMethodArgumentResolver authenticationPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class) &&
                       UserDetails.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return userDetails;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(myFeedReadController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();
        
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
        

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(myFeedReadService.getMyFeeds(eq(1L), any(Pageable.class), eq(userDetails))).thenReturn(feedPage);

        // when & then
        mockMvc.perform(get("/api/feeds/my")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].feedId").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("테스트 피드"));

        verify(myFeedReadService, times(1)).getMyFeeds(eq(1L), any(Pageable.class), eq(userDetails));
    }

    @Test
    @DisplayName("마이피드 타입별 조회 - 성공")
    void getMyFeedsByType_Success() throws Exception {
        // given
        List<FeedListResponseDto> feeds = List.of(testFeedDto);
        Page<FeedListResponseDto> feedPage = new PageImpl<>(feeds, testPageable, 1);
        

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(myFeedReadService.getMyFeedsByType(eq(1L), eq(FeedType.EVENT), any(Pageable.class), eq(userDetails))).thenReturn(feedPage);

        // when & then
        mockMvc.perform(get("/api/feeds/my/type/EVENT")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].feedId").value(1));

        verify(myFeedReadService, times(1)).getMyFeedsByType(eq(1L), eq(FeedType.EVENT), any(Pageable.class), eq(userDetails));
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
        

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(myFeedReadService.getMyFeedCounts(eq(1L))).thenReturn(counts);

        // when & then
        mockMvc.perform(get("/api/feeds/my/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.dailyCount").value(5))
                .andExpect(jsonPath("$.data.eventCount").value(3))
                .andExpect(jsonPath("$.data.rankingCount").value(2));

        verify(myFeedReadService, times(1)).getMyFeedCounts(eq(1L));
    }

    @Test
    @DisplayName("마이피드 타입별 개수 조회 - 성공")
    void getMyFeedCountByType_Success() throws Exception {
        // given

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(myFeedReadService.getMyFeedCountByType(eq(1L), eq(FeedType.DAILY))).thenReturn(5L);

        // when & then
        mockMvc.perform(get("/api/feeds/my/count/type/DAILY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));

        verify(myFeedReadService, times(1)).getMyFeedCountByType(eq(1L), eq(FeedType.DAILY));
    }

    @Test
    @DisplayName("잘못된 피드 타입 - 에러 응답")
    void getMyFeeds_InvalidFeedType_ReturnsError() throws Exception {
        // given

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));

        // when & then
        mockMvc.perform(get("/api/feeds/my")
                        .param("feedType", "INVALID")
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
        

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(myFeedReadService.getMyFeeds(eq(1L), any(Pageable.class), eq(userDetails))).thenReturn(feedPage);

        // when & then
        mockMvc.perform(get("/api/feeds/my")
                        .param("sort", "popular")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(myFeedReadService, times(1)).getMyFeeds(eq(1L), any(Pageable.class), eq(userDetails));
    }
}
