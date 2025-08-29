package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.feed.application.dto.response.FeedVoteResponseDto;
import com.cMall.feedShop.feed.application.service.FeedVoteService;
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
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedVoteController 테스트")
class FeedVoteControllerTest {

    @Mock
    private FeedVoteService feedVoteService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedVoteController feedVoteController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserDetails userDetails;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .loginId("testuser")
                .email("test@example.com")
                .build();
        // ID는 리플렉션으로 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user ID", e);
        }

        // 테스트용 UserDetails 생성
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // Custom ArgumentResolver for @AuthenticationPrincipal
        HandlerMethodArgumentResolver authenticationPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(org.springframework.security.core.annotation.AuthenticationPrincipal.class) &&
                       UserDetails.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return userDetails;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(feedVoteController)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("피드 투표 성공")
    void voteFeed_success() throws Exception {
        // given
        Long feedId = 1L;
        FeedVoteResponseDto responseDto = FeedVoteResponseDto.builder()
                .voted(true)
                .voteCount(1)
                .message("투표가 완료되었습니다!")
                .build();

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(feedVoteService.voteFeed(feedId, testUser.getId())).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/vote", feedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.voted").value(true))
                .andExpect(jsonPath("$.data.voteCount").value(1))
                .andExpect(jsonPath("$.data.message").value("투표가 완료되었습니다!"));
    }

    @Test
    @DisplayName("피드 투표 실패 - 사용자 정보를 찾을 수 없음")
    void voteFeed_userNotFound() throws Exception {
        // given
        Long feedId = 1L;
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testuser")).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/vote", feedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("사용자 정보를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("피드 투표 취소 성공")
    void cancelVote_success() throws Exception {
        // given
        Long feedId = 1L;
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));

        // when & then
        mockMvc.perform(delete("/api/feeds/{feedId}/vote", feedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("투표가 취소되었습니다."));
    }

    @Test
    @DisplayName("투표 여부 확인 성공 - 투표함")
    void hasVoted_true() throws Exception {
        // given
        Long feedId = 1L;
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(feedVoteService.hasVoted(feedId, testUser.getId())).thenReturn(true);

        // when & then
        mockMvc.perform(get("/api/feeds/{feedId}/vote/check", feedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("투표 여부 확인 성공 - 투표하지 않음")
    void hasVoted_false() throws Exception {
        // given
        Long feedId = 1L;
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(testUser));
        when(feedVoteService.hasVoted(feedId, testUser.getId())).thenReturn(false);

        // when & then
        mockMvc.perform(get("/api/feeds/{feedId}/vote/check", feedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("투표 여부 확인 - 인증되지 않은 사용자")
    void hasVoted_unauthenticated() throws Exception {
        // given
        Long feedId = 1L;

        // Custom ArgumentResolver for null UserDetails
        HandlerMethodArgumentResolver nullUserDetailsResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(org.springframework.security.core.annotation.AuthenticationPrincipal.class) &&
                       UserDetails.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return null;
            }
        };

        MockMvc mockMvcWithNullUser = MockMvcBuilders.standaloneSetup(feedVoteController)
                .setCustomArgumentResolvers(nullUserDetailsResolver)
                .build();

        // when & then
        mockMvcWithNullUser.perform(get("/api/feeds/{feedId}/vote/check", feedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("투표 개수 조회 성공")
    void getVoteCount_success() throws Exception {
        // given
        Long feedId = 1L;
        when(feedVoteService.getVoteCount(feedId)).thenReturn(5L);

        // when & then
        mockMvc.perform(get("/api/feeds/{feedId}/vote/count", feedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("이벤트 투표 개수 조회 성공")
    void getEventVoteCount_success() throws Exception {
        // given
        Long eventId = 1L;
        when(feedVoteService.getEventVoteCount(eventId)).thenReturn(10L);

        // when & then
        mockMvc.perform(get("/api/feeds/events/{eventId}/vote/count", eventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(10));
    }
}
