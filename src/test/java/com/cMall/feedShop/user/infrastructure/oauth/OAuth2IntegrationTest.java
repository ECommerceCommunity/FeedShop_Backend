package com.cMall.feedShop.user.infrastructure.oauth;

import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserSocialProvider;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.repository.UserSocialProviderRepository;
import com.cMall.feedShop.user.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OAuth2IntegrationTest {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler failureHandler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSocialProviderRepository socialProviderRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    private OAuth2UserRequest userRequest;
    private OAuth2User oAuth2User;
    private User testUser;

    @BeforeEach
    void setUp() {
        // OAuth2UserRequest 모킹
        userRequest = mock(OAuth2UserRequest.class);
        when(userRequest.getClientRegistration()).thenReturn(mock(org.springframework.security.oauth2.client.registration.ClientRegistration.class));
        when(userRequest.getClientRegistration().getRegistrationId()).thenReturn("google");

        // OAuth2User 모킹
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("name", "테스트 사용자");
        attributes.put("email", "test@example.com");
        attributes.put("picture", "https://example.com/profile.jpg");

        oAuth2User = new DefaultOAuth2User(
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );

        // 테스트 사용자 설정
        testUser = new User(
                "social_test123",
                "encodedPassword",
                "test@example.com",
                UserRole.USER
        );
        testUser.setId(1L);
        testUser.setStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("OAuth2 통합 테스트 - 새로운 사용자 소셜 로그인")
    void oauth2Integration_NewUserSocialLogin_Success() {
        // given
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString()))
                .thenReturn("test_jwt_token");

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, oAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("test@example.com", customOAuth2User.getEmail());
        assertEquals("테스트 사용자", customOAuth2User.getName());
        assertEquals("google", customOAuth2User.getProvider());

        // 데이터베이스에 사용자와 소셜 프로바이더가 저장되었는지 확인
        Optional<User> savedUser = userRepository.findByEmail("test@example.com");
        assertTrue(savedUser.isPresent());
        assertEquals("test@example.com", savedUser.get().getEmail());

        Optional<UserSocialProvider> savedProvider = socialProviderRepository
                .findByProviderAndProviderSocialUserId("google", "123456789");
        assertTrue(savedProvider.isPresent());
        assertEquals("google", savedProvider.get().getProvider());
        assertEquals("123456789", savedProvider.get().getProviderSocialUserId());
    }

    @Test
    @DisplayName("OAuth2 통합 테스트 - 기존 사용자 소셜 로그인")
    void oauth2Integration_ExistingUserSocialLogin_Success() {
        // given
        // 기존 사용자 저장
        User savedUser = userRepository.save(testUser);
        
        UserSocialProvider socialProvider = new UserSocialProvider(
                savedUser,
                "google",
                "123456789",
                "test@example.com"
        );
        socialProviderRepository.save(socialProvider);

        when(jwtTokenProvider.generateAccessToken(anyString(), anyString()))
                .thenReturn("test_jwt_token");

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, oAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("test@example.com", customOAuth2User.getEmail());

        // 기존 사용자 정보가 유지되는지 확인
        Optional<User> existingUser = userRepository.findByEmail("test@example.com");
        assertTrue(existingUser.isPresent());
        assertEquals(savedUser.getId(), existingUser.get().getId());
    }

    @Test
    @DisplayName("OAuth2 통합 테스트 - 같은 이메일 다른 제공자")
    void oauth2Integration_SameEmailDifferentProvider_Success() {
        // given
        // 기존 사용자 저장
        User savedUser = userRepository.save(testUser);
        
        // 카카오로 기존 로그인
        UserSocialProvider kakaoProvider = new UserSocialProvider(
                savedUser,
                "kakao",
                "kakao_123456",
                "test@example.com"
        );
        socialProviderRepository.save(kakaoProvider);

        // 구글 로그인 시도
        when(userRequest.getClientRegistration().getRegistrationId()).thenReturn("google");
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString()))
                .thenReturn("test_jwt_token");

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, oAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("test@example.com", customOAuth2User.getEmail());

        // 두 개의 소셜 프로바이더가 모두 저장되었는지 확인
        Optional<UserSocialProvider> kakaoSaved = socialProviderRepository
                .findByProviderAndProviderSocialUserId("kakao", "kakao_123456");
        assertTrue(kakaoSaved.isPresent());

        Optional<UserSocialProvider> googleSaved = socialProviderRepository
                .findByProviderAndProviderSocialUserId("google", "123456789");
        assertTrue(googleSaved.isPresent());

        // 같은 사용자에 연결되었는지 확인
        assertEquals(kakaoSaved.get().getUser().getId(), googleSaved.get().getUser().getId());
    }

    @Test
    @DisplayName("OAuth2 통합 테스트 - 카카오 제공자")
    void oauth2Integration_KakaoProvider_Success() {
        // given
        when(userRequest.getClientRegistration().getRegistrationId()).thenReturn("kakao");

        Map<String, Object> kakaoAttributes = new HashMap<>();
        kakaoAttributes.put("id", "123456789");
        
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", "kakao@example.com");
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", "카카오 사용자");
        profile.put("profile_image_url", "https://example.com/kakao.jpg");
        
        kakaoAccount.put("profile", profile);
        kakaoAttributes.put("kakao_account", kakaoAccount);

        OAuth2User kakaoOAuth2User = new DefaultOAuth2User(
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                kakaoAttributes,
                "id"
        );

        when(jwtTokenProvider.generateAccessToken(anyString(), anyString()))
                .thenReturn("kakao_jwt_token");

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, kakaoOAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("kakao@example.com", customOAuth2User.getEmail());
        assertEquals("카카오 사용자", customOAuth2User.getName());
        assertEquals("kakao", customOAuth2User.getProvider());

        // 데이터베이스에 저장되었는지 확인
        Optional<User> savedUser = userRepository.findByEmail("kakao@example.com");
        assertTrue(savedUser.isPresent());

        Optional<UserSocialProvider> savedProvider = socialProviderRepository
                .findByProviderAndProviderSocialUserId("kakao", "123456789");
        assertTrue(savedProvider.isPresent());
    }

    @Test
    @DisplayName("OAuth2 통합 테스트 - 네이버 제공자")
    void oauth2Integration_NaverProvider_Success() {
        // given
        when(userRequest.getClientRegistration().getRegistrationId()).thenReturn("naver");

        Map<String, Object> response = new HashMap<>();
        response.put("id", "123456789");
        response.put("email", "naver@example.com");
        response.put("name", "네이버 사용자");
        response.put("profile_image", "https://example.com/naver.jpg");

        Map<String, Object> naverAttributes = new HashMap<>();
        naverAttributes.put("response", response);

        OAuth2User naverOAuth2User = new DefaultOAuth2User(
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                naverAttributes,
                "response"
        );

        when(jwtTokenProvider.generateAccessToken(anyString(), anyString()))
                .thenReturn("naver_jwt_token");

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, naverOAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("naver@example.com", customOAuth2User.getEmail());
        assertEquals("네이버 사용자", customOAuth2User.getName());
        assertEquals("naver", customOAuth2User.getProvider());

        // 데이터베이스에 저장되었는지 확인
        Optional<User> savedUser = userRepository.findByEmail("naver@example.com");
        assertTrue(savedUser.isPresent());

        Optional<UserSocialProvider> savedProvider = socialProviderRepository
                .findByProviderAndProviderSocialUserId("naver", "123456789");
        assertTrue(savedProvider.isPresent());
    }

    @Test
    @DisplayName("OAuth2 통합 테스트 - 이메일이 없는 경우 예외 발생")
    void oauth2Integration_NoEmail_ThrowsException() {
        // given
        Map<String, Object> attributesWithoutEmail = new HashMap<>();
        attributesWithoutEmail.put("sub", "123456789");
        attributesWithoutEmail.put("name", "테스트 사용자");

        OAuth2User oAuth2UserWithoutEmail = new DefaultOAuth2User(
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                attributesWithoutEmail,
                "sub"
        );

        // when & then
        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> customOAuth2UserService.processAndSaveOAuth2User(userRequest, oAuth2UserWithoutEmail)
        );

        assertEquals("소셜 로그인 제공자에서 이메일을 가져올 수 없습니다.", exception.getMessage());
    }
}
