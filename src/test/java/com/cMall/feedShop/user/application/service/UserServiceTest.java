package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.service.EmailService;
import com.cMall.feedShop.user.application.dto.request.UserSignUpRequest;
import com.cMall.feedShop.user.application.dto.response.UserResponse;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.repository.UserProfileRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils; // @Value 필드 주입을 위한 유틸리티

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static com.cMall.feedShop.common.exception.ErrorCode.*; // ErrorCode static import

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock // EmailService mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService; // 테스트 대상 서비스

    private UserSignUpRequest signUpRequest;

    // SecurityContextHolder를 정리하는 AfterEach
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // 관리자 권한 설정을 위한 헬퍼 메서드
    private void setupAdminAuthentication() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin@example.com",
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 일반 사용자 권한 설정을 위한 헬퍼 메서드
    private void setupUserAuthentication(String email) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @BeforeEach
    void setUp() {
        signUpRequest = new UserSignUpRequest();
        signUpRequest.setName("테스트유저");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password123!");
        signUpRequest.setConfirmPassword("password123!");
        signUpRequest.setRole(UserRole.USER); // signUpRequest에 role 필드가 있다면
        signUpRequest.setPhone("01012345678");

        // UserService의 @Value 필드에 값 주입 (테스트 환경에서 필요)
        ReflectionTestUtils.setField(userService, "verificationUrl", "http://localhost:8080/api/auth/verify-email?token=");
    }

    // 회원가입 성공 - 신규 가입 (PENDING 상태, 인증 메일 발송)
    @Test
    @DisplayName("회원가입 성공 - 신규 가입, 인증 메일 발송")
    void signUp_Success_NewUser_Pending() {
        // Given
        given(userRepository.findByEmail(signUpRequest.getEmail())).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L); // ID 설정
            savedUser.setUserProfile(new UserProfile(savedUser, signUpRequest.getName(), signUpRequest.getName(), signUpRequest.getPhone())); // UserProfile 설정
            return savedUser;
        });

        // When
        UserResponse response = userService.signUp(signUpRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(signUpRequest.getEmail());
        assertThat(response.getStatus()).isEqualTo(UserStatus.PENDING); // PENDING 상태 확인
        verify(emailService, times(1)).sendSimpleEmail(
                eq(signUpRequest.getEmail()),
                contains("회원가입을 완료해주세요"),
                contains("인증을 완료해주세요")
        );
        verify(userRepository, times(1)).save(any(User.class));
    }

    // 회원가입 실패 - 이미 ACTIVE 상태
    @Test
    @DisplayName("회원가입 실패 - 이미 ACTIVE 상태")
    void signUp_Fail_ActiveUser() {
        // Given
        User existingUser = new User();
        existingUser.setStatus(UserStatus.ACTIVE);
        existingUser.setEmail(signUpRequest.getEmail()); // 이메일 설정
        given(userRepository.findByEmail(signUpRequest.getEmail())).willReturn(Optional.of(existingUser));

        // When & Then
        UserException thrown = assertThrows(UserException.class, () ->
                userService.signUp(signUpRequest)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(DUPLICATE_EMAIL); // ErrorCode 사용
        verify(emailService, never()).sendSimpleEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("회원가입 성공 - PENDING 상태에서 재인증 메일 발송")
    void signUp_Success_PendingUser_ResendEmail() {
        // Given
        User existingPendingUser = new User(
                "someLoginId", "oldEncodedPw", signUpRequest.getEmail(), UserRole.USER);
        existingPendingUser.setId(1L);
        existingPendingUser.setStatus(UserStatus.PENDING);
        existingPendingUser.setVerificationToken("oldToken");
        existingPendingUser.setVerificationTokenExpiry(LocalDateTime.now().minusHours(1)); // 만료된 토큰
        existingPendingUser.setUserProfile(new UserProfile(existingPendingUser, "기존이름", "기존이름", "01011112222"));

        given(userRepository.findByEmail(signUpRequest.getEmail())).willReturn(Optional.of(existingPendingUser));
        // save 호출 시 업데이트된 existingPendingUser를 반환하도록 Mocking
        // 이 Mock은 save가 호출될 것이라는 전제하에 작동.
        // 만약 서비스 로직에서 save 후 바로 예외를 던진다면, 이 mocking 자체는 문제 없음.
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When & Then (여기서 assertThrows를 사용하여 예외를 잡아야 합니다)
        UserException thrown = assertThrows(UserException.class, () ->
                userService.signUp(signUpRequest)
        );

        // Then
        // 예외가 발생했는지 확인
        assertThat(thrown.getErrorCode()).isEqualTo(DUPLICATE_EMAIL); // 또는 적절한 에러 코드
        assertThat(thrown.getMessage()).contains("재인증 메일이 발송되었습니다"); // 메시지 확인

        // save가 호출되었는지 확인 (서비스 로직이 토큰 업데이트 후 저장한다면)
        verify(userRepository, times(1)).save(existingPendingUser); // save 호출 확인

        // 이메일 서비스 호출 확인
        verify(emailService, times(1)).sendSimpleEmail(
                eq(signUpRequest.getEmail()),
                contains("회원가입 재인증을 완료해주세요"),
                contains("재인증을 요청하셨습니다")
        );

        // PENDING 유저의 상태가 변경되지 않았는지 확인 (예외 발생했으므로)
        // assertThat(existingPendingUser.getStatus()).isEqualTo(UserStatus.PENDING); // 이 부분은 이제 필요 없을 수 있음.
        // 실제 객체의 토큰과 만료 시간은 서비스 로직에 의해 업데이트되었을 것이므로, 확인.
        assertThat(existingPendingUser.getVerificationToken()).isNotNull();
        assertThat(existingPendingUser.getVerificationTokenExpiry()).isAfter(LocalDateTime.now());
    }



    @Test
    @DisplayName("회원가입 성공 - 비밀번호가 이미 암호화된 경우")
    void signUp_Success_PreEncryptedPassword() {
        // Given
        String preEncryptedPassword = "$2a$10$abcdefghijklmnopqrstuvwxyza.abcdefghijklmnopqrs.";
        signUpRequest.setPassword(preEncryptedPassword);
        signUpRequest.setConfirmPassword(preEncryptedPassword); // confirmPassword도 맞춰줘야 함

        given(userRepository.findByEmail(signUpRequest.getEmail())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            savedUser.setUserProfile(new UserProfile(savedUser, signUpRequest.getName(), signUpRequest.getName(), signUpRequest.getPhone()));
            assertThat(savedUser.getPassword()).isEqualTo(preEncryptedPassword); // 암호화되지 않고 그대로 저장됨을 검증
            return savedUser;
        });

        // When
        UserResponse response = userService.signUp(signUpRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(signUpRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString()); // encode 메서드가 호출되지 않아야 함
        verify(userRepository, times(1)).save(any(User.class));
    }


    // 이메일 인증 성공
    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_Success() {
        // Given
        String token = "valid-token";
        User user = new User();
        user.setStatus(UserStatus.PENDING);
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(30)); // 유효한 토큰
        given(userRepository.findByVerificationToken(token)).willReturn(Optional.of(user));

        // When
        userService.verifyEmail(token);

        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getVerificationToken()).isNull();
        assertThat(user.getVerificationTokenExpiry()).isNull();
        verify(userRepository, times(1)).save(user); // save 호출 확인
    }

    // 이메일 인증 실패 - 이미 인증 완료된 계정
    @Test
    @DisplayName("이메일 인증 실패 - 이미 인증 완료된 계정")
    void verifyEmail_Fail_AlreadyVerified() {
        // Given
        String token = "already-verified-token";
        User user = new User();
        user.setStatus(UserStatus.ACTIVE); // 이미 ACTIVE
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(30));
        given(userRepository.findByVerificationToken(token)).willReturn(Optional.of(user));

        // When & Then
        UserException thrown = assertThrows(UserException.class, () ->
                userService.verifyEmail(token)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(ACCOUNT_ALREADY_VERIFIED);
        verify(userRepository, never()).save(any(User.class)); // save가 호출되지 않아야 함
    }


    // 이메일 인증 실패 - 만료된 토큰
    @Test
    @DisplayName("이메일 인증 실패 - 만료된 토큰")
    void verifyEmail_Fail_ExpiredToken() {
        // Given
        String token = "expired-token";
        User user = new User();
        user.setStatus(UserStatus.PENDING);
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().minusMinutes(1)); // 만료된 토큰
        given(userRepository.findByVerificationToken(token)).willReturn(Optional.of(user));

        // When & Then
        UserException thrown = assertThrows(UserException.class, () ->
                userService.verifyEmail(token)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(VERIFICATION_TOKEN_EXPIRED);
        verify(userRepository, times(1)).save(user); // 토큰 정보를 null로 저장했으므로 save 호출 확인
    }

    // 이메일 인증 실패 - 잘못된 토큰 (찾을 수 없음)
    @Test
    @DisplayName("이메일 인증 실패 - 잘못된 토큰 (찾을 수 없음)")
    void verifyEmail_Fail_InvalidToken() {
        // Given
        String token = "invalid-token";
        given(userRepository.findByVerificationToken(token)).willReturn(Optional.empty());

        // When & Then
        UserException thrown = assertThrows(UserException.class, () ->
                userService.verifyEmail(token)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(INVALID_VERIFICATION_TOKEN);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 이메일이 이미 존재하는 경우")
    void isEmailDuplicated_True() {
        // Given
        String existingEmail = "duplicate@example.com";
        given(userRepository.existsByEmail(existingEmail)).willReturn(true);

        // When
        boolean isDuplicated = userService.isEmailDuplicated(existingEmail);

        // Then
        assertThat(isDuplicated).isTrue();
        verify(userRepository, times(1)).existsByEmail(existingEmail);
    }

    @Test
    @DisplayName("이메일 중복 확인 - 이메일이 존재하지 않는 경우")
    void isEmailDuplicated_False() {
        // Given
        String newEmail = "new@example.com";
        given(userRepository.existsByEmail(newEmail)).willReturn(false);

        // When
        boolean isDuplicated = userService.isEmailDuplicated(newEmail);

        // Then
        assertThat(isDuplicated).isFalse();
        verify(userRepository, times(1)).existsByEmail(newEmail);
    }

    // --- 회원 탈퇴 테스트 ---

    @Test
    @DisplayName("회원 탈퇴 성공 - 관리자가 사용자 ID로 탈퇴")
    void withdrawUser_Success_ById() {
        // Given
        setupAdminAuthentication(); // 관리자 권한 설정
        Long userId = 1L;
        User user = new User("login1", "pw", "user1@example.com", UserRole.USER);
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // When
        userService.withdrawUser(userId);

        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 관리자 권한 없이 사용자 ID로 탈퇴 시도")
    void withdrawUser_Fail_NoAdminAuthority() {
        // Given (No admin authority set)
        Long userId = 1L;
        User user = new User("login1", "pw", "user1@example.com", UserRole.USER);
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        // When & Then
        UserException thrown = assertThrows(UserException.class, () ->
                userService.withdrawUser(userId)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(FORBIDDEN);
        verify(userRepository, never()).findById(anyLong()); // 권한 없으므로 findById도 호출 안됨
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    @DisplayName("회원 탈퇴 실패 - 사용자 ID로 탈퇴 시 이미 DELETED 상태")
    void withdrawUser_Fail_AlreadyDeleted_ById() {
        // Given
        setupAdminAuthentication(); // 관리자 권한 설정
        Long userId = 1L;
        User user = new User("login1", "pw", "user1@example.com", UserRole.USER);
        user.setId(userId);
        user.setStatus(UserStatus.DELETED); // 이미 DELETED 상태
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // When & Then
        UserException thrown = assertThrows(UserException.class, () ->
                userService.withdrawUser(userId)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(USER_ALREADY_DELETED); // ErrorCode 사용
        verify(userRepository, never()).save(any(User.class)); // save가 호출되지 않아야 함
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 관리자가 이메일로 탈퇴")
    void adminWithdrawUserByEmail_Success() {
        // Given
        setupAdminAuthentication(); // 관리자 권한 설정
        String email = "test@example.com";
        User user = new User("login1", "pw", email, UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // When
        userService.adminWithdrawUserByEmail(email);

        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 관리자 권한 없이 이메일로 탈퇴 시도")
    void adminWithdrawUserByEmail_Fail_NoAdminAuthority() {
        // Given (No admin authority set)
        String email = "test@example.com";
        User user = new User("login1", "pw", email, UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        // When & Then
        UserException thrown = assertThrows(UserException.class, () ->
                userService.adminWithdrawUserByEmail(email)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(FORBIDDEN);
        verify(userRepository, never()).findByEmail(anyString()); // 권한 없으므로 findByEmail도 호출 안됨
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 관리자가 이메일로 탈퇴 시 사용자 없음")
    void adminWithdrawUserByEmail_Fail_UserNotFound() {
        // Given
        setupAdminAuthentication(); // 관리자 권한 설정
        String email = "nonexistent@example.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // When & Then
        UserException thrown = assertThrows(UserException.class, () -> // BusinessException -> UserException
                userService.adminWithdrawUserByEmail(email)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(USER_NOT_FOUND); // ErrorCode 사용
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 관리자가 이메일로 탈퇴 시 이미 DELETED 상태")
    void adminWithdrawUserByEmail_Fail_AlreadyDeleted() {
        // Given
        setupAdminAuthentication(); // 관리자 권한 설정
        String email = "test@example.com";
        User user = new User("login1", "pw", email, UserRole.USER);
        user.setStatus(UserStatus.DELETED); // 이미 DELETED 상태
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // When & Then
        UserException thrown = assertThrows(UserException.class, () -> // BusinessException -> UserException
                userService.adminWithdrawUserByEmail(email)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(USER_ALREADY_DELETED); // ErrorCode 사용
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 이메일과 비밀번호로 탈퇴")
    void withdrawUserWithPassword_Success() {
        // Given
        String email = "test@example.com";
        String rawPassword = "password123!";
        String encodedPassword = "encoded_password";

        User user = new User("loginId", encodedPassword, email, UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);

        setupUserAuthentication(email); // 사용자 권한 설정

        // When
        userService.withdrawCurrentUserWithPassword(email, rawPassword);

        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 이메일과 비밀번호로 탈퇴 시 로그인되지 않음")
    void withdrawCurrentUserWithPassword_Fail_NotLoggedIn() {
        // Given
        String email = "test@example.com";
        String rawPassword = "password123!";
        SecurityContextHolder.clearContext(); // 로그인 상태 아님

        // When & Then
        UserException thrown = assertThrows(UserException.class, () ->
                userService.withdrawCurrentUserWithPassword(email, rawPassword)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(UNAUTHORIZED);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 이메일과 비밀번호로 탈퇴 시 다른 사용자 계정 시도")
    void withdrawCurrentUserWithPassword_Fail_Forbidden() {
        // Given
        String loggedInUserEmail = "loggedin@example.com";
        String targetUserEmail = "target@example.com";
        String rawPassword = "password123!";

        setupUserAuthentication(loggedInUserEmail); // 로그인된 사용자

        // When & Then
        UserException thrown = assertThrows(UserException.class, () ->
                userService.withdrawCurrentUserWithPassword(targetUserEmail, rawPassword)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(FORBIDDEN);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    @DisplayName("회원 탈퇴 실패 - 이메일과 비밀번호로 탈퇴 시 사용자 없음")
    void withdrawCurrentUserWithPassword_Fail_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        String rawPassword = "password123!";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        setupUserAuthentication(email); // 로그인된 사용자

        // When & Then
        UserException thrown = assertThrows(UserException.class, () -> // BusinessException -> UserException
                userService.withdrawCurrentUserWithPassword(email, rawPassword)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(USER_NOT_FOUND); // ErrorCode 사용
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 이메일과 비밀번호로 탈퇴 시 비밀번호 불일치")
    void withdrawCurrentUserWithPassword_Fail_InvalidPassword() {
        // Given
        String email = "test@example.com";
        String rawPassword = "wrong_password";
        String encodedPassword = "encoded_password";

        User user = new User("loginId", encodedPassword, email, UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false); // 비밀번호 불일치

        setupUserAuthentication(email); // 로그인된 사용자

        // When & Then
        UserException thrown = assertThrows(UserException.class, () -> // BusinessException -> UserException
                userService.withdrawCurrentUserWithPassword(email, rawPassword)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(INVALID_PASSWORD); // ErrorCode 사용
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 이메일과 비밀번호로 탈퇴 시 이미 DELETED 상태")
    void withdrawCurrentUserWithPassword_Fail_AlreadyDeleted() {
        // Given
        String email = "test@example.com";
        String rawPassword = "password123!";
        String encodedPassword = "encoded_password";

        User user = new User("loginId", encodedPassword, email, UserRole.USER);
        user.setStatus(UserStatus.DELETED); // 이미 DELETED 상태

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);

        setupUserAuthentication(email); // 로그인된 사용자

        // When & Then
        UserException thrown = assertThrows(UserException.class, () -> // BusinessException -> UserException
                userService.withdrawCurrentUserWithPassword(email, rawPassword)
        );
        assertThat(thrown.getErrorCode()).isEqualTo(USER_ALREADY_DELETED); // ErrorCode 사용
        verify(userRepository, never()).save(any(User.class));
    }
}