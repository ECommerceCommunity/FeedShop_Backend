package com.cMall.feedShop.user.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 소셜 로그인 정보 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginResponse {
    
    private String userEmail;
    private List<SocialProviderInfo> connectedProviders;
    private boolean hasSocialLogin;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialProviderInfo {
        private String provider;
        private String socialEmail;
        private LocalDateTime connectedAt;
    }
}
