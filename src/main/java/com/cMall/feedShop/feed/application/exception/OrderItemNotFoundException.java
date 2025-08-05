package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class OrderItemNotFoundException extends BusinessException {
    
    public OrderItemNotFoundException(Long orderItemId) {
        super(ErrorCode.ORDER_ITEM_NOT_FOUND, "주문 상품을 찾을 수 없습니다. orderItemId: " + orderItemId);
    }
} 