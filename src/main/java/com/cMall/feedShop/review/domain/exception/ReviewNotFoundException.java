package com.cMall.feedShop.review.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class ReviewNotFoundException extends BusinessException {

    public ReviewNotFoundException() {
        super(ErrorCode.valueOf("REVIEW_NOT_FOUND"), "리뷰를 찾을 수 없습니다.");
    }

    public ReviewNotFoundException(String message) {
        super(ErrorCode.valueOf("REVIEW_NOT_FOUND"), message);
    }
}