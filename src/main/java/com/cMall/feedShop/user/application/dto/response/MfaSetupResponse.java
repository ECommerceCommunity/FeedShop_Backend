package com.cMall.feedShop.user.application.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MfaSetupResponse {
    private String secret;
    private String qrUrl;
    private String qrCodeImage; // Base64 인코딩된 QR 코드 이미지
    private List<String> backupCodes;
    private String message;
}
