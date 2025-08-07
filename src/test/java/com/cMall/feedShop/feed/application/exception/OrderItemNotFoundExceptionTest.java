package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderItemNotFoundException 테스트")
class OrderItemNotFoundExceptionTest {

    @Test
    @DisplayName("orderItemId로 예외 생성 시 올바른 메시지가 생성된다")
    void createWithOrderItemId() {
        // given
        Long orderItemId = 1L;

        // when
        OrderItemNotFoundException exception = new OrderItemNotFoundException(orderItemId);

        // then
        assertThat(exception.getMessage()).isEqualTo("주문 상품을 찾을 수 없습니다. orderItemId: 1");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("큰 orderItemId로 예외 생성 시 올바른 메시지가 생성된다")
    void createWithLargeOrderItemId() {
        // given
        Long orderItemId = 999999L;

        // when
        OrderItemNotFoundException exception = new OrderItemNotFoundException(orderItemId);

        // then
        assertThat(exception.getMessage()).isEqualTo("주문 상품을 찾을 수 없습니다. orderItemId: 999999");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("ErrorCode가 올바르게 설정된다")
    void errorCodeIsCorrect() {
        // when
        OrderItemNotFoundException exception = new OrderItemNotFoundException(1L);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_ITEM_NOT_FOUND);
        assertThat(exception.getErrorCode().getCode()).isEqualTo("F004");
        assertThat(exception.getErrorCode().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("0인 orderItemId로 예외 생성 시 올바른 메시지가 생성된다")
    void createWithZeroOrderItemId() {
        // given
        Long orderItemId = 0L;

        // when
        OrderItemNotFoundException exception = new OrderItemNotFoundException(orderItemId);

        // then
        assertThat(exception.getMessage()).isEqualTo("주문 상품을 찾을 수 없습니다. orderItemId: 0");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_ITEM_NOT_FOUND);
    }
}
