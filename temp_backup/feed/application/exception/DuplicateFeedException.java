package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class DuplicateFeedException extends BusinessException {
    
    public DuplicateFeedException(Long orderItemId, Long userId) {
        super(ErrorCode.DUPLICATE_FEED, 
              "이미 해당 주문 상품에 대한 피드를 작성하셨습니다. orderItemId: " + orderItemId + ", userId: " + userId);
    }
} 