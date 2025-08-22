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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2IntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSocialProviderRepository socialProviderRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private OAuth2UserRequest userRequest;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private OAuth2User oAuth2User;

    @BeforeEach
    void setUp() {
        // OAuth2UserRequest 모킹
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
    }

    @Test
    @DisplayName("OAuth2 단위 테스트 - 새로운 사용자 소셜 로그인")
    void oauth2Unit_NewUserSocialLogin_Success() {
        // given
        when(socialProviderRepository.findByProviderAndProviderSocialUserId("google", "123456789"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.existsByLoginId(anyString()))
                .thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });
        when(socialProviderRepository.save(any(UserSocialProvider.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, oAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("test@example.com", customOAuth2User.getEmail());
        assertEquals("테스트 사용자", customOAuth2User.getName());
        assertEquals("google", customOAuth2User.getProvider());

        // verify
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(socialProviderRepository).save(any(UserSocialProvider.class));
    }

    @Test
    @DisplayName("OAuth2 단위 테스트 - 기존 사용자 소셜 로그인")
    void oauth2Unit_ExistingUserSocialLogin_Success() {
        // given
        User existingUser = new User(
                "social_test123",
                "encodedPassword",
                "test@example.com",
                UserRole.USER
        );
        existingUser.setId(1L);
        existingUser.setStatus(UserStatus.ACTIVE);

        UserSocialProvider existingProvider = new UserSocialProvider(
                existingUser,
                "google",
                "123456789",
                "test@example.com"
        );

        when(socialProviderRepository.findByProviderAndProviderSocialUserId("google", "123456789"))
                .thenReturn(Optional.of(existingProvider));

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, oAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("test@example.com", customOAuth2User.getEmail());

        // verify
        verify(socialProviderRepository).findByProviderAndProviderSocialUserId("google", "123456789");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("OAuth2 단위 테스트 - 같은 이메일 다른 제공자")
    void oauth2Unit_SameEmailDifferentProvider_Success() {
        // given
        User existingUser = new User(
                "social_test123",
                "encodedPassword",
                "test@example.com",
                UserRole.USER
        );
        existingUser.setId(1L);
        existingUser.setStatus(UserStatus.ACTIVE);

        // 기존 소셜 프로바이더는 없지만, 같은 이메일의 사용자는 있음
        when(socialProviderRepository.findByProviderAndProviderSocialUserId("google", "123456789"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(socialProviderRepository.save(any(UserSocialProvider.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        OAuth2User result = customOAuth2UserService.processAndSaveOAuth2User(userRequest, oAuth2User);

        // then
        assertNotNull(result);
        assertTrue(result instanceof CustomOAuth2User);
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) result;
        assertEquals("test@example.com", customOAuth2User.getEmail());

        // verify
        verify(userRepository).findByEmail("test@example.com");
        verify(socialProviderRepository).save(any(UserSocialProvider.class));
    }

    @Test
    @DisplayName("OAuth2 단위 테스트 - 이메일이 없는 경우 예외 발생")
    void oauth2Unit_NoEmail_ThrowsException() {
        // given
        Map<String, Object> attributesWithoutEmail = new HashMap<>();
        attributesWithoutEmail.put("sub", "123456789");
        attributesWithoutEmail.put("name", "테스트 사용자");
        // email은 포함하지 않음

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

        // 예외가 발생했는지만 확인 (메시지는 구현에 따라 달라질 수 있음)
        assertNotNull(exception);
    }
}
