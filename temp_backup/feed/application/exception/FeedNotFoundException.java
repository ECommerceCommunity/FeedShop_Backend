package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class FeedNotFoundException extends BusinessException {
    
    public FeedNotFoundException(Long feedId) {
        super(ErrorCode.FEED_NOT_FOUND, "피드를 찾을 수 없습니다. feedId: " + feedId);
    }
} 