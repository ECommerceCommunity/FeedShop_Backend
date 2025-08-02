package com.cMall.feedShop.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetConfirmRequest {
    @NotBlank(message = "토큰은 필수입니다.")
    private String token;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    // 여기에 필요에 따라 비밀번호 유효성 검사 어노테이션을 추가할 수 있습니다.
    // 예를 들어, @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하이어야 합니다.")
    // @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&+=]).{8,}$", message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword;
}
