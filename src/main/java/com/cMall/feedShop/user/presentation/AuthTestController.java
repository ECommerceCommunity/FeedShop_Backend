package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 소셜 로그인 테스트용 컨트롤러 (개발 환경에서만 활성화)
 * 실제 운영에서는 사용하지 않음
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/test")
@RequiredArgsConstructor
@Profile("dev")
public class AuthTestController {

    /**
     * 소셜 로그인 URL 생성 테스트
     */
    @GetMapping("/social-login-urls")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSocialLoginUrls() {
        Map<String, String> loginUrls = new HashMap<>();
        
        // 실제 운영에서는 환경변수나 설정에서 도메인을 가져와야 함
        String baseUrl = "https://localhost:8443";
        
        loginUrls.put("google", baseUrl + "/oauth2/authorization/google");
        loginUrls.put("kakao", baseUrl + "/oauth2/authorization/kakao");
        loginUrls.put("naver", baseUrl + "/oauth2/authorization/naver");
        
        return ResponseEntity.ok(ApiResponse.success(loginUrls));
    }

    /**
     * 현재 인증 상태 확인
     */

    @GetMapping("/auth-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuthStatus() {
        Map<String, Object> status = new HashMap<>();

        // SecurityContextHolder에서 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 사용자가 인증되었는지 확인
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            status.put("authenticated", true);

            // 사용자 이름(이메일 등) 가져오기
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                status.put("username", username);
            } else {
                status.put("username", authentication.getName());
            }

            status.put("message", "사용자가 인증되었습니다.");
        } else {
            status.put("authenticated", false);
            status.put("message", "인증되지 않은 사용자입니다.");
        }

        return ResponseEntity.ok(ApiResponse.success(status));
    }
    /**
     * OAuth2 콜백 테스트 정보
     */
    @GetMapping("/callback-info")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCallbackInfo() {
        Map<String, String> callbackInfo = new HashMap<>();
        callbackInfo.put("frontendCallbackUrl", "http://localhost:3000/auth/callback");
        callbackInfo.put("description", "소셜 로그인 성공 시 이 URL로 토큰과 함께 리다이렉트됩니다.");
        callbackInfo.put("parameters", "token, email, name, provider");
        
        return ResponseEntity.ok(ApiResponse.success(callbackInfo));
    }
}
