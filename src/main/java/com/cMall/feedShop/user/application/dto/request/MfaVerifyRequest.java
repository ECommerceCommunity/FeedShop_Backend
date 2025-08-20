package com.cMall.feedShop.user.application.dto.request;

import lombok.Data;

@Data
public class MfaVerifyRequest {
    private int token;
    private String email;
}
