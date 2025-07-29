package com.cMall.feedShop.user.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class AccountNotVerifiedException extends BusinessException {
  public AccountNotVerifiedException() {
    super(ErrorCode.ACCOUNT_NOT_VERIFIED);
  }

  public AccountNotVerifiedException(String message) {
    super(ErrorCode.ACCOUNT_NOT_VERIFIED, message);
  }
}
