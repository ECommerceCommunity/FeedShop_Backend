package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.response.MfaSetupResponse;

public interface MfaService {
    MfaSetupResponse setupMfa(String email);
    boolean verifyMfaToken(String secret, int token);
    String generateQRCode(String qrUrl) throws Exception;
}
