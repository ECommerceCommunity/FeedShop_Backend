package com.cMall.feedShop.user.infrastructure.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    // redirectUri 기본값 제거
    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = Optional.ofNullable(exception.getLocalizedMessage())
                .orElse("알 수 없는 OAuth2 로그인 오류가 발생했습니다.");
        log.error("OAuth2 로그인 실패: {}", errorMessage);

        // 쿼리 파라미터 대신 URL 단편(#)에 에러 정보 추가
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .fragment("error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}