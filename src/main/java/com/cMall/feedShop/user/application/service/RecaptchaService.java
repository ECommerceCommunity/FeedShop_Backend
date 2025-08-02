package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.response.RecaptchaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class RecaptchaService {

    @Value("${recaptcha.secret-key}")
    private String secretKey;

    private static final String GOOGLE_RECAPTCHA_VERIFY_URL =
            "https://www.google.com/recaptcha/api/siteverify";

    public boolean verifyRecaptcha(String recaptchaToken) {
        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            return false;
        }

        RestTemplate restTemplate = new RestTemplate();

        // 구글 API에 보낼 파라미터 구성
        String url = GOOGLE_RECAPTCHA_VERIFY_URL +
                "?secret=" + secretKey +
                "&response=" + recaptchaToken;

        // POST 요청 보내고 응답 받기
        RecaptchaResponse response = restTemplate.postForObject(url, Collections.emptyList(), RecaptchaResponse.class);

        // 응답에서 success 필드 확인
        return response != null && response.isSuccess();
    }
}