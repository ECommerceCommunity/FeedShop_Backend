package com.cMall.feedShop.user.infrastructure.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    @InjectMocks
    private OAuth2AuthenticationFailureHandler failureHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        
        // 리다이렉트 URI 설정
        ReflectionTestUtils.setField(failureHandler, "redirectUri", "https://www.feedshop.store/auth/callback");
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - 기본 에러 메시지")
    void onAuthenticationFailure_DefaultErrorMessage_RedirectsWithError() throws Exception {
        // given
        AuthenticationException exception = new OAuth2AuthenticationException("인증 실패");

        // when
        failureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertEquals(302, response.getStatus()); // 리다이렉트 상태 코드
        
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith("https://www.feedshop.store/auth/callback#"));
        assertTrue(redirectUrl.contains("error=%EC%9D%B8%EC%A6%9D%20%EC%8B%A4%ED%8C%A8")); // URL 인코딩된 에러 메시지
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - 빈 에러 메시지")
    void onAuthenticationFailure_EmptyErrorMessage_UsesDefaultMessage() throws Exception {
        // given
        AuthenticationException exception = new OAuth2AuthenticationException("");

        // when
        failureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertEquals(302, response.getStatus());
        
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("error=%EC%95%8C%20%EC%88%98%20%EC%97%86%EB%8A%94%20OAuth2%20%EB%A1%9C%EA%B7%B8%EC%9D%B8%20%EC%98%A4%EB%A5%98%EA%B0%80%20%EB%B0%9C%EC%83%9D%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4"));
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - 특수문자가 포함된 에러 메시지")
    void onAuthenticationFailure_SpecialCharactersInErrorMessage_ProperlyEncoded() throws Exception {
        // given
        AuthenticationException exception = new OAuth2AuthenticationException("에러 발생: <script>alert('test')</script>");

        // when
        failureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertEquals(302, response.getStatus());
        
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("error="));
        // 특수문자가 URL 인코딩되어 있는지 확인
        assertTrue(redirectUrl.contains("%3Cscript%3Ealert%28%27test%27%29%3C%2Fscript%3E"));
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - 한글 에러 메시지")
    void onAuthenticationFailure_KoreanErrorMessage_ProperlyEncoded() throws Exception {
        // given
        AuthenticationException exception = new OAuth2AuthenticationException("소셜 로그인 서비스에 일시적인 문제가 발생했습니다.");

        // when
        failureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertEquals(302, response.getStatus());
        
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("error=%EC%86%8C%EC%85%9C%20%EB%A1%9C%EA%B7%B8%EC%9D%B8%20%EC%84%9C%EB%B9%84%EC%8A%A4%EC%97%90%20%EC%9D%BC%EC%8B%9C%EC%A0%81%EC%9D%B8%20%EB%AC%B8%EC%A0%9C%EA%B0%80%20%EB%B0%9C%EC%83%9D%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4"));
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - 긴 에러 메시지")
    void onAuthenticationFailure_LongErrorMessage_HandlesGracefully() throws Exception {
        // given
        String longErrorMessage = "매우 긴 에러 메시지입니다. ".repeat(100);
        AuthenticationException exception = new OAuth2AuthenticationException(longErrorMessage);

        // when
        failureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertEquals(302, response.getStatus());
        
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith("https://www.feedshop.store/auth/callback#"));
        assertTrue(redirectUrl.contains("error="));
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - 다른 AuthenticationException 타입")
    void onAuthenticationFailure_DifferentExceptionType_HandlesGracefully() throws Exception {
        // given
        AuthenticationException exception = new OAuth2AuthenticationException("런타임 에러가 발생했습니다.");

        // when
        failureHandler.onAuthenticationFailure(request, response, exception);

        // then
        assertEquals(302, response.getStatus());
        
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("error=%EB%9F%B0%ED%83%80%EC%9E%84%20%EC%97%90%EB%9F%AC%EA%B0%80%20%EB%B0%9C%EC%83%9D%ED%96%88%EC%8A%B5%EB%8B%88%EB%8B%A4"));
    }
}
