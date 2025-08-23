package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.response.MfaSetupResponse;
import com.cMall.feedShop.user.application.dto.response.MfaStatusResponse;

import java.util.List;

public interface MfaService {
    MfaSetupResponse setupMfa(String email);
    boolean verifyMfaToken(String email, String token);
    boolean enableMfa(String email, String token);
    void disableMfa(String email);
    MfaStatusResponse getMfaStatus(String email);
    String generateQRCode(String qrUrl) throws Exception;
    List<String> generateBackupCodes();
    boolean verifyBackupCode(String email, String backupCode);
}
