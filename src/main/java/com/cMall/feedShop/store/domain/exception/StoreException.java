package com.cMall.feedShop.store.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class StoreException {
    public static class StoreForbiddenException extends BusinessException {
        public StoreForbiddenException() {
            super(ErrorCode.STORE_FORBIDDEN);
        }
    }

    public static class StoreNotFoundException extends BusinessException {
        public StoreNotFoundException() {
            super(ErrorCode.STORE_NOT_FOUND);
        }
    }
}
