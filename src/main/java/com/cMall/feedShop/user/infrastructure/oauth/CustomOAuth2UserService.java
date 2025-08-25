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
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest); // Network call is now outside the transaction
        try {
            // Call the new transactional method
            return processAndSaveOAuth2User(userRequest, oAuth2User);
        } catch (Exception e) {
            log.error("OAuth2 사용자 처리 중 오류 발생", e);
            throw new OAuth2AuthenticationException("OAuth2 사용자 처리 실패");
        }
    }

    @Transactional
    public OAuth2User processAndSaveOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // 1. Get user info (same as before)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        String email = oAuth2UserInfo.getEmail();
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("소셜 로그인 제공자에서 이메일을 가져올 수 없습니다.");
        }

        // 2. Find or create user (move your existing logic here)
        UserSocialProvider socialProvider = socialProviderRepository
                .findByProviderAndProviderSocialUserId(registrationId, oAuth2UserInfo.getId())
                .orElse(null);

        User user;
        if (socialProvider != null) {
            user = handleExistingSocialUser(socialProvider, oAuth2UserInfo);
        } else {
            user = handleNewSocialUser(oAuth2UserInfo, registrationId);
        }

        // 3. Return a CustomOAuth2User object (same as before)
        return new CustomOAuth2User(
                oAuth2User,
                registrationId,
                oAuth2UserInfo.getId(),
                oAuth2UserInfo.getEmail(),
                oAuth2UserInfo.getName()
        );
    }

    // 기존 소셜 로그인 사용자를 처리하는 메서드
    private User handleExistingSocialUser(UserSocialProvider socialProvider, OAuth2UserInfo oAuth2UserInfo) {
        User user = socialProvider.getUser();
        if (!oAuth2UserInfo.getEmail().equals(socialProvider.getSocialEmail())) {
            socialProvider.updateSocialInfo(oAuth2UserInfo.getEmail());
            socialProviderRepository.save(socialProvider);
        }
        return user;
    }

    // 새로운 소셜 로그인 사용자를 처리하는 메서드
    private User handleNewSocialUser(OAuth2UserInfo oAuth2UserInfo, String registrationId) {
        // 1. 이메일로 기존 사용자 확인 (없으면 새로 생성)
        User user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
                .orElseGet(() -> createNewUser(oAuth2UserInfo));

        // 2. 새로운 소셜 로그인 제공자 정보 추가
        UserSocialProvider newSocialProvider = new UserSocialProvider(
                user,
                registrationId,
                oAuth2UserInfo.getId(),
                oAuth2UserInfo.getEmail()
        );
        socialProviderRepository.save(newSocialProvider);
        user.addSocialProvider(newSocialProvider);

        return user;
    }

    private User createNewUser(OAuth2UserInfo oAuth2UserInfo) {
        log.info("새로운 소셜 로그인 사용자 생성: email={}", oAuth2UserInfo.getEmail());

        String loginId = "";
        int maxAttempts = 10; // 최대 시도 횟수

        for (int i = 0; i < maxAttempts; i++) {
            loginId = "social_" + UUID.randomUUID().toString().substring(0, 8);
            if (!userRepository.existsByLoginId(loginId)) {
                break;
            }
            if (i == maxAttempts - 1) {
                throw new IllegalStateException("고유한 로그인 ID를 생성하는 데 실패했습니다.");
            }
        }

        User newUser = new User(
                loginId,
                oAuth2UserInfo.getEmail(),
                UserRole.USER
        );
        return userRepository.save(newUser);
    }
}
