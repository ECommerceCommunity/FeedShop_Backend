package com.cMall.feedShop.feed.application.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EventNotAvailableExceptionTest {
    
    @Test
    void eventId_생성자_확인() {
        EventNotAvailableException ex = new EventNotAvailableException(123L);
        assertThat(ex.getErrorCode().name()).isEqualTo("EVENT_NOT_AVAILABLE");
        assertThat(ex.getMessage()).contains("참여할 수 없는 이벤트입니다");
        assertThat(ex.getMessage()).contains("123");
    }
    
    @Test
    void eventId와_reason_생성자_확인() {
        EventNotAvailableException ex = new EventNotAvailableException(456L, "이벤트가 종료되었습니다");
        assertThat(ex.getErrorCode().name()).isEqualTo("EVENT_NOT_AVAILABLE");
        assertThat(ex.getMessage()).contains("참여할 수 없는 이벤트입니다");
        assertThat(ex.getMessage()).contains("456");
        assertThat(ex.getMessage()).contains("이유: 이벤트가 종료되었습니다");
    }
    
    @Test
    void 다른_값으로_생성자_확인() {
        EventNotAvailableException ex = new EventNotAvailableException(789L, "참여 인원이 초과되었습니다");
        assertThat(ex.getErrorCode().name()).isEqualTo("EVENT_NOT_AVAILABLE");
        assertThat(ex.getMessage()).contains("참여할 수 없는 이벤트입니다");
        assertThat(ex.getMessage()).contains("789");
        assertThat(ex.getMessage()).contains("이유: 참여 인원이 초과되었습니다");
    }
    
    @Test
    void null_값으로_생성자_확인() {
        EventNotAvailableException ex = new EventNotAvailableException(null);
        assertThat(ex.getErrorCode().name()).isEqualTo("EVENT_NOT_AVAILABLE");
        assertThat(ex.getMessage()).contains("참여할 수 없는 이벤트입니다");
        assertThat(ex.getMessage()).contains("null");
    }
    
    @Test
    void null_eventId와_reason_생성자_확인() {
        EventNotAvailableException ex = new EventNotAvailableException(null, "알 수 없는 오류");
        assertThat(ex.getErrorCode().name()).isEqualTo("EVENT_NOT_AVAILABLE");
        assertThat(ex.getMessage()).contains("참여할 수 없는 이벤트입니다");
        assertThat(ex.getMessage()).contains("null");
        assertThat(ex.getMessage()).contains("이유: 알 수 없는 오류");
    }
} 