package com.cMall.feedShop.user.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import lombok.Getter;


@Getter
public class UserAddressException extends BusinessException {
    public UserAddressException() {
        super(ErrorCode.ADDRESS_NOT_FOUND);
    }
}