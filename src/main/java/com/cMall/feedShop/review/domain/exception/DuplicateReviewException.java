package com.cMall.feedShop.review.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class DuplicateReviewException extends BusinessException {

    public DuplicateReviewException() {
        super(ErrorCode.DUPLICATE_REVIEW, "이미 해당 상품에 대한 리뷰를 작성하셨습니다.");
    }

    public DuplicateReviewException(String message) {
        super(ErrorCode.DUPLICATE_REVIEW, message);
    }

    public DuplicateReviewException(Long productId) {
        super(ErrorCode.DUPLICATE_REVIEW,
                String.format("상품 ID %d에 대한 리뷰를 이미 작성하셨습니다.", productId));
    }
}