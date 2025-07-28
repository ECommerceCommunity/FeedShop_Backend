package com.cMall.feedShop.user.infrastructure.security;

import com.cMall.feedShop.user.infrastructure.service.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-ms:3600000}") // 1시간
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration-ms:1209600000}") // 2주
    private long refreshTokenExpiration;

    private Key key;

    private final CustomUserDetailsService customUserDetailsService;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // AccessToken 생성: subject를 email로 설정하고 role을 클레임에 추가
    public String generateAccessToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
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
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
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

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        String email = claims.getSubject();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // JWT에 role이 있으면 사용, 없으면 UserDetails에서 가져오기
        String role = claims.get("role", String.class);
        Collection<? extends GrantedAuthority> authorities;

        if (role != null) {
            // 단일 role을 authority로 변환
            authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_" + role));
        } else {
            // UserDetails에서 authorities 가져오기
            authorities = userDetails.getAuthorities();
        }

        log.debug("Authenticated user: {}", userDetails.getUsername());
        log.debug("Authorities from JWT/UserDetails: {}", authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }
}

