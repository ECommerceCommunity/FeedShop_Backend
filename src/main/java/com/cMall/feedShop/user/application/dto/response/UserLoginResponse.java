package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
@Schema(description = "로그인 응답 DTO")
public class UserLoginResponse {
    @Schema(description = "로그인 ID", example = "user123")
    private String loginId; // 사용자의 로그인 ID
    
    @Schema(description = "사용자 역할", example = "USER")
    private UserRole role;
    
    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "닉네임", example = "길동이")
    private String nickname;
    
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "MFA 2단계 인증 필요 여부", example = "false")
    private boolean requiresMfa; // MFA 2단계 인증 필요 여부
    
    @Schema(description = "MFA 인증을 위한 임시 토큰", example = "temp_token_123")
    private String tempToken; // MFA 인증을 위한 임시 토큰
    
    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;
}
