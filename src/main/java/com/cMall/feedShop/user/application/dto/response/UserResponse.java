package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "사용자 응답 DTO")
public class UserResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "사용자 이름", example = "홍길동")
    private String username;
    
    @Schema(description = "이메일 주소 (마스킹됨)", example = "u***@example.com")
    private String email;
    
    @Schema(description = "전화번호", example = "010-****-5678")
    private String phone;
    
    @Schema(description = "사용자 역할", example = "USER")
    private UserRole role;
    
    @Schema(description = "사용자 상태", example = "ACTIVE")
    private UserStatus status;
    
    @Schema(description = "계정 생성일", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "응답 메시지", example = "계정을 성공적으로 찾았습니다.")
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
