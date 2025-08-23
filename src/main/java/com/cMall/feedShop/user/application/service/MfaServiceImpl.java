package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.response.MfaSetupResponse;
import com.cMall.feedShop.user.application.dto.response.MfaStatusResponse;
import com.cMall.feedShop.user.domain.enums.MfaType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserMfa;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.infrastructure.repository.UserMfaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MfaServiceImpl implements MfaService {

    private final GoogleAuthenticator googleAuth = new GoogleAuthenticator();
    private final UserRepository userRepository;
    private final UserMfaRepository userMfaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public MfaSetupResponse setupMfa(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            GoogleAuthenticatorKey key = googleAuth.createCredentials();
            String secret = key.getKey();

            // QR 코드 URL 생성
            String qrUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                    "cMall FeedShop",
                    email,
                    key
            );

            // QR 코드 이미지 생성
            String qrCodeImage = generateQRCode(qrUrl);

            // 백업 코드 생성
            List<String> backupCodes = generateBackupCodes();

            // 기존 MFA 설정이 있으면 업데이트, 없으면 새로 생성
            UserMfa userMfa = userMfaRepository.findByUser(user)
                    .orElse(UserMfa.builder()
                            .user(user)
                            .mfaType(MfaType.TOTP)
                            .isEnabled(false)
                            .tempSecretKey(secret)
                            .build());


            // if (기존 MFA 설정이 존재할 경우)
            if (userMfa.getId() != null) {
                userMfa.setTempSecret(secret);
                userMfa.setBackupCodes(objectMapper.writeValueAsString(backupCodes));
            }


            // 임시 시크릿과 백업 코드 설정
            userMfa.setTempSecret(secret);
            userMfa.setBackupCodes(objectMapper.writeValueAsString(backupCodes));

            userMfaRepository.save(userMfa);

            log.info("MFA 설정 완료 - 사용자: {}", email);

            return MfaSetupResponse.builder()
                    .secret(secret)
                    .qrUrl(qrUrl)
                    .qrCodeImage(qrCodeImage)
                    .backupCodes(backupCodes)
                    .message("Google Authenticator 앱에서 QR코드를 스캔한 후, 생성된 6자리 코드로 인증을 완료하세요.")
                    .build();

        } catch (Exception e) {
            log.error("MFA 설정 중 오류 발생 - 사용자: {}, 오류: {}", email, e.getMessage());
            throw new RuntimeException("MFA 설정 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public boolean verifyMfaToken(String email, String token) {
        try {
            Optional<UserMfa> userMfaOpt = userMfaRepository.findByUserEmail(email);

            if (userMfaOpt.isEmpty()) {
                return false;
            }

            UserMfa userMfa = userMfaOpt.get();

            // 숫자 토큰인지 확인하고 TOTP 검증
            try {
                int tokenCode = Integer.parseInt(token);
                String secretToUse = userMfa.getActiveSecret();

                if (secretToUse != null) {
                    boolean isValid = googleAuth.authorize(secretToUse, tokenCode);
                    if (isValid) {
                        log.info("MFA TOTP 인증 성공 - 사용자: {}", email);
                        return true;
                    }
                }
            } catch (NumberFormatException e) {
                // 숫자가 아니면 백업 코드로 검증 시도
                log.debug("TOTP 형식이 아님, 백업 코드 검증 시도 - 사용자: {}", email);
            }

            // 백업 코드 검증
            return verifyBackupCode(email, token);

        } catch (Exception e) {
            log.error("MFA 토큰 검증 중 오류 발생 - 사용자: {}, 오류: {}", email, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean enableMfa(String email, String token) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            UserMfa userMfa = userMfaRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("MFA 설정을 찾을 수 없습니다. 먼저 MFA 설정을 진행해주세요."));

            // 임시 시크릿으로 토큰 검증
            if (userMfa.getTempSecretKey() != null) {
                try {
                    int tokenCode = Integer.parseInt(token);
                    if (googleAuth.authorize(userMfa.getTempSecretKey(), tokenCode)) {
                        // 검증 성공시 MFA 활성화
                        userMfa.enableMfa();
                        userMfaRepository.save(userMfa);

                        log.info("MFA 활성화 완료 - 사용자: {}", email);
                        return true;
                    }
                } catch (NumberFormatException e) {
                    log.warn("MFA 활성화 시 잘못된 토큰 형식 - 사용자: {}", email);
                }
            }

            return false;
        } catch (Exception e) {
            log.error("MFA 활성화 중 오류 발생 - 사용자: {}, 오류: {}", email, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void disableMfa(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            userMfaRepository.deleteByUser(user);
            log.info("MFA 비활성화 완료 - 사용자: {}", email);
        } catch (Exception e) {
            log.error("MFA 비활성화 중 오류 발생 - 사용자: {}, 오류: {}", email, e.getMessage());
            throw new RuntimeException("MFA 비활성화 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public MfaStatusResponse getMfaStatus(String email) {
        try {
            Optional<UserMfa> userMfaOpt = userMfaRepository.findByUserEmail(email);

            if (userMfaOpt.isPresent()) {
                UserMfa userMfa = userMfaOpt.get();
                return MfaStatusResponse.builder()
                        .enabled(userMfa.getIsEnabled())
                        .setupRequired(!userMfa.getIsEnabled() && userMfa.getTempSecretKey() != null)
                        .email(email)
                        .hasBackupCodes(userMfa.getBackupCodes() != null)
                        .mfaType(userMfa.getMfaType().name())
                        .build();
            }

            return MfaStatusResponse.builder()
                    .enabled(false)
                    .setupRequired(false)
                    .email(email)
                    .hasBackupCodes(false)
                    .mfaType(MfaType.TOTP.name())
                    .build();
        } catch (Exception e) {
            log.error("MFA 상태 조회 중 오류 발생 - 사용자: {}, 오류: {}", email, e.getMessage());
            throw new RuntimeException("MFA 상태 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public String generateQRCode(String qrUrl) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    @Override
    public List<String> generateBackupCodes() {
        SecureRandom random = new SecureRandom();
        return IntStream.range(0, 10)
                .mapToObj(i -> String.format("%08d", random.nextInt(100000000)))
                .toList();
    }

    @Override
    @Transactional
    public boolean verifyBackupCode(String email, String backupCode) {
        try {
            Optional<UserMfa> userMfaOpt = userMfaRepository.findByUserEmail(email);

            if (userMfaOpt.isEmpty()) {
                return false;
            }

            UserMfa userMfa = userMfaOpt.get();
            if (userMfa.getBackupCodes() == null) {
                return false;
            }

            List<String> backupCodes = objectMapper.readValue(
                    userMfa.getBackupCodes(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );

            if (backupCodes.contains(backupCode)) {
                // 사용된 백업 코드 제거
                backupCodes.remove(backupCode);
                userMfa.setBackupCodes(objectMapper.writeValueAsString(backupCodes));
                userMfaRepository.save(userMfa);

                log.info("백업 코드 인증 성공 - 사용자: {}", email);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("백업 코드 검증 중 오류 발생 - 사용자: {}, 오류: {}", email, e.getMessage());
            return false;
        }
    }
}
