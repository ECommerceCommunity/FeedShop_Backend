package com.cMall.feedShop.feed.application.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class FeedAccessDeniedExceptionTest {
    
    @Test
    void feedId와_userId_생성자_확인() {
        FeedAccessDeniedException ex = new FeedAccessDeniedException(123L, 456L);
        assertThat(ex.getErrorCode().name()).isEqualTo("FEED_ACCESS_DENIED");
        assertThat(ex.getMessage()).contains("해당 피드에 대한 권한이 없습니다");
        assertThat(ex.getMessage()).contains("123");
        assertThat(ex.getMessage()).contains("456");
    }
    
    @Test
    void 다른_값으로_생성자_확인() {
        FeedAccessDeniedException ex = new FeedAccessDeniedException(789L, 101L);
        assertThat(ex.getErrorCode().name()).isEqualTo("FEED_ACCESS_DENIED");
        assertThat(ex.getMessage()).contains("해당 피드에 대한 권한이 없습니다");
        assertThat(ex.getMessage()).contains("789");
        assertThat(ex.getMessage()).contains("101");
    }
    
    @Test
    void null_값으로_생성자_확인() {
        FeedAccessDeniedException ex = new FeedAccessDeniedException(null, null);
        assertThat(ex.getErrorCode().name()).isEqualTo("FEED_ACCESS_DENIED");
        assertThat(ex.getMessage()).contains("해당 피드에 대한 권한이 없습니다");
        assertThat(ex.getMessage()).contains("null");
    }
} 