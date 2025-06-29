package com.cMall.feedShop.user.presentation; // 현재 패키지 유지, 필요시 com.cMall.feedShop.auth.presentation으로 변경 권장

import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;

import com.cMall.feedShop.user.application.dto.request.UserSignUpRequest;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.application.dto.response.UserResponse;
import com.cMall.feedShop.user.application.service.UserAuthService;
import com.cMall.feedShop.user.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; // Lombok 임포트
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth") // 인증 관련 엔드포인트 기본 경로
@RequiredArgsConstructor // final 필드를 인자로 받는 생성자를 자동 생성
public class UserAuthController {

    private final UserService userService; // 회원가입 등 기본적인 사용자 CRUD를 담당하는 서비스
    private final UserAuthService userAuthService; // 로그인 등 인증 관련 로직을 담당하는 서비스

    // @RequiredArgsConstructor가 생성자를 자동으로 만들어주므로 수동 생성자 삭제
    // public AuthController(UserService userService, UserAuthService userAuthService) {
    //     this.userService = userService;
    //     this.userAuthService = userAuthService;
    // }

    @PostMapping("/signup") // POST /api/auth/signup 요청 처리
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody UserSignUpRequest request) {
        // 회원가입은 UserService에 위임 (사용자 생성 로직)
        return ResponseEntity.ok(userService.signUp(request));
    }

    @PostMapping("/login") // POST /api/auth/login 요청 처리 (React 코드와 일치)
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        // 로그인 인증은 AuthService에 위임
        return ResponseEntity.ok(userAuthService.login(request));
    }
}
