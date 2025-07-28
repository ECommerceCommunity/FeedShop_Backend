package com.cMall.feedShop.event.application.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

/**
 * 이벤트를 찾을 수 없을 때 발생하는 예외
 */
public class EventNotFoundException extends BusinessException {
    
    public EventNotFoundException() {
        super(ErrorCode.EVENT_NOT_FOUND);
    }
    
    public EventNotFoundException(String message) {
        super(ErrorCode.EVENT_NOT_FOUND, message);
    }
    
    public EventNotFoundException(Long eventId) {
        super(ErrorCode.EVENT_NOT_FOUND, "Event not found with id: " + eventId);
    }
} 