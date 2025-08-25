package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.response.SocialLoginResponse;

/**
 * 소셜 로그인 관련 서비스 인터페이스
 */
public interface SocialAuthService {
    
    /**
     * 사용자의 소셜 로그인 연동 정보 조회
     */
    SocialLoginResponse getUserSocialProviders(String userEmail);
    
    /**
     * 특정 소셜 제공자 연동 해제
     */
    void unlinkProvider(String userEmail, String provider);
    
    /**
     * 소셜 제공자 연동 여부 확인
     */
    boolean isProviderLinked(String userEmail, String provider);
}
