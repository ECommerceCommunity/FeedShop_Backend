package com.cMall.feedShop.review.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class DuplicateReviewException extends BusinessException {

    public DuplicateReviewException() {
        super(ErrorCode.valueOf("DUPLICATE_REVIEW"), "이미 해당 상품에 대한 리뷰를 작성하셨습니다.");
    }
}