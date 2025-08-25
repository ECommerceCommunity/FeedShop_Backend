package com.cMall.feedShop.ai.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class AIException extends BusinessException {
    public AIException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AIException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
