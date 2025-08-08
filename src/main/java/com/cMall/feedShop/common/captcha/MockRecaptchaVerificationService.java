package com.cMall.feedShop.common.captcha;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!prod") // prod 프로파일이 아닐 때만 이 빈이 등록됩니다.
public class MockRecaptchaVerificationService implements RecaptchaVerificationService {
    @Override
    public void verify(String recaptchaToken, String expectedAction) {
        // 개발 환경에서는 검증 로직을 건너뜁니다.
        log.warn("🚨 개발/테스트 환경에서는 reCAPTCHA 검증을 건너뜁니다.");
    }
}