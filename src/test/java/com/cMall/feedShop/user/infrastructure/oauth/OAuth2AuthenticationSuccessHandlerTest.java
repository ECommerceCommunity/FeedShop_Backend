package com.cMall.feedShop.user.infrastructure.oauth;

import com.cMall.feedShop.user.infrastructure.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Authentication authentication;
    private CustomOAuth2User customOAuth2User;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        
        // 리다이렉트 URI 설정
        ReflectionTestUtils.setField(successHandler, "redirectUri", "https://www.feedshop.store/auth/callback");

        // CustomOAuth2User 모킹
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("name", "테스트 사용자");
        attributes.put("email", "test@example.com");

        customOAuth2User = new CustomOAuth2User(
                null, // 실제 OAuth2User는 필요하지 않음
                "google",
                "123456789",
                "test@example.com",
                "테스트 사용자"
        );

        // Authentication 모킹
        authentication = mock(Authentication.class);
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 - JWT 토큰 생성 및 리다이렉트")
    void onAuthenticationSuccess_GeneratesTokenAndRedirects() throws Exception {
        // given
        when(authentication.getPrincipal()).thenReturn(customOAuth2User);
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        when(jwtTokenProvider.generateAccessToken("test@example.com", "ROLE_USER"))
                .thenReturn(expectedToken);

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertEquals(302, response.getStatus()); // 리다이렉트 상태 코드
        
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith("https://www.feedshop.store/auth/callback#"));
        assertTrue(redirectUrl.contains("token=" + expectedToken));
        assertTrue(redirectUrl.contains("email=test%40example.com"));
        assertTrue(redirectUrl.contains("name="));
        assertTrue(redirectUrl.contains("provider=google"));

        verify(jwtTokenProvider, times(1)).generateAccessToken("test@example.com", "ROLE_USER");
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 - 이름이 null인 경우")
    void onAuthenticationSuccess_WithNullName_HandlesGracefully() throws Exception {
        // given
        CustomOAuth2User userWithNullName = new CustomOAuth2User(
                null,
                "google",
                "123456789",
                "test@example.com",
                null
        );
        when(authentication.getPrincipal()).thenReturn(userWithNullName);

        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        when(jwtTokenProvider.generateAccessToken("test@example.com", "ROLE_USER"))
                .thenReturn(expectedToken);

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertEquals(302, response.getStatus());
        
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("name=")); // 빈 문자열로 인코딩됨

        verify(jwtTokenProvider, times(1)).generateAccessToken("test@example.com", "ROLE_USER");
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 - 카카오 제공자")
    void onAuthenticationSuccess_KakaoProvider_Success() throws Exception {
        // given
        CustomOAuth2User kakaoUser = new CustomOAuth2User(
                null,
                "kakao",
                "123456789",
                "kakao@example.com",
                "카카오 사용자"
        );
        when(authentication.getPrincipal()).thenReturn(kakaoUser);

        String expectedToken = "kakao_jwt_token";
        when(jwtTokenProvider.generateAccessToken("kakao@example.com", "ROLE_USER"))
                .thenReturn(expectedToken);

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertEquals(302, response.getStatus());
        
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("provider=kakao"));
        assertTrue(redirectUrl.contains("email=kakao%40example.com"));

        verify(jwtTokenProvider, times(1)).generateAccessToken("kakao@example.com", "ROLE_USER");
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 - 네이버 제공자")
    void onAuthenticationSuccess_NaverProvider_Success() throws Exception {
        // given
        CustomOAuth2User naverUser = new CustomOAuth2User(
                null,
                "naver",
                "123456789",
                "naver@example.com",
                "네이버 사용자"
        );
        when(authentication.getPrincipal()).thenReturn(naverUser);

        String expectedToken = "naver_jwt_token";
        when(jwtTokenProvider.generateAccessToken("naver@example.com", "ROLE_USER"))
                .thenReturn(expectedToken);

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertEquals(302, response.getStatus());
        
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("provider=naver"));
        assertTrue(redirectUrl.contains("email=naver%40example.com"));

        verify(jwtTokenProvider, times(1)).generateAccessToken("naver@example.com", "ROLE_USER");
    }

    @Test
    @DisplayName("응답이 이미 커밋된 경우 - 리다이렉트 수행하지 않음")
    void onAuthenticationSuccess_ResponseCommitted_NoRedirect() throws Exception {
        // given
        response.setCommitted(true);

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertNull(response.getRedirectedUrl());
        verify(jwtTokenProvider, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    @DisplayName("JWT 토큰 생성 실패 - 예외 처리")
    void onAuthenticationSuccess_JwtTokenGenerationFails_ThrowsException() {
        // given
        when(authentication.getPrincipal()).thenReturn(customOAuth2User);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString()))
                .thenThrow(new RuntimeException("JWT 토큰 생성 실패"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            successHandler.onAuthenticationSuccess(request, response, authentication);
        });
    }
}
