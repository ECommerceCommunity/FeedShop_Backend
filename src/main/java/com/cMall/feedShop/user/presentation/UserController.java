package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.user.application.dto.response.UserProfileResponse;
import com.cMall.feedShop.user.application.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users") // 사용자 리소스에 대한 기본 경로
@RequiredArgsConstructor
public class UserController {
    private final UserProfileService userProfileService;
    // UserService 주입 제거 (signup이 AuthController로 이동했으므로)

    // 사용자 프로필을 조회하는 예시 메서드
    @GetMapping("/{userId}/profile")
    public UserProfileResponse getUserProfile(@PathVariable Long userId) {
        // userProfileService를 사용하여 실제 비즈니스 로직 호출
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return response;
    }

    // JWT 로그인 메서드도 있다면 여기에 추가
    // @PostMapping("/login")
    // public AuthTokenResponse login(@RequestBody UserLoginRequest request) {
    //     return userService.login(request);
    // }
}