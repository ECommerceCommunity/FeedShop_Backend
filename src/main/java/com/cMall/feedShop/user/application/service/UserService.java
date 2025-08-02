package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.request.UserSignUpRequest;
import com.cMall.feedShop.user.application.dto.response.UserResponse;
import com.cMall.feedShop.user.domain.model.User;

public interface UserService {
    UserResponse signUp(UserSignUpRequest request);

    void updateVerificationToken(User user);

    void sendVerificationEmail(User user, String subject, String contentBody);

    boolean isEmailDuplicated(String email);

    void verifyEmail(String token);

    void deleteUser(User user);

    void checkAdminAuthority(String methodName);

    void withdrawUser(Long userId);

    void adminWithdrawUserByEmail(String email);

    void withdrawCurrentUserWithPassword(String email, String rawPassword);

    UserResponse findByUsernameAndPhoneNumber(String username, String phoneNumber);
}

