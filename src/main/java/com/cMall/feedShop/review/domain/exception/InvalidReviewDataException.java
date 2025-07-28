package com.cMall.feedShop.review.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class InvalidReviewDataException extends BusinessException {

    public InvalidReviewDataException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }
}