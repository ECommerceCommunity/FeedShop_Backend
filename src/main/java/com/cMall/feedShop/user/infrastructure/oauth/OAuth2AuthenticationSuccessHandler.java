package com.cMall.feedShop.user.infrastructure.oauth;

import com.cMall.feedShop.user.infrastructure.security.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 로그인 성공 시 처리하는 핸들러
 * JWT 토큰을 생성하고 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.authorized-redirect-uri:http://localhost:3000/auth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        if (response.isCommitted()) {
            log.debug("응답이 이미 커밋되었습니다. 리다이렉트를 수행할 수 없습니다.");
            return;
        }

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        
        // JWT 토큰 생성
        String token = jwtTokenProvider.generateAccessToken(oAuth2User.getEmail(), "ROLE_USER");
        
        // 프론트엔드로 리다이렉트 (토큰과 사용자 정보 포함)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .queryParam("email", URLEncoder.encode(oAuth2User.getEmail(), StandardCharsets.UTF_8))
                .queryParam("name", URLEncoder.encode(oAuth2User.getName() != null ? oAuth2User.getName() : "", StandardCharsets.UTF_8))
                .queryParam("provider", oAuth2User.getProvider())
                .build().toUriString();

        log.info("OAuth2 로그인 성공 - 리다이렉트: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
