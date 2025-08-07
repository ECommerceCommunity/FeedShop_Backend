package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

/**
 * 피드 접근 권한이 없을 때 발생하는 예외
 * 마이피드 목록 조회 시 사용자 권한 확인에 사용
 */
public class FeedAccessDeniedException extends BusinessException {
    
    public FeedAccessDeniedException(Long userId) {
        super(ErrorCode.FEED_ACCESS_DENIED, "피드 접근 권한이 없습니다. userId: " + userId);
    }
    
    public FeedAccessDeniedException(String message) {
        super(ErrorCode.FEED_ACCESS_DENIED, message);
    }
    
    public FeedAccessDeniedException(Long userId, String message) {
        super(ErrorCode.FEED_ACCESS_DENIED, "피드 접근 권한이 없습니다. userId: " + userId + ", " + message);
    }
} 