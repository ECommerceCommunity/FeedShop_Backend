package com.cMall.feedShop.user.infrastructure.oauth;

import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserSocialProvider;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.repository.UserSocialProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * OAuth2 사용자 정보를 처리하는 커스텀 서비스
 * 소셜 로그인 시 사용자 정보를 가져와서 회원가입 또는 로그인 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserSocialProviderRepository socialProviderRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 부모 클래스의 메서드를 호출하여 OAuth2User 객체를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception e) {
            log.error("OAuth2 사용자 처리 중 오류 발생", e);
            throw new OAuth2AuthenticationException("OAuth2 사용자 처리 실패");
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // 제공자 정보 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        // 제공자별 사용자 정보 추출
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
            registrationId, 
            oAuth2User.getAttributes()
        );

        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("소셜 로그인 제공자에서 이메일을 가져올 수 없습니다.");
        }

        // 소셜 로그인 제공자 정보로 사용자 찾기
        UserSocialProvider socialProvider = socialProviderRepository
            .findByProviderAndProviderSocialUserId(registrationId, oAuth2UserInfo.getId())
            .orElse(null);

        User user;
        if (socialProvider != null) {
            // 기존 소셜 로그인 사용자
            user = socialProvider.getUser();
            // 소셜 이메일이 변경된 경우 업데이트
            if (!oAuth2UserInfo.getEmail().equals(socialProvider.getSocialEmail())) {
                socialProvider.updateSocialInfo(oAuth2UserInfo.getEmail());
                socialProviderRepository.save(socialProvider);
            }
        } else {
            // 새로운 소셜 로그인 - 이메일로 기존 사용자 확인
            user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
                .orElseGet(() -> createNewUser(oAuth2UserInfo));
            
            // 새로운 소셜 로그인 제공자 정보 추가
            UserSocialProvider newSocialProvider = new UserSocialProvider(
                user, 
                registrationId, 
                oAuth2UserInfo.getId(), 
                oAuth2UserInfo.getEmail()
            );
            socialProviderRepository.save(newSocialProvider);
            user.addSocialProvider(newSocialProvider);
        }

        return new CustomOAuth2User(
            oAuth2User,
            registrationId,
            oAuth2UserInfo.getId(),
            oAuth2UserInfo.getEmail(),
            oAuth2UserInfo.getName()
        );
    }

    private User createNewUser(OAuth2UserInfo oAuth2UserInfo) {
        log.info("새로운 소셜 로그인 사용자 생성: email={}", oAuth2UserInfo.getEmail());
        
        // 고유한 loginId 생성 (소셜 로그인용)
        String loginId = "social_" + UUID.randomUUID().toString().substring(0, 8);
        
        // loginId 중복 체크
        while (userRepository.existsByLoginId(loginId)) {
            loginId = "social_" + UUID.randomUUID().toString().substring(0, 8);
        }

        User newUser = new User(
            loginId,
            oAuth2UserInfo.getEmail(),
            UserRole.USER
        );

        return userRepository.save(newUser);
    }
}
