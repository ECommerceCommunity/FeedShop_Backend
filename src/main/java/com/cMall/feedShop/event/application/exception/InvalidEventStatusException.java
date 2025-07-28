package com.cMall.feedShop.event.application.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

/**
 * 유효하지 않은 이벤트 상태일 때 발생하는 예외
 */
public class InvalidEventStatusException extends BusinessException {
    
    public InvalidEventStatusException() {
        super(ErrorCode.INVALID_EVENT_STATUS);
    }
    
    public InvalidEventStatusException(String message) {
        super(ErrorCode.INVALID_EVENT_STATUS, message);
    }
    
    public InvalidEventStatusException(String status, String reason) {
        super(ErrorCode.INVALID_EVENT_STATUS, "Invalid event status: " + status + ". " + reason);
    }
} 