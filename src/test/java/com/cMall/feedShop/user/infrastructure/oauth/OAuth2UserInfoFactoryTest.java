package com.cMall.feedShop.user.infrastructure.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OAuth2UserInfoFactoryTest {

    @Test
    @DisplayName("Google OAuth2 사용자 정보 생성 - 성공")
    void getOAuth2UserInfo_GoogleProvider_ReturnsGoogleOAuth2UserInfo() {
        // given
        String registrationId = "google";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("name", "테스트 사용자");
        attributes.put("email", "test@example.com");
        attributes.put("picture", "https://example.com/profile.jpg");

        // when
        OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        // then
        assertNotNull(result);
        assertTrue(result instanceof GoogleOAuth2UserInfo);
        assertEquals("123456789", result.getId());
        assertEquals("테스트 사용자", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("https://example.com/profile.jpg", result.getImageUrl());
    }

    @Test
    @DisplayName("Kakao OAuth2 사용자 정보 생성 - 성공")
    void getOAuth2UserInfo_KakaoProvider_ReturnsKakaoOAuth2UserInfo() {
        // given
        String registrationId = "kakao";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "123456789");
        
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", "test@example.com");
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", "카카오 사용자");
        profile.put("profile_image_url", "https://example.com/kakao.jpg");
        
        kakaoAccount.put("profile", profile);
        attributes.put("kakao_account", kakaoAccount);

        // when
        OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        // then
        assertNotNull(result);
        assertTrue(result instanceof KakaoOAuth2UserInfo);
        assertEquals("123456789", result.getId());
        assertEquals("카카오 사용자", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("https://example.com/kakao.jpg", result.getImageUrl());
    }

    @Test
    @DisplayName("Naver OAuth2 사용자 정보 생성 - 성공")
    void getOAuth2UserInfo_NaverProvider_ReturnsNaverOAuth2UserInfo() {
        // given
        String registrationId = "naver";
        Map<String, Object> response = new HashMap<>();
        response.put("id", "123456789");
        response.put("email", "test@example.com");
        response.put("name", "네이버 사용자");
        response.put("profile_image", "https://example.com/naver.jpg");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("response", response);

        // when
        OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        // then
        assertNotNull(result);
        assertTrue(result instanceof NaverOAuth2UserInfo);
        assertEquals("123456789", result.getId());
        assertEquals("네이버 사용자", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("https://example.com/naver.jpg", result.getImageUrl());
    }

    @Test
    @DisplayName("대소문자 구분 없는 제공자 ID 처리 - 성공")
    void getOAuth2UserInfo_CaseInsensitiveProvider_ReturnsCorrectUserInfo() {
        // given
        String registrationId = "GOOGLE";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("name", "테스트 사용자");
        attributes.put("email", "test@example.com");

        // when
        OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        // then
        assertNotNull(result);
        assertTrue(result instanceof GoogleOAuth2UserInfo);
    }

    @Test
    @DisplayName("지원하지 않는 제공자 - 예외 발생")
    void getOAuth2UserInfo_UnsupportedProvider_ThrowsException() {
        // given
        String registrationId = "unsupported";
        Map<String, Object> attributes = new HashMap<>();

        // when & then
        assertThrows(
                OAuth2AuthenticationException.class,
                () -> OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes)
        );
    }

    @Test
    @DisplayName("빈 제공자 ID - 예외 발생")
    void getOAuth2UserInfo_EmptyProvider_ThrowsException() {
        // given
        String registrationId = "";
        Map<String, Object> attributes = new HashMap<>();

        // when & then
        assertThrows(
                OAuth2AuthenticationException.class,
                () -> OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes)
        );
    }

    @Test
    @DisplayName("null 제공자 ID - 예외 발생")
    void getOAuth2UserInfo_NullProvider_ThrowsException() {
        // given
        String registrationId = null;
        Map<String, Object> attributes = new HashMap<>();

        // when & then
        assertThrows(
                OAuth2AuthenticationException.class,
                () -> OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes)
        );
    }
}
