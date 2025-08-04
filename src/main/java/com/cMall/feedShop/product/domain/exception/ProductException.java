package com.cMall.feedShop.product.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class ProductException extends BusinessException{
    public ProductException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ProductException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
