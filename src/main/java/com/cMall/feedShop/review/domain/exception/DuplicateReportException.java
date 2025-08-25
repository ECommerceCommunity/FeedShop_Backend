package com.cMall.feedShop.review.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class DuplicateReportException extends BusinessException {
    public DuplicateReportException() {
        super(ErrorCode.DUPLICATE_REPORT);
    }
    
    public DuplicateReportException(String message) {
        super(ErrorCode.DUPLICATE_REPORT, message);
    }
}