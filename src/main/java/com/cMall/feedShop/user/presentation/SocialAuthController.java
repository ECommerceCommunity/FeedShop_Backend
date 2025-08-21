package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.response.SocialLoginResponse;
import com.cMall.feedShop.user.application.service.SocialAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 소셜 로그인 관련 컨트롤러
 * 소셜 로그인 정보 조회, 연동/해제 등의 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    /**
     * 현재 사용자의 소셜 로그인 연동 정보 조회
     */
    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> getSocialProviders() {
        // TODO: SecurityContextHolder에서 현재 사용자 정보 가져오기
        // 현재는 예시로 하드코딩
        String userEmail = "example@example.com"; // 실제로는 JWT에서 추출
        
        SocialLoginResponse response = socialAuthService.getUserSocialProviders(userEmail);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 소셜 제공자 연동 해제
     */
    @DeleteMapping("/providers/{provider}")
    public ResponseEntity<ApiResponse<String>> unlinkProvider(@PathVariable String provider) {
        // TODO: SecurityContextHolder에서 현재 사용자 정보 가져오기
        String userEmail = "example@example.com"; // 실제로는 JWT에서 추출
        
        socialAuthService.unlinkProvider(userEmail, provider);
        return ResponseEntity.ok(ApiResponse.success(provider + " 연동이 해제되었습니다."));
    }

    /**
     * 소셜 로그인 지원 제공자 목록 조회
     */
    @GetMapping("/supported-providers")
    public ResponseEntity<ApiResponse<String[]>> getSupportedProviders() {
        String[] providers = {"google", "kakao", "naver"};
        return ResponseEntity.ok(ApiResponse.success(providers));
    }
}