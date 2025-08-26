package com.cMall.feedShop.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그에서 민감한 정보를 마스킹하는 유틸리티 클래스
 */
@Slf4j
public class LogMaskingUtil {

    private static final String MASK_CHAR = "*";
    private static final int MIN_VISIBLE_CHARS = 2;

    /**
     * 이메일 주소를 마스킹합니다.
     * 예: user@example.com → u***@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= MIN_VISIBLE_CHARS) {
            return localPart.charAt(0) + MASK_CHAR + "@" + domain;
        } else {
            return localPart.charAt(0) + MASK_CHAR.repeat(localPart.length() - 2) + 
                   localPart.charAt(localPart.length() - 1) + "@" + domain;
        }
    }

    /**
     * 전화번호를 마스킹합니다.
     * 예: 010-1234-5678 → 010-****-5678
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return phoneNumber;
        }

        // 하이픈 제거 후 마스킹
        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");
        if (cleanNumber.length() < 4) {
            return phoneNumber;
        }

        // 마지막 4자리만 보이고 나머지는 마스킹
        String masked = MASK_CHAR.repeat(cleanNumber.length() - 4) + 
                       cleanNumber.substring(cleanNumber.length() - 4);

        // 원래 형식에 맞게 하이픈 추가
        if (phoneNumber.contains("-")) {
            if (masked.length() == 11) {
                return masked.substring(0, 3) + "-" + masked.substring(3, 7) + "-" + masked.substring(7);
            } else if (masked.length() == 10) {
                return masked.substring(0, 3) + "-" + masked.substring(3, 6) + "-" + masked.substring(6);
            }
        }

        return masked;
    }

    /**
     * 토큰을 마스킹합니다.
     * 예: abc123def456 → abc***def456
     */
    public static String maskToken(String token) {
        if (token == null || token.length() < 6) {
            return token;
        }

        int visibleChars = Math.min(3, token.length() / 3);
        return token.substring(0, visibleChars) + 
               MASK_CHAR.repeat(token.length() - visibleChars * 2) + 
               token.substring(token.length() - visibleChars);
    }

    /**
     * MFA 토큰을 마스킹합니다 (6자리 숫자).
     * 예: 123456 → 12****
     */
    public static String maskMfaToken(String token) {
        if (token == null || token.length() != 6) {
            return token;
        }

        return token.substring(0, 2) + MASK_CHAR.repeat(4);
    }

    /**
     * 백업 코드를 마스킹합니다 (8자리 숫자).
     * 예: 12345678 → 12****78
     */
    public static String maskBackupCode(String code) {
        if (code == null || code.length() != 8) {
            return code;
        }

        return code.substring(0, 2) + MASK_CHAR.repeat(4) + code.substring(6);
    }

    /**
     * 사용자 ID를 마스킹합니다.
     * 예: 12345 → 1****
     */
    public static String maskUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        String userIdStr = userId.toString();
        if (userIdStr.length() <= 1) {
            return userIdStr;
        }

        return userIdStr.charAt(0) + MASK_CHAR.repeat(userIdStr.length() - 1);
    }

    /**
     * 일반적인 민감 정보를 마스킹합니다.
     */
    public static String maskSensitiveInfo(String info, String type) {
        if (info == null) {
            return null;
        }

        switch (type.toLowerCase()) {
            case "email":
                return maskEmail(info);
            case "phone":
                return maskPhoneNumber(info);
            case "token":
                return maskToken(info);
            case "mfa":
                return maskMfaToken(info);
            case "backup":
                return maskBackupCode(info);
            default:
                return info;
        }
    }
}
