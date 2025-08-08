package com.cMall.feedShop.common.captcha;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.response.RecaptchaResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Service
@Profile("prod")
public class GoogleRecaptchaVerificationService implements RecaptchaVerificationService {
 
    @Value("${recaptcha.secret-key}")
    private String secretKey;

    @Value("${recaptcha.score-threshold:0.5}") // 기본 임계값 0.5
    private double scoreThreshold;

    private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RestTemplate restTemplate;

    public GoogleRecaptchaVerificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void verify(String recaptchaToken, String expectedAction) {

        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            throw new BusinessException(ErrorCode.RECAPTCHA_VERIFICATION_FAILED, "reCAPTCHA 토큰이 없습니다.");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", secretKey);
        params.add("response", recaptchaToken);

        RecaptchaResponse response = restTemplate.postForObject(GOOGLE_RECAPTCHA_VERIFY_URL, params, RecaptchaResponse.class);

        if (response == null || !response.isSuccess()) {
            log.warn("reCAPTCHA API 검증 실패: {}", response != null ? (response.getErrorCodes() != null ? response.getErrorCodes() : Collections.emptyList()) : "응답 없음");
            throw new BusinessException(ErrorCode.RECAPTCHA_VERIFICATION_FAILED);
        }

        if (!expectedAction.equals(response.getAction())) {
            log.warn("reCAPTCHA action 불일치. 기대값: {}, 실제값: {}", expectedAction, response.getAction());
            throw new BusinessException(ErrorCode.RECAPTCHA_VERIFICATION_FAILED, "reCAPTCHA action이 일치하지 않습니다.");
        }

        if (response.getScore() < scoreThreshold) {
            log.warn("reCAPTCHA 점수 낮음. 점수: {}, 임계값: {}", response.getScore(), scoreThreshold);
            throw new BusinessException(ErrorCode.RECAPTCHA_SCORE_TOO_LOW);
        }

        log.info("reCAPTCHA 검증 성공. 점수: {}, 액션: {}", response.getScore(), response.getAction());
    }
}