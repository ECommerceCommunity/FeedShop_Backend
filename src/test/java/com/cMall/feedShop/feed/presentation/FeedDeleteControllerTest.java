package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.feed.application.service.FeedDeleteService;
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

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedDeleteController 테스트")
class FeedDeleteControllerTest {

    @Mock
    private FeedDeleteService feedDeleteService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private FeedDeleteController feedDeleteController;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(feedDeleteController)
                
                .build();

        // 테스트용 사용자 생성
        testUser = User.builder()
                .loginId("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .build();

        // UserDetails 모킹 설정
        org.mockito.BDDMockito.given(userDetails.getUsername()).willReturn("testuser");
    }

@Test
    @DisplayName("피드 삭제 - 성공")
    void deleteFeed_Success() throws Exception {
        // given
        doNothing().when(feedDeleteService).deleteFeed(eq(1L), eq(userDetails));

        // when & then
        mockMvc.perform(delete("/api/feeds/1")
                        .with(request -> {
                            // request.setUserPrincipal(() -> userDetails.getUsername());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(feedDeleteService).deleteFeed(eq(1L), eq(userDetails));
    }

@Test
    @DisplayName("피드 삭제 - 인증되지 않은 사용자")
    void deleteFeed_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/feeds/1"))
                .andExpect(status().isUnauthorized());
    }

@Test
    @DisplayName("다른 피드 ID로 삭제 - 성공")
    void deleteFeed_DifferentFeedId_Success() throws Exception {
        // given
        doNothing().when(feedDeleteService).deleteFeed(eq(999L), eq(userDetails));

        // when & then
        mockMvc.perform(delete("/api/feeds/999")
                        .with(request -> {
                            // request.setUserPrincipal(() -> userDetails.getUsername());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedDeleteService).deleteFeed(eq(999L), eq(userDetails));
    }

@Test
    @DisplayName("긴 피드 ID로 삭제 - 성공")
    void deleteFeed_LongFeedId_Success() throws Exception {
        // given
        Long longFeedId = 123456789L;
        doNothing().when(feedDeleteService).deleteFeed(eq(longFeedId), eq(userDetails));

        // when & then
        mockMvc.perform(delete("/api/feeds/" + longFeedId)
                        .with(request -> {
                            // request.setUserPrincipal(() -> userDetails.getUsername());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedDeleteService).deleteFeed(eq(longFeedId), eq(userDetails));
    }

@Test
    @DisplayName("0 피드 ID로 삭제 - 성공")
    void deleteFeed_ZeroFeedId_Success() throws Exception {
        // given
        doNothing().when(feedDeleteService).deleteFeed(eq(0L), eq(userDetails));

        // when & then
        mockMvc.perform(delete("/api/feeds/0")
                        .with(request -> {
                            // request.setUserPrincipal(() -> userDetails.getUsername());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(feedDeleteService).deleteFeed(eq(0L), eq(userDetails));
    }
}
