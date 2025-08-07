package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

/**
 * 피드를 찾을 수 없을 때 발생하는 예외
 */
public class FeedNotFoundException extends BusinessException {
    
    public FeedNotFoundException(Long feedId) {
        super(ErrorCode.FEED_NOT_FOUND, "피드를 찾을 수 없습니다. feedId: " + feedId);
    }
    
    public FeedNotFoundException(String message) {
        super(ErrorCode.FEED_NOT_FOUND, message);
    }
    
    public FeedNotFoundException(Long feedId, String message) {
        super(ErrorCode.FEED_NOT_FOUND, "피드를 찾을 수 없습니다. feedId: " + feedId + ", " + message);
    }
} 