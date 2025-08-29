package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.captcha.RecaptchaVerificationService;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.exception.GlobalExceptionHandler;
import com.cMall.feedShop.user.application.dto.request.EmailRequest;
import com.cMall.feedShop.user.application.dto.request.PasswordResetConfirmRequest;
import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;
import com.cMall.feedShop.user.application.dto.request.UserSignUpRequest;
import com.cMall.feedShop.user.application.dto.response.MfaStatusResponse;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.application.dto.response.UserResponse;
import com.cMall.feedShop.user.application.service.MfaService;
import com.cMall.feedShop.user.application.service.UserAuthService;
import com.cMall.feedShop.user.application.service.UserService;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAuthController 테스트")
class UserAuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserAuthService userAuthService;

    @Mock
    private RecaptchaVerificationService recaptchaService;

    @Mock
    private MfaService mfaService;

    @InjectMocks
    private UserAuthController userAuthController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userAuthController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void signUp_Success() throws Exception {
        // given
        UserSignUpRequest request = new UserSignUpRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123!");
        request.setConfirmPassword("password123!");
        request.setName("테스트 사용자");
        request.setPhone("010-1234-5678");

        UserResponse expectedResponse = UserResponse.builder()
                .userId(1L)
                .email("test@example.com")
                .username("테스트 사용자")
                .role(UserRole.USER)
                .build();

        given(userService.signUp(any(UserSignUpRequest.class)))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.username").value("테스트 사용자"));

        verify(userService, times(1)).signUp(any(UserSignUpRequest.class));
    }

    @Test
    @DisplayName("회원가입 - 실패 (잘못된 요청)")
    void signUp_Failure_InvalidRequest() throws Exception {
        // given
        UserSignUpRequest request = new UserSignUpRequest();
        // 필수 필드 누락

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signUp(any(UserSignUpRequest.class));
    }

    @Test
    @DisplayName("로그인 - 성공 (MFA 비활성화)")
    void login_Success_WithoutMfa() throws Exception {
        // given
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRecaptchaToken("valid-token");

        UserLoginResponse loginResponse = UserLoginResponse.builder()
                .loginId("testuser")
                .role(UserRole.USER)
                .nickname("테스트")
                .token("jwt-token")
                .build();

        MfaStatusResponse mfaStatus = MfaStatusResponse.builder()
                .enabled(false)
                .email("test@example.com")
                .build();

        doNothing().when(recaptchaService).verifyRecaptcha(anyString(), anyString());
        given(userAuthService.login(any(UserLoginRequest.class)))
                .willReturn(loginResponse);
        given(mfaService.getMfaStatus(anyString()))
                .willReturn(mfaStatus);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value("testuser"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.requiresMfa").value(false));

        verify(recaptchaService, times(1)).verifyRecaptcha(anyString(), anyString());
        verify(userAuthService, times(1)).login(any(UserLoginRequest.class));
        verify(mfaService, times(1)).getMfaStatus(anyString());
    }

    @Test
    @DisplayName("로그인 - 성공 (MFA 활성화)")
    void login_Success_WithMfa() throws Exception {
        // given
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRecaptchaToken("valid-token");

        UserLoginResponse loginResponse = UserLoginResponse.builder()
                .loginId("testuser")
                .role(UserRole.USER)
                .nickname("테스트")
                .token("jwt-token")
                .build();

        MfaStatusResponse mfaStatus = MfaStatusResponse.builder()
                .enabled(true)
                .email("test@example.com")
                .build();

        doNothing().when(recaptchaService).verifyRecaptcha(anyString(), anyString());
        given(userAuthService.login(any(UserLoginRequest.class)))
                .willReturn(loginResponse);
        given(mfaService.getMfaStatus(anyString()))
                .willReturn(mfaStatus);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value("testuser"))
                .andExpect(jsonPath("$.data.requiresMfa").value(true))
                .andExpect(jsonPath("$.data.tempToken").value("jwt-token"));

        verify(recaptchaService, times(1)).verifyRecaptcha(anyString(), anyString());
        verify(userAuthService, times(1)).login(any(UserLoginRequest.class));
        verify(mfaService, times(1)).getMfaStatus(anyString());
    }

    @Test
    @DisplayName("로그인 - 실패 (reCAPTCHA 검증 실패)")
    void login_Failure_RecaptchaVerificationFailed() throws Exception {
        // given
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRecaptchaToken("invalid-token");

        doThrow(new BusinessException(ErrorCode.RECAPTCHA_VERIFICATION_FAILED))
                .when(recaptchaService).verifyRecaptcha(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(recaptchaService, times(1)).verifyRecaptcha(anyString(), anyString());
        verify(userAuthService, never()).login(any(UserLoginRequest.class));
    }

    @Test
    @DisplayName("이메일 인증 - 성공")
    void verifyEmail_Success() throws Exception {
        // given
        String token = "valid-email-token";
        doNothing().when(userService).verifyEmail(anyString());

        // when & then
        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다."));

        verify(userService, times(1)).verifyEmail(token);
    }

    @Test
    @DisplayName("이메일 인증 - 실패 (잘못된 토큰)")
    void verifyEmail_Failure_InvalidToken() throws Exception {
        // given
        String token = "invalid-token";
        doThrow(new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN))
                .when(userService).verifyEmail(anyString());

        // when & then
        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", token))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).verifyEmail(token);
    }

    @Test
    @DisplayName("계정 찾기 - 성공")
    void findAccountByNameAndPhone_Success() throws Exception {
        // given
        String username = "홍길동";
        String phoneNumber = "010-1234-5678";

        List<UserResponse> expectedAccounts = Arrays.asList(
                UserResponse.builder()
                        .userId(1L)
                        .email("h***@example.com")
                        .username("홍길동")
                        .role(UserRole.USER)
                        .build()
        );

        given(userService.findByUsernameAndPhoneNumber(anyString(), anyString()))
                .willReturn(expectedAccounts);

        // when & then
        mockMvc.perform(get("/api/auth/find-account")
                        .param("username", username)
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value(1))
                .andExpect(jsonPath("$.data[0].email").value("h***@example.com"))
                .andExpect(jsonPath("$.data[0].username").value("홍길동"));

        verify(userService, times(1)).findByUsernameAndPhoneNumber(username, phoneNumber);
    }

    @Test
    @DisplayName("계정 찾기 - 실패 (계정 없음)")
    void findAccountByNameAndPhone_Failure_AccountNotFound() throws Exception {
        // given
        String username = "존재하지않는사용자";
        String phoneNumber = "010-9999-9999";

        given(userService.findByUsernameAndPhoneNumber(anyString(), anyString()))
                .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/auth/find-account")
                        .param("username", username)
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findByUsernameAndPhoneNumber(username, phoneNumber);
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 - 성공")
    void forgotPassword_Success() throws Exception {
        // given
        EmailRequest request = new EmailRequest();
        request.setEmail("test@example.com");

        doNothing().when(userAuthService).requestPasswordReset(anyString());

        // when & then
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("비밀번호 재설정 이메일이 발송되었습니다."));

        verify(userAuthService, times(1)).requestPasswordReset("test@example.com");
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 - 실패 (존재하지 않는 이메일)")
    void forgotPassword_Failure_EmailNotFound() throws Exception {
        // given
        EmailRequest request = new EmailRequest();
        request.setEmail("nonexistent@example.com");

        doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                .when(userAuthService).requestPasswordReset(anyString());

        // when & then
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(userAuthService, times(1)).requestPasswordReset("nonexistent@example.com");
    }

    @Test
    @DisplayName("비밀번호 재설정 토큰 검증 - 성공")
    void validatePasswordResetToken_Success() throws Exception {
        // given
        String token = "valid-reset-token";
        doNothing().when(userAuthService).validatePasswordResetToken(anyString());

        // when & then
        mockMvc.perform(get("/api/auth/reset-password/validate")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("토큰이 유효합니다."));

        verify(userAuthService, times(1)).validatePasswordResetToken(token);
    }

    @Test
    @DisplayName("비밀번호 재설정 토큰 검증 - 실패 (잘못된 토큰)")
    void validatePasswordResetToken_Failure_InvalidToken() throws Exception {
        // given
        String token = "invalid-reset-token";
        doThrow(new BusinessException(ErrorCode.INVALID_TOKEN))
                .when(userAuthService).validatePasswordResetToken(anyString());

        // when & then
        mockMvc.perform(get("/api/auth/reset-password/validate")
                        .param("token", token))
                .andExpect(status().isBadRequest());

        verify(userAuthService, times(1)).validatePasswordResetToken(token);
    }

    @Test
    @DisplayName("비밀번호 재설정 - 성공")
    void resetPassword_Success() throws Exception {
        // given
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken("valid-reset-token");
        request.setNewPassword("newPassword123");

        doNothing().when(userAuthService).resetPassword(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("비밀번호가 성공적으로 재설정되었습니다."));

        verify(userAuthService, times(1)).resetPassword("valid-reset-token", "newPassword123");
    }

    @Test
    @DisplayName("비밀번호 재설정 - 실패 (잘못된 토큰)")
    void resetPassword_Failure_InvalidToken() throws Exception {
        // given
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken("invalid-reset-token");
        request.setNewPassword("newPassword123");

        doThrow(new BusinessException(ErrorCode.INVALID_TOKEN))
                .when(userAuthService).resetPassword(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userAuthService, times(1)).resetPassword("invalid-reset-token", "newPassword123");
    }

    @Test
    @DisplayName("비밀번호 재설정 - 실패 (잘못된 요청)")
    void resetPassword_Failure_InvalidRequest() throws Exception {
        // given
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        // 필수 필드 누락

        // when & then
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userAuthService, never()).resetPassword(anyString(), anyString());
    }
}
