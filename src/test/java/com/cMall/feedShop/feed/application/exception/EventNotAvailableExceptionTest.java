package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventNotAvailableException 테스트")
class EventNotAvailableExceptionTest {

    @Test
    @DisplayName("eventId만으로 예외 생성 시 올바른 메시지가 생성된다")
    void createWithEventId() {
        // given
        Long eventId = 1L;

        // when
        EventNotAvailableException exception = new EventNotAvailableException(eventId);

        // then
        assertThat(exception.getMessage()).isEqualTo("참여할 수 없는 이벤트입니다. eventId: 1");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EVENT_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("eventId와 이유로 예외 생성 시 올바른 메시지가 생성된다")
    void createWithEventIdAndReason() {
        // given
        Long eventId = 1L;
        String reason = "이미 종료된 이벤트입니다";

        // when
        EventNotAvailableException exception = new EventNotAvailableException(eventId, reason);

        // then
        assertThat(exception.getMessage()).isEqualTo("참여할 수 없는 이벤트입니다. eventId: 1, 이유: 이미 종료된 이벤트입니다");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EVENT_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("ErrorCode가 올바르게 설정된다")
    void errorCodeIsCorrect() {
        // when
        EventNotAvailableException exception = new EventNotAvailableException(1L);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EVENT_NOT_AVAILABLE);
        assertThat(exception.getErrorCode().getCode()).isEqualTo("E004");
        assertThat(exception.getErrorCode().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("긴 이유 메시지가 포함된 예외 생성 시 올바른 메시지가 생성된다")
    void createWithLongReason() {
        // given
        Long eventId = 999L;
        String reason = "이벤트가 진행중이지 않거나 참여 조건을 만족하지 않습니다";

        // when
        EventNotAvailableException exception = new EventNotAvailableException(eventId, reason);

        // then
        assertThat(exception.getMessage()).isEqualTo("참여할 수 없는 이벤트입니다. eventId: 999, 이유: 이벤트가 진행중이지 않거나 참여 조건을 만족하지 않습니다");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EVENT_NOT_AVAILABLE);
    }
}
