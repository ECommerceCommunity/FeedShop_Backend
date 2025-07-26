package com.cMall.feedShop.feed.domain.exception;

import com.cMall.feedShop.common.exception.ErrorCode;

public class EventNotAvailableException extends FeedException {
    
    public EventNotAvailableException(Long eventId) {
        super(ErrorCode.EVENT_NOT_AVAILABLE, "참여할 수 없는 이벤트입니다. eventId: " + eventId);
    }
    
    public EventNotAvailableException(Long eventId, String reason) {
        super(ErrorCode.EVENT_NOT_AVAILABLE, "참여할 수 없는 이벤트입니다. eventId: " + eventId + ", 이유: " + reason);
    }
} 