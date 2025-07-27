package com.cMall.feedShop.feed.application.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class OrderItemNotFoundExceptionTest {
    
    @Test
    void orderItemId_생성자_확인() {
        OrderItemNotFoundException ex = new OrderItemNotFoundException(123L);
        assertThat(ex.getErrorCode().name()).isEqualTo("ORDER_ITEM_NOT_FOUND");
        assertThat(ex.getMessage()).contains("주문 상품을 찾을 수 없습니다");
        assertThat(ex.getMessage()).contains("123");
    }
    
    @Test
    void 다른_orderItemId로_생성자_확인() {
        OrderItemNotFoundException ex = new OrderItemNotFoundException(456L);
        assertThat(ex.getErrorCode().name()).isEqualTo("ORDER_ITEM_NOT_FOUND");
        assertThat(ex.getMessage()).contains("주문 상품을 찾을 수 없습니다");
        assertThat(ex.getMessage()).contains("456");
    }
    
    @Test
    void null_orderItemId로_생성자_확인() {
        OrderItemNotFoundException ex = new OrderItemNotFoundException(null);
        assertThat(ex.getErrorCode().name()).isEqualTo("ORDER_ITEM_NOT_FOUND");
        assertThat(ex.getMessage()).contains("주문 상품을 찾을 수 없습니다");
        assertThat(ex.getMessage()).contains("null");
    }
} 