package com.cMall.feedShop.user.infrastructure.oauth;

import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserSocialProvider;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.repository.UserSocialProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSocialProviderRepository socialProviderRepository;

    @Mock
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private OAuth2UserRequest userRequest;
    private OAuth2User oAuth2User;
    private User testUser;
    private UserSocialProvider testSocialProvider;

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
        ReflectionTestUtils.setField(testUser, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testUser, "updatedAt", LocalDateTime.now());

        // 테스트 소셜 프로바이더 설정
        testSocialProvider = new UserSocialProvider(
                testUser,
                "google",
                "123456789",
                "test@example.com"
        );
        testSocialProvider.setId(1L);
    }

    @Test
    @DisplayName("기존 소셜 로그인 사용자 처리 - 성공")
    void processAndSaveOAuth2User_ExistingSocialUser_Success() {
        // given
        when(socialProviderRepository.findByProviderAndProviderSocialUserId("google", "123456789"))
                .thenReturn(Optional.of(testSocialProvider));

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, oAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("test@example.com", customOAuth2User.getEmail());
        assertEquals("테스트 사용자", customOAuth2User.getName());
        assertEquals("google", customOAuth2User.getProvider());
        assertEquals("123456789", customOAuth2User.getProviderId());

        verify(socialProviderRepository, times(1))
                .findByProviderAndProviderSocialUserId("google", "123456789");
        verify(userRepository, never()).findByEmail(anyString());
        verify(socialProviderRepository, never()).save(any(UserSocialProvider.class));
    }

    @Test
    @DisplayName("새로운 소셜 로그인 사용자 처리 - 기존 이메일 사용자 존재")
    void processAndSaveOAuth2User_NewSocialUser_ExistingEmailUser_Success() {
        // given
        when(socialProviderRepository.findByProviderAndProviderSocialUserId("google", "123456789"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.existsByLoginId(anyString()))
                .thenReturn(false);
        when(socialProviderRepository.save(any(UserSocialProvider.class)))
                .thenReturn(testSocialProvider);

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, oAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("test@example.com", customOAuth2User.getEmail());

        verify(socialProviderRepository, times(1))
                .findByProviderAndProviderSocialUserId("google", "123456789");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(socialProviderRepository, times(1)).save(any(UserSocialProvider.class));
    }

    @Test
    @DisplayName("새로운 소셜 로그인 사용자 처리 - 완전히 새로운 사용자")
    void processAndSaveOAuth2User_NewSocialUser_CompletelyNewUser_Success() {
        // given
        when(socialProviderRepository.findByProviderAndProviderSocialUserId("google", "123456789"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.existsByLoginId(anyString()))
                .thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);
        when(socialProviderRepository.save(any(UserSocialProvider.class)))
                .thenReturn(testSocialProvider);

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, oAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("test@example.com", customOAuth2User.getEmail());

        verify(socialProviderRepository, times(1))
                .findByProviderAndProviderSocialUserId("google", "123456789");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(socialProviderRepository, times(1)).save(any(UserSocialProvider.class));
    }

    @Test
    @DisplayName("이메일이 없는 OAuth2 사용자 - 예외 발생")
    void processAndSaveOAuth2User_NoEmail_ThrowsException() {
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

    @Test
    @DisplayName("지원하지 않는 OAuth2 제공자 - 예외 발생")
    void processAndSaveOAuth2User_UnsupportedProvider_ThrowsException() {
        // given
        when(userRequest.getClientRegistration().getRegistrationId()).thenReturn("unsupported");

        // when & then
        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> customOAuth2UserService.processAndSaveOAuth2User(userRequest, oAuth2User)
        );

        assertEquals("지원하지 않는 소셜 로그인 제공자입니다: unsupported", exception.getMessage());
    }

    @Test
    @DisplayName("카카오 OAuth2 사용자 처리 - 성공")
    void processAndSaveOAuth2User_KakaoProvider_Success() {
        // given
        when(userRequest.getClientRegistration().getRegistrationId()).thenReturn("kakao");

        Map<String, Object> kakaoAttributes = new HashMap<>();
        kakaoAttributes.put("id", "123456789");
        
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", "test@example.com");
        
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

        when(socialProviderRepository.findByProviderAndProviderSocialUserId("kakao", "123456789"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(socialProviderRepository.save(any(UserSocialProvider.class)))
                .thenReturn(testSocialProvider);

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, kakaoOAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("test@example.com", customOAuth2User.getEmail());
        assertEquals("카카오 사용자", customOAuth2User.getName());
        assertEquals("kakao", customOAuth2User.getProvider());
    }

    @Test
    @DisplayName("네이버 OAuth2 사용자 처리 - 성공")
    void processAndSaveOAuth2User_NaverProvider_Success() {
        // given
        when(userRequest.getClientRegistration().getRegistrationId()).thenReturn("naver");

        Map<String, Object> response = new HashMap<>();
        response.put("id", "123456789");
        response.put("email", "test@example.com");
        response.put("name", "네이버 사용자");
        response.put("profile_image", "https://example.com/naver.jpg");

        Map<String, Object> naverAttributes = new HashMap<>();
        naverAttributes.put("response", response);

        OAuth2User naverOAuth2User = new DefaultOAuth2User(
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                naverAttributes,
                "response"
        );

        when(socialProviderRepository.findByProviderAndProviderSocialUserId("naver", "123456789"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(socialProviderRepository.save(any(UserSocialProvider.class)))
                .thenReturn(testSocialProvider);

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, naverOAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("test@example.com", customOAuth2User.getEmail());
        assertEquals("네이버 사용자", customOAuth2User.getName());
        assertEquals("naver", customOAuth2User.getProvider());
    }
}
