package com.cMall.feedShop.common.captcha;

public interface RecaptchaVerificationService {
    void verifyRecaptcha(String recaptchaToken, String expectedAction);
}