package com.cMall.feedShop.user.infrastructure.oauth;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

import java.util.Map;

/**
 * OAuth2 제공자별 사용자 정보 객체를 생성하는 팩토리 클래스
 * 제공자에 따라 적절한 OAuth2UserInfo 구현체를 반환
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return new GoogleOAuth2UserInfo(attributes);
            case "kakao":
                return new KakaoOAuth2UserInfo(attributes);
            case "naver":
                return new NaverOAuth2UserInfo(attributes);
            default:
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
        }
    }
}
