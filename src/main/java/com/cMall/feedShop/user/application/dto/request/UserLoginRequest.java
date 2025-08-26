package com.cMall.feedShop.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Getter
@Setter
@Schema(description = "로그인 요청 DTO")
public class UserLoginRequest {
    @Schema(description = "이메일 주소", example = "user@example.com", required = true)
    @NotNull
    private String email;
    
    @Schema(description = "비밀번호", example = "password123!", required = true)
    @NotNull
    private String password;
    
    @Schema(description = "reCAPTCHA 토큰", example = "03AFcWeA...", required = true)
    @NotNull
    private String recaptchaToken;
}
