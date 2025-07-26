package com.cMall.feedShop.feed.domain.exception;

import com.cMall.feedShop.common.exception.ErrorCode;

public class FeedAccessDeniedException extends FeedException {
    
    public FeedAccessDeniedException(Long feedId, Long userId) {
        super(ErrorCode.FEED_ACCESS_DENIED, 
              "해당 피드에 대한 권한이 없습니다. feedId: " + feedId + ", userId: " + userId);
    }
} 