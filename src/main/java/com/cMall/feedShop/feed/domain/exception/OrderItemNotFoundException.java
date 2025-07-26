package com.cMall.feedShop.feed.domain.exception;

import com.cMall.feedShop.common.exception.ErrorCode;

public class OrderItemNotFoundException extends FeedException {
    
    public OrderItemNotFoundException(Long orderItemId) {
        super(ErrorCode.ORDER_ITEM_NOT_FOUND, "주문 상품을 찾을 수 없습니다. orderItemId: " + orderItemId);
    }
} 