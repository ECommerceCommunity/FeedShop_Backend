package com.cMall.feedShop.feed.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class FeedException extends BusinessException {
    
    public FeedException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public FeedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
} 