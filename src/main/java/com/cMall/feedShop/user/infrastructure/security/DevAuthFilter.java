package com.cMall.feedShop.user.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 로컬 개발 전용 인증 필터
 *
 * X-User-Id 헤더에 loginId를 담아 요청하면 해당 사용자로 인증됩니다.
 * 헤더가 없으면 기본값 "dev-user"로 인증됩니다.
 *
 * 사용 예: curl -H "X-User-Id: testuser1" http://localhost:8081/api/feeds
 */
@Slf4j
@Component
@Profile("dev")
public class DevAuthFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String DEFAULT_LOGIN_ID = "dev-user";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String loginId = request.getHeader(USER_ID_HEADER);
        if (loginId == null || loginId.isBlank()) {
            loginId = DEFAULT_LOGIN_ID;
        }

        UserDetails userDetails = User.builder()
                .username(loginId)
                .password("")
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                ))
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("DevAuthFilter - 인증 설정: loginId={}", loginId);

        filterChain.doFilter(request, response);
    }
}
