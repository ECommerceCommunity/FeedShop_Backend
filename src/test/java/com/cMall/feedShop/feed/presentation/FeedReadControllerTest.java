package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.response.FeedListResponseDto;
import com.cMall.feedShop.feed.application.service.FeedReadService;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.enums.UserRole;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedReadController 테스트")
class FeedReadControllerTest {

    @Mock
    private FeedReadService feedReadService;

    @InjectMocks
    private FeedReadController feedReadController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private List<FeedListResponseDto> testFeeds;
    private Page<FeedListResponseDto> testFeedPage;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(feedReadController)
                .build();
        objectMapper = new ObjectMapper();

        // 테스트용 사용자 생성
        testUser = User.builder()
                .loginId("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        // 테스트용 피드 목록 생성
        testFeeds = Arrays.asList(
                FeedListResponseDto.builder()
                        .feedId(1L)
                        .title("첫 번째 피드")
                        .content("첫 번째 피드 내용")
                        .userNickname("테스트 사용자")
                        .likeCount(10)
                        .commentCount(5)
                        .participantVoteCount(3)
                        .createdAt(LocalDateTime.now())
                        .feedType(FeedType.DAILY)
                        .isLiked(false)
                        .isVoted(false)
                        .build(),
                FeedListResponseDto.builder()
                        .feedId(2L)
                        .title("두 번째 피드")
                        .content("두 번째 피드 내용")
                        .userNickname("테스트 사용자")
                        .likeCount(15)
                        .commentCount(8)
                        .participantVoteCount(5)
                        .createdAt(LocalDateTime.now().minusHours(1))
                        .feedType(FeedType.EVENT)
                        .isLiked(true)
                        .isVoted(false)
                        .build()
        );

        testFeedPage = new PageImpl<>(testFeeds, PageRequest.of(0, 20), 2);
    }

    @Test
    @DisplayName("피드 전체 목록 조회 - 성공")
    void getFeeds_Success() throws Exception {
        // given
        given(feedReadService.getFeeds(eq(null), any(Pageable.class), eq(null)))
                .willReturn(testFeedPage);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].feedId").value(1L))
                .andExpect(jsonPath("$.data.content[0].title").value("첫 번째 피드"))
                .andExpect(jsonPath("$.data.content[1].feedId").value(2L))
                .andExpect(jsonPath("$.data.content[1].title").value("두 번째 피드"))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1));

        verify(feedReadService).getFeeds(eq(null), any(Pageable.class), eq(null));
    }

    @Test
    @DisplayName("피드 타입별 조회 - DAILY")
    void getFeeds_ByFeedType_DAILY() throws Exception {
        // given
        given(feedReadService.getFeeds(eq(FeedType.DAILY), any(Pageable.class), eq(null)))
                .willReturn(testFeedPage);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("feedType", "DAILY")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(feedReadService).getFeeds(eq(FeedType.DAILY), any(Pageable.class), eq(null));
    }

    @Test
    @DisplayName("피드 타입별 조회 - EVENT")
    void getFeeds_ByFeedType_EVENT() throws Exception {
        // given
        given(feedReadService.getFeeds(eq(FeedType.EVENT), any(Pageable.class), eq(null)))
                .willReturn(testFeedPage);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("feedType", "EVENT")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedReadService).getFeeds(eq(FeedType.EVENT), any(Pageable.class), eq(null));
    }

    @Test
    @DisplayName("피드 타입별 조회 - RANKING")
    void getFeeds_ByFeedType_RANKING() throws Exception {
        // given
        given(feedReadService.getFeeds(eq(FeedType.RANKING), any(Pageable.class), eq(null)))
                .willReturn(testFeedPage);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("feedType", "RANKING")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedReadService).getFeeds(eq(FeedType.RANKING), any(Pageable.class), eq(null));
    }

    @Test
    @DisplayName("잘못된 피드 타입 - 에러 응답")
    void getFeeds_InvalidFeedType() throws Exception {
        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("feedType", "INVALID_TYPE")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("잘못된 피드 타입입니다. (DAILY, EVENT, RANKING)"));
    }

    @Test
    @DisplayName("인기순 정렬로 피드 조회")
    void getFeeds_SortByPopular() throws Exception {
        // given
        given(feedReadService.getFeeds(eq(null), any(Pageable.class), eq(null)))
                .willReturn(testFeedPage);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("sort", "popular")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedReadService).getFeeds(eq(null), any(Pageable.class), eq(null));
    }

    @Test
    @DisplayName("페이징 처리 - 두 번째 페이지")
    void getFeeds_SecondPage() throws Exception {
        // given
        Page<FeedListResponseDto> secondPage = new PageImpl<>(testFeeds, PageRequest.of(1, 20), 40);
        given(feedReadService.getFeeds(eq(null), any(Pageable.class), any(String.class)))
                .willReturn(secondPage);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(40));

        verify(feedReadService).getFeeds(eq(null), any(Pageable.class), eq(null));
    }

    @Test
    @DisplayName("빈 피드 목록 조회")
    void getFeeds_EmptyList() throws Exception {
        // given
        Page<FeedListResponseDto> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
        given(feedReadService.getFeeds(eq(null), any(Pageable.class), any(String.class)))
                .willReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(0));

        verify(feedReadService).getFeeds(eq(null), any(Pageable.class), eq(null));
    }

    @Test
    @DisplayName("인증되지 않은 사용자로 피드 조회")
    void getFeeds_Unauthenticated() throws Exception {
        // given
        given(feedReadService.getFeeds(eq(null), any(Pageable.class), eq(null)))
                .willReturn(testFeedPage);

        // when & then
        mockMvc.perform(get("/api/feeds")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedReadService).getFeeds(eq(null), any(Pageable.class), eq(null));
    }

    @Test
    @DisplayName("기본 파라미터로 피드 조회")
    void getFeeds_DefaultParameters() throws Exception {
        // given
        given(feedReadService.getFeeds(eq(null), any(Pageable.class), eq(null)))
                .willReturn(testFeedPage);

        // when & then
        mockMvc.perform(get("/api/feeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedReadService).getFeeds(eq(null), any(Pageable.class), eq(null));
    }
}
