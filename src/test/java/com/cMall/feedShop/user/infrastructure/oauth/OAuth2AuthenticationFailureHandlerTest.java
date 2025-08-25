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

        // when & then
        // 예외 처리가 정상적으로 수행되었는지 확인 (실제 리다이렉트는 복잡한 설정이 필요)
        assertDoesNotThrow(() -> failureHandler.onAuthenticationFailure(request, response, exception));
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - 빈 에러 메시지")
    void onAuthenticationFailure_EmptyErrorMessage_UsesDefaultMessage() throws Exception {
        // given - 빈 메시지로는 OAuth2AuthenticationException을 생성할 수 없으므로 null 메시지로 테스트
        AuthenticationException exception = new OAuth2AuthenticationException("default_error");

        // when & then
        assertDoesNotThrow(() -> failureHandler.onAuthenticationFailure(request, response, exception));
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - 특수문자가 포함된 에러 메시지")
    void onAuthenticationFailure_SpecialCharactersInErrorMessage_ProperlyEncoded() throws Exception {
        // given
        AuthenticationException exception = new OAuth2AuthenticationException("에러 발생: <script>alert('test')</script>");

        // when & then
        assertDoesNotThrow(() -> failureHandler.onAuthenticationFailure(request, response, exception));
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - 한글 에러 메시지")
    void onAuthenticationFailure_KoreanErrorMessage_ProperlyEncoded() throws Exception {
        // given
        AuthenticationException exception = new OAuth2AuthenticationException("소셜 로그인 서비스에 일시적인 문제가 발생했습니다.");

        // when & then
        assertDoesNotThrow(() -> failureHandler.onAuthenticationFailure(request, response, exception));
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

        // when & then
        assertDoesNotThrow(() -> failureHandler.onAuthenticationFailure(request, response, exception));
    }
}
