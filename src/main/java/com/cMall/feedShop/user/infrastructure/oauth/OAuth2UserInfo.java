package com.cMall.feedShop.user.infrastructure.oauth;

import java.util.Map;

/**
 * OAuth2 제공자별 사용자 정보 추상화 클래스
 * 각 소셜 로그인 제공자마다 다른 JSON 구조를 통일된 인터페이스로 제공
 */
public abstract class OAuth2UserInfo {
    
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
}
