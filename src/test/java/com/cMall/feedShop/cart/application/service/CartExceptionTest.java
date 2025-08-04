package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CartException 테스트")
class CartExceptionTest {

    @Test
    @DisplayName("CartZeroQuantityException 생성 및 속성 확인")
    void cartZeroQuantityException_Creation() {
        // when
        CartException exception = new CartException(ErrorCode.ZERO_QUANTITY);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ZERO_QUANTITY);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.ZERO_QUANTITY.getMessage());
        assertThat(exception.getErrorCode().getStatus()).isEqualTo(400);
        assertThat(exception.getErrorCode().getCode()).isEqualTo("CA001");
    }

    @Test
    @DisplayName("CartZeroQuantityException은 RuntimeException을 상속")
    void cartZeroQuantityException_ExtendsRuntimeException() {
        // when
        CartException exception = new CartException(ErrorCode.ZERO_QUANTITY);

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
    }
}