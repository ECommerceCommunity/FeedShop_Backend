package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.request.UserLoginRequest;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;

public interface UserAuthService {
    UserLoginResponse login(UserLoginRequest request);

    void requestPasswordReset(String email);

    void validatePasswordResetToken(String tokenValue);

    void resetPassword(String tokenValue, String newPassword);
}
