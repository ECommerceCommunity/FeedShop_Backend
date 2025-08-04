package com.cMall.feedShop.cart.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class CartException extends BusinessException {
    public CartException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CartException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
