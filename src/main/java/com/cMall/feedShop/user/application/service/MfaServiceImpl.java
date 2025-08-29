package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.util.LogMaskingUtil;
import com.cMall.feedShop.user.application.dto.response.MfaSetupResponse;
import com.cMall.feedShop.user.application.dto.response.MfaStatusResponse;
import com.cMall.feedShop.user.domain.enums.MfaType;
import com.cMall.feedShop.user.domain.exception.MfaException;
import com.cMall.feedShop.user.domain.exception.UserNotFoundException;
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
import org.springframework.transaction.annotation.Propagation;

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
        String maskedEmail = LogMaskingUtil.maskEmail(email);
        
        try {
            User user = findUserByEmail(email);
            
            // 이미 MFA가 활성화되어 있는지 확인
            Optional<UserMfa> existingMfa = userMfaRepository.findByUser(user);
            if (existingMfa.isPresent() && existingMfa.get().getIsEnabled()) {
                throw new MfaException(ErrorCode.MFA_ALREADY_ENABLED);
            }

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

            // MFA 설정 생성 또는 업데이트
            UserMfa userMfa = existingMfa.orElse(UserMfa.builder()
                    .user(user)
                    .mfaType(MfaType.TOTP)
                    .isEnabled(false)
                    .build());

            // 임시 시크릿과 백업 코드 설정
            userMfa.setTempSecret(secret);
            userMfa.setBackupCodes(objectMapper.writeValueAsString(backupCodes));

            userMfaRepository.save(userMfa);

            log.info("MFA 설정 완료 - 사용자: {}", maskedEmail);

            return MfaSetupResponse.builder()
                    .secret(secret)
                    .qrUrl(qrUrl)
                    .qrCodeImage(qrCodeImage)
                    .backupCodes(backupCodes)
                    .message("Google Authenticator 앱에서 QR코드를 스캔한 후, 생성된 6자리 코드로 인증을 완료하세요.")
                    .build();

        } catch (MfaException e) {
            log.warn("MFA 설정 실패 - 사용자: {}, 오류: {}", maskedEmail, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("MFA 설정 중 예상치 못한 오류 발생 - 사용자: {}, 오류: {}", maskedEmail, e.getMessage());
            throw new MfaException(ErrorCode.INTERNAL_SERVER_ERROR, "MFA 설정 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public boolean verifyMfaToken(String email, String token) {
        String maskedEmail = LogMaskingUtil.maskEmail(email);
        String maskedToken = LogMaskingUtil.maskMfaToken(token);
        
        try {
            UserMfa userMfa = findUserMfaByEmail(email);
            
            // TOTP 토큰 검증 시도
            if (isValidTotpToken(token)) {
                return verifyTotpToken(userMfa, token, maskedEmail, maskedToken);
            }
            
            // 백업 코드 검증 시도 - 새로운 트랜잭션으로 실행
            return verifyBackupCodeInNewTransaction(email, token);

        } catch (MfaException e) {
            log.warn("MFA 토큰 검증 실패 - 사용자: {}, 토큰: {}, 오류: {}", maskedEmail, maskedToken, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("MFA 토큰 검증 중 예상치 못한 오류 발생 - 사용자: {}, 토큰: {}, 오류: {}", 
                     maskedEmail, maskedToken, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean enableMfa(String email, String token) {
        String maskedEmail = LogMaskingUtil.maskEmail(email);
        String maskedToken = LogMaskingUtil.maskMfaToken(token);
        
        try {
            User user = findUserByEmail(email);
            UserMfa userMfa = findUserMfaByUser(user);

            // 임시 시크릿으로 토큰 검증
            if (userMfa.getTempSecretKey() == null) {
                throw new MfaException(ErrorCode.MFA_SETUP_REQUIRED);
            }

            if (!isValidTotpToken(token)) {
                log.warn("MFA 활성화 시 잘못된 토큰 형식 - 사용자: {}, 토큰: {}", maskedEmail, maskedToken);
                return false;
            }

            if (googleAuth.authorize(userMfa.getTempSecretKey(), Integer.parseInt(token))) {
                // 검증 성공시 MFA 활성화
                userMfa.enableMfa();
                userMfaRepository.save(userMfa);

                log.info("MFA 활성화 완료 - 사용자: {}", maskedEmail);
                return true;
            }

            log.warn("MFA 활성화 실패 - 잘못된 토큰 - 사용자: {}, 토큰: {}", maskedEmail, maskedToken);
            return false;

        } catch (MfaException e) {
            log.warn("MFA 활성화 실패 - 사용자: {}, 토큰: {}, 오류: {}", maskedEmail, maskedToken, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("MFA 활성화 중 예상치 못한 오류 발생 - 사용자: {}, 토큰: {}, 오류: {}", 
                     maskedEmail, maskedToken, e.getMessage());
            throw new MfaException(ErrorCode.INTERNAL_SERVER_ERROR, "MFA 활성화 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional
    public void disableMfa(String email) {
        String maskedEmail = LogMaskingUtil.maskEmail(email);
        
        try {
            User user = findUserByEmail(email);
            userMfaRepository.deleteByUser(user);
            
            log.info("MFA 비활성화 완료 - 사용자: {}", maskedEmail);
            
        } catch (Exception e) {
            log.error("MFA 비활성화 중 오류 발생 - 사용자: {}, 오류: {}", maskedEmail, e.getMessage());
            throw new MfaException(ErrorCode.INTERNAL_SERVER_ERROR, "MFA 비활성화 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public MfaStatusResponse getMfaStatus(String email) {
        String maskedEmail = LogMaskingUtil.maskEmail(email);
        
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
            log.error("MFA 상태 조회 중 오류 발생 - 사용자: {}, 오류: {}", maskedEmail, e.getMessage());
            throw new MfaException(ErrorCode.INTERNAL_SERVER_ERROR, "MFA 상태 조회 중 오류가 발생했습니다.", e);
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
        String maskedEmail = LogMaskingUtil.maskEmail(email);
        String maskedBackupCode = LogMaskingUtil.maskBackupCode(backupCode);
        
        try {
            UserMfa userMfa = findUserMfaByEmail(email);
            
            if (userMfa.getBackupCodes() == null) {
                log.warn("백업 코드가 설정되지 않음 - 사용자: {}", maskedEmail);
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

                log.info("백업 코드 인증 성공 - 사용자: {}, 남은 백업 코드 수: {}", maskedEmail, backupCodes.size());
                return true;
            }

            log.warn("백업 코드 인증 실패 - 잘못된 코드 - 사용자: {}, 코드: {}", maskedEmail, maskedBackupCode);
            return false;
            
        } catch (MfaException e) {
            log.warn("백업 코드 검증 실패 - 사용자: {}, 코드: {}, 오류: {}", maskedEmail, maskedBackupCode, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("백업 코드 검증 중 예상치 못한 오류 발생 - 사용자: {}, 코드: {}, 오류: {}", 
                     maskedEmail, maskedBackupCode, e.getMessage());
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean verifyBackupCodeInNewTransaction(String email, String backupCode) {
        String maskedEmail = LogMaskingUtil.maskEmail(email);
        String maskedBackupCode = LogMaskingUtil.maskBackupCode(backupCode);
        
        try {
            UserMfa userMfa = findUserMfaByEmail(email);
            
            if (userMfa.getBackupCodes() == null) {
                log.warn("백업 코드가 설정되지 않음 - 사용자: {}", maskedEmail);
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

                log.info("백업 코드 인증 성공 (새 트랜잭션) - 사용자: {}, 남은 백업 코드 수: {}", maskedEmail, backupCodes.size());
                return true;
            }

            log.warn("백업 코드 인증 실패 - 잘못된 코드 - 사용자: {}, 코드: {}", maskedEmail, maskedBackupCode);
            return false;
            
        } catch (MfaException e) {
            log.warn("백업 코드 검증 실패 - 사용자: {}, 코드: {}, 오류: {}", maskedEmail, maskedBackupCode, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("백업 코드 검증 중 예상치 못한 오류 발생 - 사용자: {}, 코드: {}, 오류: {}", 
                     maskedEmail, maskedBackupCode, e.getMessage());
            return false;
        }
    }

    // =========================== Private Helper Methods ===========================

    /**
     * 이메일로 사용자를 찾습니다.
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 이메일로 MFA 설정을 찾습니다.
     */
    private UserMfa findUserMfaByEmail(String email) {
        return userMfaRepository.findByUserEmail(email)
                .orElseThrow(() -> new MfaException(ErrorCode.MFA_NOT_FOUND));
    }

    /**
     * 사용자로 MFA 설정을 찾습니다.
     */
    private UserMfa findUserMfaByUser(User user) {
        return userMfaRepository.findByUser(user)
                .orElseThrow(() -> new MfaException(ErrorCode.MFA_NOT_FOUND));
    }

    /**
     * TOTP 토큰이 유효한 형식인지 확인합니다.
     */
    private boolean isValidTotpToken(String token) {
        try {
            int tokenCode = Integer.parseInt(token);
            return tokenCode >= 100000 && tokenCode <= 999999; // 6자리 숫자
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * TOTP 토큰을 검증합니다.
     */
    private boolean verifyTotpToken(UserMfa userMfa, String token, String maskedEmail, String maskedToken) {
        String secretToUse = userMfa.getActiveSecret();
        
        if (secretToUse == null) {
            log.warn("활성화된 MFA 시크릿이 없음 - 사용자: {}", maskedEmail);
            return false;
        }

        try {
            int tokenCode = Integer.parseInt(token);
            boolean isValid = googleAuth.authorize(secretToUse, tokenCode);
            
            if (isValid) {
                log.info("MFA TOTP 인증 성공 - 사용자: {}", maskedEmail);
                return true;
            } else {
                log.warn("MFA TOTP 인증 실패 - 잘못된 토큰 - 사용자: {}, 토큰: {}", maskedEmail, maskedToken);
                return false;
            }
        } catch (NumberFormatException e) {
            log.warn("MFA TOTP 토큰 형식 오류 - 사용자: {}, 토큰: {}", maskedEmail, maskedToken);
            return false;
        }
    }
}
