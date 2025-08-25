package com.cMall.feedShop.user.domain.enums;

public enum MfaType {
    TOTP,    // Time-based One-Time Password (Google Authenticator)
    SMS,     // SMS 인증 (향후 확장)
    EMAIL,   // 이메일 인증 (향후 확장)
    HARDWARE // 하드웨어 키 (향후 확장)
}
