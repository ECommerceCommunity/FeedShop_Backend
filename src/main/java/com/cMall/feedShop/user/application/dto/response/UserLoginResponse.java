package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UserLoginResponse {
    private String loginId; // 사용자의 로그인 ID
    private UserRole role;
    private String token;
    private String nickname;

    private boolean requiresMfa; // MFA 2단계 인증 필요 여부
    private String tempToken; // MFA 인증을 위한 임시 토큰
    private String email;
}
