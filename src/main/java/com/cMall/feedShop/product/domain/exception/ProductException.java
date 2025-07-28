package com.cMall.feedShop.product.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class ProductException {
    public static class ProductNotFoundException extends BusinessException {
        public ProductNotFoundException() {
            super(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    public static class CategoryNotFoundException extends BusinessException {
        public CategoryNotFoundException() {
            super(ErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    public static class OutOfStockException extends BusinessException {
        public OutOfStockException() {
            super(ErrorCode.OUT_OF_STOCK);
        }
    }

    public static class ProductInOrderException extends BusinessException {
        public ProductInOrderException() { super(ErrorCode.PRODUCT_IN_ORDER); }
    }

    public static class ProductOptionNotFoundException extends BusinessException {
        public ProductOptionNotFoundException() {
            super(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }
    }

    public static class ProductImageNotFoundException extends BusinessException {
        public ProductImageNotFoundException() {
            super(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        }
    }

    public static class DuplicateProductNameException extends BusinessException {
        public DuplicateProductNameException() {
            super(ErrorCode.DUPLICATE_PRODUCT_NAME);
        }
    }
}
