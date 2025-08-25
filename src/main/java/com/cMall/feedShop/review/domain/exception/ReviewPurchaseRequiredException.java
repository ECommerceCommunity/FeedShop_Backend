package com.cMall.feedShop.review.domain.exception;

public class ReviewPurchaseRequiredException extends RuntimeException {
    
    public ReviewPurchaseRequiredException(String message) {
        super(message);
    }
    
    public ReviewPurchaseRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}