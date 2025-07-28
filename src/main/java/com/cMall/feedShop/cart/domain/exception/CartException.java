package com.cMall.feedShop.cart.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class CartException {
    public static class CartZeroQuantityException extends BusinessException {
        public CartZeroQuantityException() {
            super(ErrorCode.ZERO_QUANTITY);
        }
    }

    public static class CartItemNotFoundException extends BusinessException {
        public CartItemNotFoundException() {
            super(ErrorCode.CART_ITEM_NOT_FOUND);
        }
    }
}
