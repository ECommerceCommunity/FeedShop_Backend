package com.cMall.feedShop.event.application.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class InvalidEventTypeExceptionTest {
    @Test
    void 기본_생성자_메시지_확인() {
        InvalidEventTypeException ex = new InvalidEventTypeException();
        assertThat(ex.getErrorCode().name()).isEqualTo("INVALID_EVENT_TYPE");
    }

    @Test
    void 메시지_생성자_확인() {
        InvalidEventTypeException ex = new InvalidEventTypeException("타입 오류");
        assertThat(ex.getMessage()).contains("타입 오류");
    }

    @Test
    void type_및_reason_생성자_확인() {
        InvalidEventTypeException ex = new InvalidEventTypeException("BATTLE", "지원하지 않는 타입");
        assertThat(ex.getMessage()).contains("BATTLE").contains("지원하지 않는 타입");
    }
} 