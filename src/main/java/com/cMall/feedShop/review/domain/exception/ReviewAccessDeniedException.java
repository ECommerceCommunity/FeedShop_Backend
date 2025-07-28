package com.cMall.feedShop.review.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class ReviewAccessDeniedException extends BusinessException {

    public ReviewAccessDeniedException() {
        super(ErrorCode.FORBIDDEN, "리뷰에 대한 권한이 없습니다.");
    }
}