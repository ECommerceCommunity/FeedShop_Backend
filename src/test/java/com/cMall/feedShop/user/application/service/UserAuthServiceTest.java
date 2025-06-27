package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder mock은 AuthenticationManager 내부에서 사용되므로 직접 필요없지만, 생성자에 있다면 Mock으로 주입
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class) // JUnit 5에서 Mockito를 사용하기 위한 설정
class UserAuthServiceTest {

    @Mock // Mock 객체 생성
    private UserRepository userRepository;

    @Mock // Mock 객체 생성
    private PasswordEncoder passwordEncoder; // UserAuthService의 생성자에 있다면 Mock으로 필요

    @Mock // Mock 객체 생성
    private JwtTokenProvider jwtTokenProvider;

    @Mock // Mock 객체 생성
    private AuthenticationManager authenticationManager;

    @InjectMocks // Mock 객체들을 주입받을 테스트 대상 서비스
    private UserAuthService userAuthService;

    // 테스트에 사용될 공통 데이터
    private UserLoginRequest loginRequest;
    private User testUser;
    private String dummyToken;

    @BeforeEach // 각 테스트 메서드 실행 전에 초기화
    void setUp() {
        loginRequest = new UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123"); // 평문 비밀번호

        testUser = new User(
                "testLoginId",
                "encodedPassword123", // DB에 저장된 암호화된 비밀번호
                "test@example.com",
                "010-1234-5678",
                UserRole.ROLE_USER
        );
        // User 엔티티의 createdAt, updatedAt 등은 Lombok @CreatedDate/@LastModifiedDate에 의해 자동 설정되므로
        // 테스트 객체 생성 시에는 생략하거나 mock 데이터를 직접 설정
        testUser.setId(1L);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.setPasswordChangedAt(LocalDateTime.now());

        dummyToken = "dummy_jwt_token";
    }

    @Test
    @DisplayName("성공적인 로그인 - JWT 토큰 발급 확인")
    void login_success_returnsToken() {
        // given (준비): Mock 객체의 행동 정의
        // 1. AuthenticationManager가 인증 성공을 반환하도록 설정
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        // 2. userRepository.findByEmail이 사용자를 찾도록 설정
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));

        // 3. jwtTokenProvider.generateAccessToken이 더미 토큰을 반환하도록 설정
        when(jwtTokenProvider.generateAccessToken(testUser.getEmail(), testUser.getRole().name()))
                .thenReturn(dummyToken);

        // when (실행): 테스트 대상 메서드 호출
        UserLoginResponse response = userAuthService.login(loginRequest);

        // then (검증): 결과 확인
        assertNotNull(response); // 응답이 null이 아닌지 확인
        assertEquals(testUser.getLoginId(), response.getLoginId()); // 로그인 ID 일치 확인
        assertEquals(testUser.getRole(), response.getRole());       // 역할 일치 확인
        assertEquals(dummyToken, response.getToken());              // 토큰 일치 확인

        // Mock 객체의 메서드가 예상대로 호출되었는지 검증
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(jwtTokenProvider, times(1)).generateAccessToken(testUser.getEmail(), testUser.getRole().name());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 회원 (이메일 없음)")
    void login_fail_userNotFound() {
        // given: AuthenticationManager가 UsernameNotFoundException을 던지도록 설정
        // (CustomUserDetailsService 내부에서 발생하며 AuthenticationManager를 통해 전달됨)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail()));

        // when & then: BusinessException이 예상대로 발생하는지 검증
        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            userAuthService.login(loginRequest);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, thrown.getErrorCode());
        assertEquals("존재하지 않는 회원입니다.", thrown.getMessage());

        // Mock 객체 호출 검증
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString()); // 사용자를 찾지 못했으므로 호출되지 않음
        verify(jwtTokenProvider, never()).generateAccessToken(anyString(), anyString()); // 토큰 생성도 호출되지 않음
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_passwordMismatch() {
        // given: AuthenticationManager가 BadCredentialsException을 던지도록 설정
        // (비밀번호 불일치 시 발생)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when & then: BusinessException이 예상대로 발생하는지 검증
        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            userAuthService.login(loginRequest);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, thrown.getErrorCode());
        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다.", thrown.getMessage());

        // Mock 객체 호출 검증
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString()); // 인증 실패로 User 조회가 진행되지 않음
        verify(jwtTokenProvider, never()).generateAccessToken(anyString(), anyString()); // 토큰 생성도 호출되지 않음
    }
}
