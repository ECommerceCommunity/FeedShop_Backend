package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.response.SocialLoginResponse;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserSocialProvider;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.repository.UserSocialProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 소셜 로그인 관련 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAuthServiceImpl implements SocialAuthService {

    private final UserRepository userRepository;
    private final UserSocialProviderRepository socialProviderRepository;

    @Override
    public SocialLoginResponse getUserSocialProviders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        List<UserSocialProvider> socialProviders = socialProviderRepository.findByUser(user);
        
        List<SocialLoginResponse.SocialProviderInfo> providerInfos = socialProviders.stream()
            .map(sp -> new SocialLoginResponse.SocialProviderInfo(
                sp.getProvider(),
                sp.getSocialEmail(),
                sp.getConnectedAt()
            ))
            .collect(Collectors.toList());

        return new SocialLoginResponse(
            userEmail,
            providerInfos,
            !socialProviders.isEmpty()
        );
    }

    @Override
    @Transactional
    public void unlinkProvider(String userEmail, String provider) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        UserSocialProvider socialProvider = socialProviderRepository.findByUserAndProvider(user, provider)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "연동되지 않은 소셜 제공자입니다."));

        // 마지막 로그인 방법인지 확인 (소셜 로그인만 있는 경우)
        List<UserSocialProvider> allProviders = socialProviderRepository.findByUser(user);
        if (allProviders.size() == 1 && user.getPassword().equals("SOCIAL_LOGIN")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                "마지막 로그인 방법은 해제할 수 없습니다. 다른 로그인 방법을 먼저 설정해주세요.");
        }

        socialProviderRepository.delete(socialProvider);
        user.removeSocialProvider(socialProvider);
        
        log.info("소셜 제공자 연동 해제: user={}, provider={}", userEmail, provider);
    }

    @Override
    public boolean isProviderLinked(String userEmail, String provider) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        return socialProviderRepository.existsByUserAndProvider(user, provider);
    }
}
