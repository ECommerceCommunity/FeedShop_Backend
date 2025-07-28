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

    // User 엔티티를 UserResponse DTO로 변환하는 정적 팩토리 메서드
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static UserResponse from(User user, String message) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus()) // Enum을 String으로 변환하여 반환
                .createdAt(user.getCreatedAt())
                .message(message)
                .build();
    }
}
