package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.captcha.RecaptchaVerificationService;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.request.EmailRequest;
import com.cMall.feedShop.user.application.dto.request.PasswordResetConfirmRequest;
import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;
import com.cMall.feedShop.user.application.dto.request.UserSignUpRequest;
import com.cMall.feedShop.user.application.dto.response.MfaStatusResponse;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.application.dto.response.UserResponse;
import com.cMall.feedShop.common.captcha.GoogleRecaptchaVerificationService;
import com.cMall.feedShop.user.application.service.MfaService;
import com.cMall.feedShop.user.application.service.UserAuthService;
import com.cMall.feedShop.user.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "인증", description = "회원가입, 로그인, 비밀번호 재설정 등 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;
    private final UserAuthService userAuthService;
    private final RecaptchaVerificationService recaptchaService;
    private final MfaService mfaService;

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다. 이메일 인증이 필요하며, 인증 완료 후 로그인이 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력 값 오류, 이메일 형식 오류 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 이메일"
            )
    })
    @PostMapping("/signup")
    @ApiResponseFormat(message = "회원가입이 성공적으로 완료되었습니다.")
    public ApiResponse<UserResponse> signUp(
            @Parameter(description = "회원가입 요청 데이터", required = true)
            @Valid @RequestBody UserSignUpRequest request) {
        return ApiResponse.success(userService.signUp(request));
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인합니다. MFA가 활성화된 경우 2단계 인증이 필요할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = UserLoginResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (reCAPTCHA 토큰 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패 (이메일/비밀번호 불일치, 이메일 미인증)"
            )
    })
    @PostMapping("/login")
    @ApiResponseFormat(message = "로그인이 성공적으로 완료되었습니다.")
    public ApiResponse<UserLoginResponse> login(
            @Parameter(description = "로그인 요청 데이터 (이메일, 비밀번호, reCAPTCHA 토큰)", required = true)
            @Valid @RequestBody UserLoginRequest request) {
        recaptchaService.verifyRecaptcha(request.getRecaptchaToken(), "login_submit");

        UserLoginResponse loginResponse = userAuthService.login(request);
        MfaStatusResponse mfaStatus = mfaService.getMfaStatus(request.getEmail());

        if (mfaStatus != null && mfaStatus.isEnabled()) {
            UserLoginResponse mfaRequiredResponse = UserLoginResponse.builder()
                    .loginId(loginResponse.getLoginId())
                    .role(loginResponse.getRole())
                    .nickname(loginResponse.getNickname())
                    .tempToken(loginResponse.getToken())
                    .email(request.getEmail())
                    .requiresMfa(true)
                    .build();
            return ApiResponse.success(mfaRequiredResponse);
        } else {
            return ApiResponse.success(loginResponse);
        }
    }

    @Operation(
            summary = "이메일 인증",
            description = "회원가입 시 발송된 이메일의 인증 링크를 통해 이메일을 인증합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이메일 인증 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 인증 토큰"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "인증 토큰을 찾을 수 없음"
            )
    })
    @GetMapping("/verify-email")
    @ApiResponseFormat(message = "이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다.")
    public ApiResponse<String> verifyEmail(
            @Parameter(description = "이메일 인증 토큰", required = true, example = "abc123-def456-ghi789")
            @RequestParam("token") String token) {
        userService.verifyEmail(token);
        return ApiResponse.success("이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다.");
    }

    @Operation(
            summary = "계정 찾기",
            description = "이름과 전화번호로 등록된 계정을 찾습니다. 이메일 주소가 마스킹되어 반환됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "계정 찾기 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 정보로 등록된 계정을 찾을 수 없음"
            )
    })
    @GetMapping("/find-account")
    @ApiResponseFormat(message = "계정을 성공적으로 찾았습니다.")
    public ApiResponse<List<UserResponse>> findAccountByNameAndPhone(
            @Parameter(description = "사용자 이름", required = true, example = "홍길동")
            @RequestParam("username") String username,
            @Parameter(description = "전화번호", required = true, example = "010-1234-5678")
            @RequestParam("phoneNumber") String phoneNumber) {
        List<UserResponse> accounts = userService.findByUsernameAndPhoneNumber(username, phoneNumber);
        return ApiResponse.success(accounts);
    }

    @Operation(
            summary = "비밀번호 재설정 요청",
            description = "이메일로 비밀번호 재설정 링크를 발송합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 재설정 이메일 발송 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 이메일로 등록된 계정을 찾을 수 없음"
            )
    })
    @PostMapping("/forgot-password")
    @ApiResponseFormat(message = "비밀번호 재설정 이메일이 발송되었습니다.")
    public ApiResponse<String> forgotPassword(
            @Parameter(description = "비밀번호 재설정을 요청할 이메일", required = true)
            @Valid @RequestBody EmailRequest request) {
        userAuthService.requestPasswordReset(request.getEmail());
        return ApiResponse.success("비밀번호 재설정 이메일이 발송되었습니다.");
    }

    @Operation(
            summary = "비밀번호 재설정 토큰 검증",
            description = "비밀번호 재설정 이메일의 토큰이 유효한지 검증합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 검증 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 토큰"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토큰을 찾을 수 없음"
            )
    })
    @GetMapping("/reset-password/validate")
    @ApiResponseFormat(message = "토큰이 유효합니다.")
    public ApiResponse<String> validatePasswordResetToken(
            @Parameter(description = "비밀번호 재설정 토큰", required = true, example = "abc123-def456-ghi789")
            @RequestParam("token") String token) {
        userAuthService.validatePasswordResetToken(token);
        return ApiResponse.success("토큰이 유효합니다.");
    }

    @Operation(
            summary = "비밀번호 재설정",
            description = "유효한 토큰과 새로운 비밀번호로 비밀번호를 재설정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 재설정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (토큰 오류, 비밀번호 형식 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토큰을 찾을 수 없음"
            )
    })
    @PostMapping("/reset-password")
    @ApiResponseFormat(message = "비밀번호가 성공적으로 재설정되었습니다.")
    public ApiResponse<String> resetPassword(
            @Parameter(description = "비밀번호 재설정 요청 데이터", required = true)
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        userAuthService.resetPassword(request.getToken(), request.getNewPassword());
        return ApiResponse.success("비밀번호가 성공적으로 재설정되었습니다.");
    }
}