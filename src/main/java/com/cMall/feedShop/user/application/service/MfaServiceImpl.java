package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.response.MfaSetupResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class MfaServiceImpl implements MfaService {

    private final GoogleAuthenticator googleAuth = new GoogleAuthenticator();


    @Override
    public MfaSetupResponse setupMfa(String email) {
        GoogleAuthenticatorKey key = googleAuth.createCredentials();
        String secret = key.getKey();

        // QR 코드 URL 생성
        String qrUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                "YourAppName",
                email,
                key
        );

        return MfaSetupResponse.builder()
                .secret(secret)
                .qrUrl(qrUrl)
                .build();
    }

    @Override
    public boolean verifyMfaToken(String secret, int token) {
        return googleAuth.authorize(secret, token);
    }

    @Override
    public String generateQRCode(String qrUrl) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, 200, 200);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}
