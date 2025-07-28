package com.cMall.feedShop.event.application.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class InvalidEventStatusExceptionTest {
    @Test
    void 기본_생성자_메시지_확인() {
        InvalidEventStatusException ex = new InvalidEventStatusException();
        assertThat(ex.getErrorCode().name()).isEqualTo("INVALID_EVENT_STATUS");
    }

    @Test
    void 메시지_생성자_확인() {
        InvalidEventStatusException ex = new InvalidEventStatusException("상태 오류");
        assertThat(ex.getMessage()).contains("상태 오류");
    }

    @Test
    void status_및_reason_생성자_확인() {
        InvalidEventStatusException ex = new InvalidEventStatusException("ENDED", "이미 종료됨");
        assertThat(ex.getMessage()).contains("ENDED").contains("이미 종료됨");
    }
} 