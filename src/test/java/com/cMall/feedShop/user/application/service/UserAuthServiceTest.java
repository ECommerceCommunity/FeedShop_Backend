package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.email.EmailService;
import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.model.PasswordResetToken;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.PasswordResetTokenRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.repository.UserProfileRepository;
import com.cMall.feedShop.user.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserAuthServiceImpl userAuthService;

    private UserLoginRequest loginRequest;
    private User testUser;
    private String dummyToken;

    @BeforeEach
    void setUp() {
        loginRequest = new UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testUser = new User(
                "testLoginId",
                "encodedPassword123",
                "test@example.com",
                UserRole.USER
        );
        testUser.setId(1L);
        testUser.setStatus(UserStatus.ACTIVE);
        ReflectionTestUtils.setField(testUser, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testUser, "updatedAt", LocalDateTime.now());
        testUser.setPasswordChangedAt(LocalDateTime.now());

        dummyToken = "dummy_jwt_token";
        ReflectionTestUtils.setField(userAuthService, "passwordResetBaseUrl", "http://localhost:8080/reset-password");
    }

    @Test
    @DisplayName("성공적인 로그인 - JWT 토큰 발급 확인")
    void login_success_returnsToken() {

        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));

        when(userProfileRepository.findByUser(testUser))
                .thenReturn(Optional.empty());

        when(jwtTokenProvider.generateAccessToken(testUser.getEmail(), testUser.getRole().name()))
                .thenReturn(dummyToken);

        UserLoginResponse response = userAuthService.login(loginRequest);

        assertNotNull(response);
        assertEquals(testUser.getLoginId(), response.getLoginId());
        assertEquals(testUser.getRole(), response.getRole());
        assertEquals(dummyToken, response.getToken());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(jwtTokenProvider, times(1)).generateAccessToken(testUser.getEmail(), testUser.getRole().name());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 회원 (이메일 없음)")
    void login_fail_userNotFound() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail()));

        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            userAuthService.login(loginRequest);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, thrown.getErrorCode());
        assertEquals("존재하지 않는 회원입니다.", thrown.getMessage());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtTokenProvider, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_passwordMismatch() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            userAuthService.login(loginRequest);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, thrown.getErrorCode());
        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다.", thrown.getMessage());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtTokenProvider, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - DELETED 상태 사용자")
    void login_fail_deletedUser() {
        // Given
        User deletedUser = new User("login1", "password123", "deleted@example.com", UserRole.USER);
        deletedUser.setId(1L);
        deletedUser.setStatus(UserStatus.DELETED);
        
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(deletedUser));

        // When & Then
        BusinessException thrown = assertThrows(BusinessException.class, () ->
                userAuthService.login(loginRequest)
        );
        
        assertEquals(ErrorCode.USER_ALREADY_DELETED, thrown.getErrorCode());
        assertEquals("탈퇴된 계정입니다. 새로운 계정으로 가입해주세요.", thrown.getMessage());
        
        // AuthenticationManager가 호출되지 않아야 함
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 - 성공")
    void requestPasswordReset_success() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // when
        userAuthService.requestPasswordReset(testUser.getEmail());

        // then
        verify(passwordResetTokenRepository, times(1)).deleteByUser(testUser);
        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 - 실패 (사용자 없음)")
    void requestPasswordReset_fail_userNotFound() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userAuthService.requestPasswordReset("nonexistent@example.com");
        });
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("비밀번호 재설정 토큰 검증 - 성공")
    void validatePasswordResetToken_success() {
        // given
        PasswordResetToken token = new PasswordResetToken(testUser);
        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        // when & then
        assertDoesNotThrow(() -> {
            userAuthService.validatePasswordResetToken("test-token");
        });
    }

    @Test
    @DisplayName("비밀번호 재설정 토큰 검증 - 실패 (토큰 없음)")
    void validatePasswordResetToken_fail_invalidToken() {
        // given
        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userAuthService.validatePasswordResetToken("invalid-token");
        });
        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("비밀번호 재설정 - 성공")
    void resetPassword_success() {
        // given
        PasswordResetToken token = new PasswordResetToken(testUser);
        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        // when
        userAuthService.resetPassword("test-token", "newPassword");

        // then
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordResetTokenRepository, times(1)).delete(token);
    }

    @Test
    @DisplayName("비밀번호 재설정 - 실패 (토큰 만료)")
    void resetPassword_fail_tokenExpired() {
        // given
        PasswordResetToken token = mock(PasswordResetToken.class);
        when(token.isExpired()).thenReturn(true);
        when(passwordResetTokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userAuthService.resetPassword("expired-token", "newPassword");
        });
        assertEquals(ErrorCode.TOKEN_EXPIRED, exception.getErrorCode());
        verify(passwordResetTokenRepository, times(1)).delete(token);
    }
}