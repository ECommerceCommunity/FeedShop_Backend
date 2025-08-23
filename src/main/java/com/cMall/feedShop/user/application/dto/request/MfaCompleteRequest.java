package com.cMall.feedShop.user.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MfaCompleteRequest {
    private String email;
    private String token; // MFA 인증 코드
}
