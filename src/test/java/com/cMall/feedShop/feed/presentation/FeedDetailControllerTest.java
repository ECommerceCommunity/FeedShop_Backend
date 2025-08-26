package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.service.FeedDetailService;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedDetailController 테스트")
class FeedDetailControllerTest {

    @Mock
    private FeedDetailService feedDetailService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private FeedDetailController feedDetailController;

    private MockMvc mockMvc;
    private User testUser;
    private FeedDetailResponseDto testFeedDetail;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(feedDetailController)
                
                .build();

        // 테스트용 사용자 생성
        testUser = User.builder()
                .loginId("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        // UserDetails 모킹 설정
        org.mockito.BDDMockito.given(userDetails.getUsername()).willReturn("testuser");

        // 테스트용 피드 상세 정보 생성
        testFeedDetail = FeedDetailResponseDto.builder()
                .feedId(1L)
                .title("테스트 피드 제목")
                .content("테스트 피드 내용입니다.")
                .userNickname("테스트 사용자")
                .userId(1L)
                .likeCount(15)
                .commentCount(8)
                .participantVoteCount(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .feedType(FeedType.DAILY)
                .isLiked(true)
                .isVoted(false)
                .build();
    }

@Test
    @DisplayName("피드 상세 조회 - 성공")
    void getFeedDetail_Success() throws Exception {
        // given
        given(feedDetailService.getFeedDetail(eq(1L), eq(userDetails)))
                .willReturn(testFeedDetail);

        // when & then
        mockMvc.perform(get("/api/feeds/1")
                        .with(request -> {
                            // request.setUserPrincipal(() -> userDetails.getUsername());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(1L))
                .andExpect(jsonPath("$.data.title").value("테스트 피드 제목"))
                .andExpect(jsonPath("$.data.content").value("테스트 피드 내용입니다."))
                .andExpect(jsonPath("$.data.authorName").value("테스트 사용자"))
                .andExpect(jsonPath("$.data.authorId").value(1L))
                .andExpect(jsonPath("$.data.likeCount").value(15))
                .andExpect(jsonPath("$.data.commentCount").value(8))
                .andExpect(jsonPath("$.data.voteCount").value(5))
                .andExpect(jsonPath("$.data.feedType").value("DAILY"))
                .andExpect(jsonPath("$.data.isLiked").value(true))
                .andExpect(jsonPath("$.data.isVoted").value(false))
                .andExpect(jsonPath("$.data.hashtags").isArray())
                .andExpect(jsonPath("$.data.images").isArray());

        verify(feedDetailService).getFeedDetail(eq(1L), eq(userDetails));
    }

@Test
    @DisplayName("피드 상세 조회 - 인증되지 않은 사용자")
    void getFeedDetail_Unauthenticated() throws Exception {
        // given
        given(feedDetailService.getFeedDetail(eq(1L), eq(null)))
                .willReturn(testFeedDetail);

        // when & then
        mockMvc.perform(get("/api/feeds/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(1L));

        verify(feedDetailService).getFeedDetail(eq(1L), eq(null));
    }

@Test
    @DisplayName("다른 피드 ID로 상세 조회 - 성공")
    void getFeedDetail_DifferentFeedId_Success() throws Exception {
        // given
        FeedDetailResponseDto differentFeed = FeedDetailResponseDto.builder()
                .feedId(999L)
                .title("다른 피드")
                .content("다른 피드 내용")
                .userNickname("다른 사용자")
                .userId(999L)
                .likeCount(5)
                .commentCount(2)
                .participantVoteCount(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .feedType(FeedType.EVENT)
                .isLiked(false)
                .isVoted(true)
                .build();

        given(feedDetailService.getFeedDetail(eq(999L), eq(userDetails)))
                .willReturn(differentFeed);

        // when & then
        mockMvc.perform(get("/api/feeds/999")
                        .with(request -> {
                            // request.setUserPrincipal(() -> userDetails.getUsername());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(999L))
                .andExpect(jsonPath("$.data.title").value("다른 피드"))
                .andExpect(jsonPath("$.data.feedType").value("EVENT"));

        verify(feedDetailService).getFeedDetail(eq(999L), eq(userDetails));
    }

@Test
    @DisplayName("이벤트 피드 상세 조회 - 성공")
    void getFeedDetail_EventFeed_Success() throws Exception {
        // given
        FeedDetailResponseDto eventFeed = FeedDetailResponseDto.builder()
                .feedId(2L)
                .title("이벤트 피드")
                .content("이벤트 피드 내용")
                .userNickname("이벤트 사용자")
                .userId(2L)
                .likeCount(25)
                .commentCount(12)
                .participantVoteCount(8)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .feedType(FeedType.EVENT)
                .isLiked(false)
                .isVoted(true)
                .build();

        given(feedDetailService.getFeedDetail(eq(2L), eq(userDetails)))
                .willReturn(eventFeed);

        // when & then
        mockMvc.perform(get("/api/feeds/2")
                        .with(request -> {
                            // request.setUserPrincipal(() -> userDetails.getUsername());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(2L))
                .andExpect(jsonPath("$.data.title").value("이벤트 피드"))
                .andExpect(jsonPath("$.data.feedType").value("EVENT"))
                .andExpect(jsonPath("$.data.isVoted").value(true));

        verify(feedDetailService).getFeedDetail(eq(2L), eq(userDetails));
    }

@Test
    @DisplayName("랭킹 피드 상세 조회 - 성공")
    void getFeedDetail_RankingFeed_Success() throws Exception {
        // given
        FeedDetailResponseDto rankingFeed = FeedDetailResponseDto.builder()
                .feedId(3L)
                .title("랭킹 피드")
                .content("랭킹 피드 내용")
                .userNickname("랭킹 사용자")
                .userId(3L)
                .likeCount(50)
                .commentCount(20)
                .participantVoteCount(15)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .feedType(FeedType.RANKING)
                .isLiked(true)
                .isVoted(false)
                .build();

        given(feedDetailService.getFeedDetail(eq(3L), eq(userDetails)))
                .willReturn(rankingFeed);

        // when & then
        mockMvc.perform(get("/api/feeds/3")
                        .with(request -> {
                            // request.setUserPrincipal(() -> userDetails.getUsername());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(3L))
                .andExpect(jsonPath("$.data.title").value("랭킹 피드"))
                .andExpect(jsonPath("$.data.feedType").value("RANKING"))
                .andExpect(jsonPath("$.data.likeCount").value(50));

        verify(feedDetailService).getFeedDetail(eq(3L), eq(userDetails));
    }

@Test
    @DisplayName("긴 피드 ID로 상세 조회 - 성공")
    void getFeedDetail_LongFeedId_Success() throws Exception {
        // given
        Long longFeedId = 123456789L;
        given(feedDetailService.getFeedDetail(eq(longFeedId), eq(userDetails)))
                .willReturn(testFeedDetail);

        // when & then
        mockMvc.perform(get("/api/feeds/" + longFeedId)
                        .with(request -> {
                            // request.setUserPrincipal(() -> userDetails.getUsername());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedDetailService).getFeedDetail(eq(longFeedId), eq(userDetails));
    }

@Test
    @DisplayName("0 피드 ID로 상세 조회 - 성공")
    void getFeedDetail_ZeroFeedId_Success() throws Exception {
        // given
        given(feedDetailService.getFeedDetail(eq(0L), eq(userDetails)))
                .willReturn(testFeedDetail);

        // when & then
        mockMvc.perform(get("/api/feeds/0")
                        .with(request -> {
                            // request.setUserPrincipal(() -> userDetails.getUsername());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedDetailService).getFeedDetail(eq(0L), eq(userDetails));
    }
}
