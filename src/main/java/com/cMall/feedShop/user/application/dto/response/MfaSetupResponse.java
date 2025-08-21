package com.cMall.feedShop.user.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MfaSetupResponse {
    private String secret;
    private String qrUrl;
    private String qrImage;
}
