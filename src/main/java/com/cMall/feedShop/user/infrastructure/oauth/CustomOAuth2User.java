package com.cMall.feedShop.user.infrastructure.oauth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * OAuth2 사용자 정보를 담는 커스텀 클래스
 * Spring Security의 OAuth2User를 구현하여 소셜 로그인 사용자 정보를 관리
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oAuth2User;
    private final String provider;
    private final String providerId;
    private final String email;
    private final String name;

    public CustomOAuth2User(OAuth2User oAuth2User, String provider, String providerId, String email, String name) {
        this.oAuth2User = oAuth2User;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.name = name;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return name;
    }
}
