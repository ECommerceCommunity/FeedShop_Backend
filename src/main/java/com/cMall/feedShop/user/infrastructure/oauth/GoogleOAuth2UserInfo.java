package com.cMall.feedShop.user.infrastructure.oauth;

import java.util.Map;

/**
 * 구글 OAuth2 사용자 정보 구현체
 * 구글에서 제공하는 사용자 정보 JSON 구조에 맞춰 데이터를 추출
 */
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
}
