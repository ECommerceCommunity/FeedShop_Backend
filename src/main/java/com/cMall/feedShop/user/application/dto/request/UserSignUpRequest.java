package com.cMall.feedShop.user.application.dto.request;

import com.cMall.feedShop.annotation.CustomEncryption;
import com.cMall.feedShop.annotation.CustomPasswordMatch;
import com.cMall.feedShop.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CustomPasswordMatch
@Schema(description = "회원가입 요청 DTO")
public class UserSignUpRequest {
    @Schema(description = "실명 (2-50자)", example = "홍길동", required = true)
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
    private String name;

    @Schema(description = "이메일 주소", example = "user@example.com", required = true)
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @Size(max = 255, message = "이메일은 255자 이하로 입력해주세요.")
    private String email;

    @Schema(description = "로그인 ID (자동 생성됨)", example = "user123")
    private String loginId;

    @Schema(description = "비밀번호 (영문, 숫자, 특수문자 포함, 8자 이상)", example = "password123!", required = true)
    @CustomEncryption
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 255, message = "비밀번호는 8자 이상으로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(?=.*[0-9]).*$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @Schema(description = "비밀번호 확인", example = "password123!", required = true)
    @NotBlank(message = "비밀번호 확인은 필수 입력 값입니다.")
    private String confirmPassword;

    @Schema(description = "전화번호", example = "010-1234-5678", required = true)
    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    private String phone;

    @Schema(description = "닉네임", example = "길동이")
    private String nickname;

    @Schema(description = "사용자 역할", example = "USER")
    private UserRole role = UserRole.USER;
}
