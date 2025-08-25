package com.cMall.feedShop.user.infrastructure.oauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CustomOAuth2UserTest {

    private OAuth2User originalOAuth2User;
    private CustomOAuth2User customOAuth2User;

    @BeforeEach
    void setUp() {
        // 원본 OAuth2User 설정
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("name", "테스트 사용자");
        attributes.put("email", "test@example.com");
        attributes.put("picture", "https://example.com/profile.jpg");

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        originalOAuth2User = new DefaultOAuth2User(authorities, attributes, "sub");

        // CustomOAuth2User 생성
        customOAuth2User = new CustomOAuth2User(
                originalOAuth2User,
                "google",
                "123456789",
                "test@example.com",
                "테스트 사용자"
        );
    }

    @Test
    @DisplayName("CustomOAuth2User 생성 - 기본 정보 확인")
    void customOAuth2User_Creation_ContainsCorrectInformation() {
        // then
        assertEquals("google", customOAuth2User.getProvider());
        assertEquals("123456789", customOAuth2User.getProviderId());
        assertEquals("test@example.com", customOAuth2User.getEmail());
        assertEquals("테스트 사용자", customOAuth2User.getName());
        assertSame(originalOAuth2User, customOAuth2User.getOAuth2User());
    }

    @Test
    @DisplayName("getAttributes - 원본 OAuth2User의 속성 반환")
    void getAttributes_ReturnsOriginalOAuth2UserAttributes() {
        // when
        Map<String, Object> attributes = customOAuth2User.getAttributes();

        // then
        assertNotNull(attributes);
        assertEquals("123456789", attributes.get("sub"));
        assertEquals("테스트 사용자", attributes.get("name"));
        assertEquals("test@example.com", attributes.get("email"));
        assertEquals("https://example.com/profile.jpg", attributes.get("picture"));
    }

    @Test
    @DisplayName("getAuthorities - 원본 OAuth2User의 권한 반환")
    void getAuthorities_ReturnsOriginalOAuth2UserAuthorities() {
        // when
        var authorities = customOAuth2User.getAuthorities();

        // then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("getName - 커스텀 이름 반환")
    void getName_ReturnsCustomName() {
        // when
        String name = customOAuth2User.getName();

        // then
        assertEquals("테스트 사용자", name);
    }

    @Test
    @DisplayName("getName - null 이름 처리")
    void getName_WithNullName_ReturnsNull() {
        // given
        CustomOAuth2User userWithNullName = new CustomOAuth2User(
                originalOAuth2User,
                "google",
                "123456789",
                "test@example.com",
                null
        );

        // when
        String name = userWithNullName.getName();

        // then
        assertNull(name);
    }

    @Test
    @DisplayName("다양한 제공자로 CustomOAuth2User 생성")
    void customOAuth2User_DifferentProviders_CreatedSuccessfully() {
        // given & when
        CustomOAuth2User kakaoUser = new CustomOAuth2User(
                originalOAuth2User,
                "kakao",
                "kakao_123456",
                "kakao@example.com",
                "카카오 사용자"
        );

        CustomOAuth2User naverUser = new CustomOAuth2User(
                originalOAuth2User,
                "naver",
                "naver_123456",
                "naver@example.com",
                "네이버 사용자"
        );

        // then
        assertEquals("kakao", kakaoUser.getProvider());
        assertEquals("kakao_123456", kakaoUser.getProviderId());
        assertEquals("kakao@example.com", kakaoUser.getEmail());
        assertEquals("카카오 사용자", kakaoUser.getName());

        assertEquals("naver", naverUser.getProvider());
        assertEquals("naver_123456", naverUser.getProviderId());
        assertEquals("naver@example.com", naverUser.getEmail());
        assertEquals("네이버 사용자", naverUser.getName());
    }

    @Test
    @DisplayName("빈 문자열 값으로 CustomOAuth2User 생성")
    void customOAuth2User_EmptyStringValues_HandlesGracefully() {
        // given & when
        CustomOAuth2User userWithEmptyValues = new CustomOAuth2User(
                originalOAuth2User,
                "",
                "",
                "",
                ""
        );

        // then
        assertEquals("", userWithEmptyValues.getProvider());
        assertEquals("", userWithEmptyValues.getProviderId());
        assertEquals("", userWithEmptyValues.getEmail());
        assertEquals("", userWithEmptyValues.getName());
    }

    @Test
    @DisplayName("null OAuth2User로 CustomOAuth2User 생성")
    void customOAuth2User_NullOAuth2User_HandlesGracefully() {
        // given & when
        CustomOAuth2User userWithNullOAuth2User = new CustomOAuth2User(
                null,
                "google",
                "123456789",
                "test@example.com",
                "테스트 사용자"
        );

        // then
        assertNull(userWithNullOAuth2User.getOAuth2User());
        assertEquals("google", userWithNullOAuth2User.getProvider());
        assertEquals("123456789", userWithNullOAuth2User.getProviderId());
        assertEquals("test@example.com", userWithNullOAuth2User.getEmail());
        assertEquals("테스트 사용자", userWithNullOAuth2User.getName());
    }

    @Test
    @DisplayName("getAttributes - null OAuth2User인 경우")
    void getAttributes_WithNullOAuth2User_ThrowsException() {
        // given
        CustomOAuth2User userWithNullOAuth2User = new CustomOAuth2User(
                null,
                "google",
                "123456789",
                "test@example.com",
                "테스트 사용자"
        );

        // when & then
        assertThrows(NullPointerException.class, () -> {
            userWithNullOAuth2User.getAttributes();
        });
    }

    @Test
    @DisplayName("getAuthorities - null OAuth2User인 경우")
    void getAuthorities_WithNullOAuth2User_ThrowsException() {
        // given
        CustomOAuth2User userWithNullOAuth2User = new CustomOAuth2User(
                null,
                "google",
                "123456789",
                "test@example.com",
                "테스트 사용자"
        );

        // when & then
        assertThrows(NullPointerException.class, () -> {
            userWithNullOAuth2User.getAuthorities();
        });
    }
}
