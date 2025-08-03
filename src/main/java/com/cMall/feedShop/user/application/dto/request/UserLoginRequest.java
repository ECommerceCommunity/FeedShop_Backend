package com.cMall.feedShop.user.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class UserLoginRequest {
    @NotNull
    private String email;
    @NotNull
    private String password;
    @NotNull
    private String recaptchaToken;
}
