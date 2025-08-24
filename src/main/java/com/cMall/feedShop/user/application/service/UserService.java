package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.request.UserSignUpRequest;
import com.cMall.feedShop.user.application.dto.response.UserResponse;
import com.cMall.feedShop.user.domain.exception.UserException; // UserException은 필수
import com.cMall.feedShop.user.domain.model.DailyPoints;
import com.cMall.feedShop.user.domain.model.User;

import java.time.LocalDateTime;
import java.util.List;

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

    List<UserResponse> findByUsernameAndPhoneNumber(String username, String phoneNumber);

    List<DailyPoints> getDailyPointsStatisticsForUser(User user, LocalDateTime startDate);
}

