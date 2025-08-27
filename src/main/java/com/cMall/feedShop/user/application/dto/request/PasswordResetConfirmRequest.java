package com.cMall.feedShop.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "비밀번호 재설정 확인 요청 DTO")
public class PasswordResetConfirmRequest {
    @Schema(description = "비밀번호 재설정 토큰", example = "abc123-def456-ghi789", required = true)
    @NotBlank(message = "토큰은 필수입니다.")
    private String token;

    @Schema(description = "새 비밀번호 (영문, 숫자, 특수문자 포함, 8자 이상)", example = "newpassword123!", required = true)
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    private String newPassword;
}
