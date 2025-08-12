package com.cMall.feedShop.user.application.dto.request;

import com.cMall.feedShop.annotation.CustomEncryption;
import com.cMall.feedShop.annotation.CustomPasswordMatch;
import com.cMall.feedShop.user.domain.enums.UserRole;
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
public class UserSignUpRequest {
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
    private String name;

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @Size(max = 255, message = "이메일은 255자 이하로 입력해주세요.")
    private String email;

    private String loginId;

    @CustomEncryption
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 255, message = "비밀번호는 8자 이상으로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(?=.*[0-9]).*$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수 입력 값입니다.")
    private String confirmPassword; // 비밀번호 확인 필드

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    private String phone;

    private String nickname;

    private UserRole role = UserRole.USER;
}
