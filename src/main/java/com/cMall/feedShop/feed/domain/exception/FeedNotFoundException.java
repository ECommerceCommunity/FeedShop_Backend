package com.cMall.feedShop.feed.domain.exception;

import com.cMall.feedShop.common.exception.ErrorCode;

public class FeedNotFoundException extends FeedException {
    
    public FeedNotFoundException(Long feedId) {
        super(ErrorCode.FEED_NOT_FOUND, "피드를 찾을 수 없습니다. feedId: " + feedId);
    }
} 