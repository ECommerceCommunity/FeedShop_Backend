package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.store.domain.exception.StoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exception 클래스 테스트")
class ProductExceptionTest {

    @Test
    @DisplayName("BusinessException 생성 및 속성 확인")
    void businessException_Creation_Test() {
        // given & when
        BusinessException exception = new BusinessException(ErrorCode.USER_NOT_FOUND);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("BusinessException 커스텀 메시지 생성")
    void businessException_CustomMessage_Test() {
        // given & when
        String customMessage = "커스텀 에러 메시지";
        BusinessException exception = new BusinessException(ErrorCode.USER_NOT_FOUND, customMessage);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
    }

    @Test
    @DisplayName("ProductException 모든 서브클래스 테스트")
    void productException_AllSubclasses_Test() {
        // given & when
        ProductException.ProductNotFoundException productNotFound =
                new ProductException.ProductNotFoundException();
        ProductException.CategoryNotFoundException categoryNotFound =
                new ProductException.CategoryNotFoundException();
        ProductException.OutOfStockException outOfStock =
                new ProductException.OutOfStockException();
        ProductException.ProductInOrderException productInOrder =
                new ProductException.ProductInOrderException();
        ProductException.ProductOptionNotFoundException optionNotFound =
                new ProductException.ProductOptionNotFoundException();
        ProductException.ProductImageNotFoundException imageNotFound =
                new ProductException.ProductImageNotFoundException();

        // then
        assertThat(productNotFound.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        assertThat(categoryNotFound.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
        assertThat(outOfStock.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
        assertThat(productInOrder.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_IN_ORDER);
        assertThat(optionNotFound.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        assertThat(imageNotFound.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
    }

    @Test
    @DisplayName("StoreException 모든 서브클래스 테스트")
    void storeException_AllSubclasses_Test() {
        // given & when
        StoreException.StoreForbiddenException storeForbidden =
                new StoreException.StoreForbiddenException();
        StoreException.StoreNotFoundException storeNotFound =
                new StoreException.StoreNotFoundException();

        // then
        assertThat(storeForbidden.getErrorCode()).isEqualTo(ErrorCode.STORE_FORBIDDEN);
        assertThat(storeNotFound.getErrorCode()).isEqualTo(ErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("ErrorCode 모든 값 존재 확인")
    void errorCode_AllValues_Test() {
        // given & when
        ErrorCode[] allErrorCodes = ErrorCode.values();

        // then
        assertThat(allErrorCodes).hasSizeGreaterThan(20);
        assertThat(allErrorCodes).contains(
                ErrorCode.INVALID_INPUT_VALUE,
                ErrorCode.METHOD_NOT_ALLOWED,
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.UNAUTHORIZED,
                ErrorCode.FORBIDDEN,
                ErrorCode.USER_NOT_FOUND,
                ErrorCode.DUPLICATE_EMAIL,
                ErrorCode.INVALID_PASSWORD,
                ErrorCode.PRODUCT_NOT_FOUND,
                ErrorCode.CATEGORY_NOT_FOUND,
                ErrorCode.OUT_OF_STOCK,
                ErrorCode.STORE_FORBIDDEN,
                ErrorCode.STORE_NOT_FOUND,
                ErrorCode.ZERO_QUANTITY
        );
    }

    @Test
    @DisplayName("ErrorCode 속성 값 확인")
    void errorCode_Properties_Test() {
        // given
        ErrorCode userNotFound = ErrorCode.USER_NOT_FOUND;
        ErrorCode productNotFound = ErrorCode.PRODUCT_NOT_FOUND;
        ErrorCode zeroQuantity = ErrorCode.ZERO_QUANTITY;

        // when & then
        assertThat(userNotFound.getStatus()).isEqualTo(404);
        assertThat(userNotFound.getCode()).isEqualTo("U001");
        assertThat(userNotFound.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");

        assertThat(productNotFound.getStatus()).isEqualTo(404);
        assertThat(productNotFound.getCode()).isEqualTo("P001");
        assertThat(productNotFound.getMessage()).isEqualTo("상품을 찾을 수 없습니다.");

        assertThat(zeroQuantity.getStatus()).isEqualTo(400);
        assertThat(zeroQuantity.getCode()).isEqualTo("CA001");
        assertThat(zeroQuantity.getMessage()).isEqualTo("수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("Exception 상속 관계 확인")
    void exception_Inheritance_Test() {
        // given & when
        BusinessException businessException = new BusinessException(ErrorCode.USER_NOT_FOUND);
        ProductException.ProductNotFoundException productException =
                new ProductException.ProductNotFoundException();
        StoreException.StoreNotFoundException storeException =
                new StoreException.StoreNotFoundException();

        // then
        assertThat(businessException).isInstanceOf(RuntimeException.class);
        assertThat(productException).isInstanceOf(BusinessException.class);
        assertThat(productException).isInstanceOf(RuntimeException.class);
        assertThat(storeException).isInstanceOf(BusinessException.class);
        assertThat(storeException).isInstanceOf(RuntimeException.class);
    }
}