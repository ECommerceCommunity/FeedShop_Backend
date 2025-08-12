package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.captcha.GoogleRecaptchaVerificationService;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.response.RecaptchaResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleRecaptchaVerificationServiceTest {

    private GoogleRecaptchaVerificationService googleRecaptchaVerificationService;
    @Mock
    private RestTemplate mockRestTemplate;

    private final String testToken = "test-recaptcha-token";
    private final String testAction = "login_submit";
    private final String testSecretKey = "test-secret-key";
    private final double testScoreThreshold = 0.5;

    @BeforeEach
    void setUp() {
        googleRecaptchaVerificationService = new GoogleRecaptchaVerificationService(mockRestTemplate);

        // @Value로 주입되는 필드들을 직접 설정
        ReflectionTestUtils.setField(googleRecaptchaVerificationService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(googleRecaptchaVerificationService, "scoreThreshold", testScoreThreshold);
    }

    private RecaptchaResponse createMockResponse(boolean success, double score, String action) {
        RecaptchaResponse response = new RecaptchaResponse();
        response.setSuccess(success);
        response.setScore(score);
        response.setAction(action);
        response.setErrorCodes(Collections.emptyList()); // errorCodes 초기화
        return response;
    }

    @Test
    @DisplayName("reCAPTCHA 검증 성공")
    void verify_success() {
        // given
        RecaptchaResponse mockResponse = createMockResponse(true, 0.9, testAction);

        when(mockRestTemplate.postForObject(any(String.class), any(MultiValueMap.class), eq(RecaptchaResponse.class)))
                .thenReturn(mockResponse);

        // when & then
        assertDoesNotThrow(() -> googleRecaptchaVerificationService.verifyRecaptcha(testToken, testAction));
    }

    @Test
    @DisplayName("reCAPTCHA 검증 실패 - 토큰 없음")
    void verify_fail_noToken() {
        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            googleRecaptchaVerificationService.verifyRecaptcha(null, testAction);
        });
        assertEquals(ErrorCode.RECAPTCHA_VERIFICATION_FAILED, exception.getErrorCode());
    }

    @Test
    @DisplayName("reCAPTCHA 검증 실패 - API 응답 실패")
    void verify_fail_apiError() {
        // given
        RecaptchaResponse mockResponse = createMockResponse(false, 0.0, null);

        when(mockRestTemplate.postForObject(any(String.class), any(MultiValueMap.class), eq(RecaptchaResponse.class)))
                .thenReturn(mockResponse);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            googleRecaptchaVerificationService.verifyRecaptcha(testToken, testAction);
        });
        assertEquals(ErrorCode.RECAPTCHA_VERIFICATION_FAILED, exception.getErrorCode());
    }

    @Test
    @DisplayName("reCAPTCHA 검증 실패 - Action 불일치")
    void verify_fail_actionMismatch() {
        // given
        RecaptchaResponse mockResponse = createMockResponse(true, 0.9, "different_action");

        when(mockRestTemplate.postForObject(any(String.class), any(MultiValueMap.class), eq(RecaptchaResponse.class)))
                .thenReturn(mockResponse);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            googleRecaptchaVerificationService.verifyRecaptcha(testToken, testAction);
        });
        assertEquals(ErrorCode.RECAPTCHA_VERIFICATION_FAILED, exception.getErrorCode());
    }

    @Test
    @DisplayName("reCAPTCHA 검증 실패 - 점수 미달")
    void verify_fail_scoreTooLow() {
        // given
        RecaptchaResponse mockResponse = createMockResponse(true, 0.4, testAction);

        when(mockRestTemplate.postForObject(any(String.class), any(MultiValueMap.class), eq(RecaptchaResponse.class)))
                .thenReturn(mockResponse);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            googleRecaptchaVerificationService.verifyRecaptcha(testToken, testAction);
        });
        assertEquals(ErrorCode.RECAPTCHA_SCORE_TOO_LOW, exception.getErrorCode());
    }
}