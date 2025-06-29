package com.cMall.feedShop.user.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails; // UserDetails import 유지 (generateRefreshToken 등에서 사용될 수 있음)
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-ms:3600000}") // 1시간
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration-ms:1209600000}") // 2주
    private long refreshTokenExpiration;

    private Key key;

    @PostConstruct
    public void init() {
        // 시크릿 키는 최소 256비트 (HS256) 또는 512비트 (HS512) 이상을 권장합니다.
        // 환경 변수나 설정 파일에서 가져오는 secretKey가 충분히 길고 복잡한지 확인하세요.
        // 만약 짧다면, "your_super_secret_key_for_jwt_signing_which_should_be_at_least_256_bit"
        // 와 같이 안전한 값을 사용해야 합니다.
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // AccessToken 생성: subject를 email로 설정하고 role을 클레임에 추가
    public String generateAccessToken(String email, String role) { // <-- 시그니처 변경: UserDetails 대신 email과 role 직접 받음
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // 클레임에 사용자 역할 추가

        return Jwts.builder()
                .setClaims(claims) // 클레임 설정
                .setSubject(email) // <-- 토큰의 주체(subject)를 email로 설정
                .setIssuedAt(new Date()) // 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration)) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 서명 알고리즘과 시크릿 키
                .compact(); // 토큰 생성
    }

    public String generateRefreshToken(String username) {
        // refresh token의 subject도 email로 할지 loginId로 할지 결정해야 합니다.
        // 여기서는 기존대로 username (즉, loginId)을 사용합니다.
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage()); // JWT 서명 문제
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage()); // JWT 만료
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage()); // 지원되지 않는 JWT 형식
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage()); // JWT 클레임 문자열이 비어있음
        }
        return false;
    }

    // 토큰에서 email (subject)을 가져오는 메서드
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
