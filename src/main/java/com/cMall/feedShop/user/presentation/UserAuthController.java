package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.captcha.RecaptchaVerificationService;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.request.EmailRequest;
import com.cMall.feedShop.user.application.dto.request.PasswordResetConfirmRequest;
import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;

import com.cMall.feedShop.user.application.dto.request.UserSignUpRequest;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.application.dto.response.UserResponse;
import com.cMall.feedShop.common.captcha.GoogleRecaptchaVerificationService;
import com.cMall.feedShop.user.application.service.UserAuthService;
import com.cMall.feedShop.user.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;
    private final UserAuthService userAuthService;
    private final RecaptchaVerificationService recaptchaService;


    @PostMapping("/signup")
    @ApiResponseFormat(message = "회원가입이 성공적으로 완료되었습니다.")
    public ApiResponse<UserResponse> signUp(@Valid @RequestBody UserSignUpRequest request) {
        return ApiResponse.success(userService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(@Valid @RequestBody UserLoginRequest request) {
        recaptchaService.verifyRecaptcha(request.getRecaptchaToken(), "login_submit");
        return ResponseEntity.ok(ApiResponse.success(userAuthService.login(request)));
    }

    @GetMapping("/verify-email")
    @ApiResponseFormat(message = "이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다.") // AOP 적용
    public ApiResponse<String> verifyEmail(@RequestParam("token") String token) {
        userService.verifyEmail(token);
        // String을 반환하지만 AOP가 ApiResponse.success("이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다.", "이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다.")
        // 형태로 래핑할 것입니다. 만약 데이터 부분에 메시지 문자열을 다시 넣고 싶지 않다면,
        // ApiResponse.success(message)만 반환하도록 Aspect에서 로직을 조정해야 할 수도 있습니다.
        // 현재 Aspect 로직은 `ApiResponse.success(message, result)` 형태이므로 result가 "이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다." 문자열이 됩니다.
        // 만약 result가 String일 때 데이터를 null로 처리하고 싶다면 Aspect 로직을 수정해야 합니다.
        return ApiResponse.success("이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다.");
    }

    @GetMapping("/find-account")
    @ApiResponseFormat(message = "계정 조회 처리 완료.")
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAccountByNameAndPhone(
            @RequestParam("username") String username,
            @RequestParam("phoneNumber") String phoneNumber
    ) {
        List<UserResponse> accounts = userService.findByUsernameAndPhoneNumber(username, phoneNumber);

        return new ResponseEntity<>(ApiResponse.success("계정을 성공적으로 찾았습니다.", accounts), HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    @ApiResponseFormat(message="비밀번호 재설정 링크가 이메일로 전송되었습니다.")
    public ResponseEntity<ApiResponse<String>> findPassword(@RequestBody EmailRequest emailRequest) {
        userAuthService.requestPasswordReset(emailRequest.getEmail());
        return new ResponseEntity<>(ApiResponse.success("비밀번호 재설정 링크가 이메일로 전송되었습니다.", null), HttpStatus.OK);
    }

    // 1. 비밀번호 재설정 링크 클릭 시 토큰을 처리하는 엔드포인트
    // 이메일 링크를 통해 들어오는 GET 요청을 처리합니다.
    // 주로 토큰의 유효성을 검사하고, 유효하면 프론트엔드의 비밀번호 재설정 폼으로 리다이렉트합니다.
    @GetMapping("/reset-password")
    @ApiResponseFormat(message = "비밀번호 재설정 페이지로 이동합니다.") // AOP 메시지
    public ResponseEntity<ApiResponse<String>> showResetPasswordForm(@RequestParam("token") String token) {
        // 토큰 유효성만 검사하고, 실제 비밀번호 변경은 POST 엔드포인트에서 진행합니다.
        // userAuthService에 토큰 유효성 검사 메서드를 추가해야 합니다.
        userAuthService.validatePasswordResetToken(token); // 토큰 유효성 검사 (만료, 존재 여부 등)

        // 클라이언트를 프론트엔드의 비밀번호 재설정 폼 페이지로 리다이렉트하거나,
        // 성공 메시지를 반환하여 클라이언트가 해당 폼으로 이동하도록 유도합니다.
        // 여기서는 API 응답으로 처리하고, 프론트엔드가 이 응답을 받아 다음 단계를 진행한다고 가정합니다.
        // 만약 직접 리다이렉트가 필요하다면 ResponseEntity.status(HttpStatus.FOUND).location(URI.create("...")).build(); 사용
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 토큰이 유효합니다. 새 비밀번호를 입력해주세요.", token));
    }

    // 2. 새로운 비밀번호를 제출하여 실제로 비밀번호를 변경하는 엔드포인트
    // 프론트엔드에서 새 비밀번호와 토큰을 받아 비밀번호를 업데이트합니다.
    @PostMapping("/reset-password")
    @ApiResponseFormat(message = "비밀번호가 성공적으로 재설정되었습니다.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid PasswordResetConfirmRequest request) {
        // userAuthService에 실제 비밀번호 재설정 로직을 호출합니다.
        userAuthService.resetPassword(request.getToken(), request.getNewPassword());
        return new ResponseEntity<>(ApiResponse.success("비밀번호가 성공적으로 재설정되었습니다.", null), HttpStatus.OK);
    }
}