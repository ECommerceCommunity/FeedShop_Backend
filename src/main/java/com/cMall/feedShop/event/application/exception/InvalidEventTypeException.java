package com.cMall.feedShop.event.application.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

/**
 * 유효하지 않은 이벤트 타입일 때 발생하는 예외
 */
public class InvalidEventTypeException extends BusinessException {
    
    public InvalidEventTypeException() {
        super(ErrorCode.INVALID_EVENT_TYPE);
    }
    
    public InvalidEventTypeException(String message) {
        super(ErrorCode.INVALID_EVENT_TYPE, message);
    }
    
    public InvalidEventTypeException(String type, String reason) {
        super(ErrorCode.INVALID_EVENT_TYPE, "Invalid event type: " + type + ". " + reason);
    }
} 