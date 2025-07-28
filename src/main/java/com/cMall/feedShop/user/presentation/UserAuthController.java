package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;

import com.cMall.feedShop.user.application.dto.request.UserSignUpRequest;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.application.dto.response.UserResponse;
import com.cMall.feedShop.user.application.service.UserAuthService;
import com.cMall.feedShop.user.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; // Lombok 임포트
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;
    private final UserAuthService userAuthService;

    @PostMapping("/signup")
    @ApiResponseFormat(message = "회원가입이 성공적으로 완료되었습니다.")
    public ApiResponse<UserResponse> signUp(@Valid @RequestBody UserSignUpRequest request) {
        return ApiResponse.success(userService.signUp(request));
    }

    @PostMapping("/login")
    @ApiResponseFormat(message = "로그인이 성공적으로 완료되었습니다.")
    public ApiResponse<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ApiResponse.success(userAuthService.login(request));
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
}
