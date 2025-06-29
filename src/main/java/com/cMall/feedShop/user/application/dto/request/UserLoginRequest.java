package com.cMall.feedShop.user.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class UserLoginRequest {
    private String email;
    private String password;
}
