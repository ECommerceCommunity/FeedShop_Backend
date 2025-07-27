package com.cMall.feedShop.feed.application.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class FeedNotFoundExceptionTest {
    
    @Test
    void feedId_생성자_확인() {
        FeedNotFoundException ex = new FeedNotFoundException(123L);
        assertThat(ex.getErrorCode().name()).isEqualTo("FEED_NOT_FOUND");
        assertThat(ex.getMessage()).contains("피드를 찾을 수 없습니다");
        assertThat(ex.getMessage()).contains("123");
    }
    
    @Test
    void 다른_feedId로_생성자_확인() {
        FeedNotFoundException ex = new FeedNotFoundException(456L);
        assertThat(ex.getErrorCode().name()).isEqualTo("FEED_NOT_FOUND");
        assertThat(ex.getMessage()).contains("피드를 찾을 수 없습니다");
        assertThat(ex.getMessage()).contains("456");
    }
    
    @Test
    void null_feedId로_생성자_확인() {
        FeedNotFoundException ex = new FeedNotFoundException(null);
        assertThat(ex.getErrorCode().name()).isEqualTo("FEED_NOT_FOUND");
        assertThat(ex.getMessage()).contains("피드를 찾을 수 없습니다");
        assertThat(ex.getMessage()).contains("null");
    }
} 