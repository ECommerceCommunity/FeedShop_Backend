package com.cMall.feedShop.store.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class StoreException extends BusinessException{
    public StoreException(ErrorCode errorCode) {
        super(errorCode);
    }

    public StoreException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
