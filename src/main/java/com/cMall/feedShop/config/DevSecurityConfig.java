package com.cMall.feedShop.config;

import com.cMall.feedShop.user.infrastructure.oauth.CustomOAuth2UserService;
import com.cMall.feedShop.user.infrastructure.oauth.OAuth2AuthenticationSuccessHandler;
import com.cMall.feedShop.user.infrastructure.oauth.OAuth2AuthenticationFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile("dev")
public class DevSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호는 개발 환경에서 API 테스트 편의를 위해 비활성화하는 경우가 많습니다.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 개발 환경에서는 모든 요청에 대해 인증 없이 접근을 허용합니다 (편의성).
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // 폼 로그인 및 HTTP Basic 인증은 사용하지 않습니다.
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                
                // OAuth2 로그인 설정 (개발 환경에서도 테스트 가능)
                .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)
                    )
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
                );
        // 개발 환경에서는 JWT 필터를 추가하지 않습니다. (인증 과정 자체를 생략하므로)

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 개발 환경에서 허용할 Origin 목록
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000", // React 개발 서버 기본 포트
                "http://127.0.0.1:3000" // localhost 대신 127.0.0.1을 사용할 수도 있으므로 추가
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
        config.setAllowCredentials(true); // 자격 증명(쿠키, HTTP 인증 등) 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 CORS 설정 적용
        return source;
    }

    // 개발 환경에서는 일반적으로 PasswordEncoder나 AuthenticationManager를 직접적으로 사용하지 않습니다.
    // authorizeHttpRequests(auth -> auth.anyRequest().permitAll())로 모든 요청을 허용했기 때문입니다.
    // 만약 개발 환경에서도 특정 인증 로직을 테스트해야 한다면, PasswordEncoderConfig에서 제공하는 빈을 사용하면 됩니다.
}