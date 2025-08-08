package com.cMall.feedShop.common.captcha;

public interface RecaptchaVerificationService {
    void verify(String recaptchaToken, String expectedAction);
}