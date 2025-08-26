package com.cMall.feedShop.user.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

/**
 * MFA 관련 예외 클래스
 */
public class MfaException extends BusinessException {

    public MfaException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MfaException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public MfaException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public MfaException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
