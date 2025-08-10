package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.model.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private UserRole role; 
    private UserStatus status;
    private LocalDateTime createdAt;
    private String message;

    public static UserResponse from(User user) {
        String profileName = null;
        String profilePhone = null;
        if (user.getUserProfile() != null) {
            profileName = user.getUserProfile().getName();
            profilePhone = user.getUserProfile().getPhone();
        }

        return UserResponse.builder()
                .userId(user.getId())
                .username(profileName)
                .email(user.getEmail())
                .phone(profilePhone)
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static UserResponse from(User user, String message) {
        String profileName = null;
        String profilePhone = null;
        if (user.getUserProfile() != null) {
            profileName = user.getUserProfile().getName();
            profilePhone = user.getUserProfile().getPhone();
        }

        return UserResponse.builder()
                .userId(user.getId())
                .username(profileName)
                .email(user.getEmail())
                .phone(profilePhone)
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .message(message)
                .build();
    }
}
