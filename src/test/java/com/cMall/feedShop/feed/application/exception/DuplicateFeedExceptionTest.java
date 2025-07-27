package com.cMall.feedShop.feed.application.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DuplicateFeedExceptionTest {
    
    @Test
    void orderItemId와_userId_생성자_확인() {
        DuplicateFeedException ex = new DuplicateFeedException(123L, 456L);
        assertThat(ex.getErrorCode().name()).isEqualTo("DUPLICATE_FEED");
        assertThat(ex.getMessage()).contains("이미 해당 주문 상품에 대한 피드를 작성하셨습니다");
        assertThat(ex.getMessage()).contains("123");
        assertThat(ex.getMessage()).contains("456");
    }
    
    @Test
    void 다른_값으로_생성자_확인() {
        DuplicateFeedException ex = new DuplicateFeedException(789L, 101L);
        assertThat(ex.getErrorCode().name()).isEqualTo("DUPLICATE_FEED");
        assertThat(ex.getMessage()).contains("이미 해당 주문 상품에 대한 피드를 작성하셨습니다");
        assertThat(ex.getMessage()).contains("789");
        assertThat(ex.getMessage()).contains("101");
    }
    
    @Test
    void null_값으로_생성자_확인() {
        DuplicateFeedException ex = new DuplicateFeedException(null, null);
        assertThat(ex.getErrorCode().name()).isEqualTo("DUPLICATE_FEED");
        assertThat(ex.getMessage()).contains("이미 해당 주문 상품에 대한 피드를 작성하셨습니다");
        assertThat(ex.getMessage()).contains("null");
    }
} 