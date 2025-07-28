package com.cMall.feedShop.event.application.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EventNotFoundExceptionTest {
    @Test
    void 기본_생성자_메시지_확인() {
        EventNotFoundException ex = new EventNotFoundException();
        assertThat(ex.getErrorCode().name()).isEqualTo("EVENT_NOT_FOUND");
    }

    @Test
    void 메시지_생성자_확인() {
        EventNotFoundException ex = new EventNotFoundException("이벤트 없음");
        assertThat(ex.getMessage()).contains("이벤트 없음");
    }

    @Test
    void eventId_생성자_확인() {
        EventNotFoundException ex = new EventNotFoundException(123L);
        assertThat(ex.getMessage()).contains("123");
    }
} 