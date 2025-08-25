package com.cMall.feedShop.review.domain.exception;

import com.cMall.feedShop.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DuplicateReportException 테스트")
class DuplicateReportExceptionTest {

    @Test
    @DisplayName("기본 생성자로 예외 생성")
    void createException_WithDefaultConstructor() {
        // when
        DuplicateReportException exception = new DuplicateReportException();

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_REPORT);
        assertThat(exception.getMessage()).isNotNull();
    }

    @Test
    @DisplayName("사용자 정의 메시지로 예외 생성")
    void createException_WithCustomMessage() {
        // given
        String customMessage = "이미 신고한 리뷰입니다.";

        // when
        DuplicateReportException exception = new DuplicateReportException(customMessage);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_REPORT);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
    }

    @Test
    @DisplayName("예외가 BusinessException을 상속하는지 확인")
    void exception_ExtendsBusinessException() {
        // when
        DuplicateReportException exception = new DuplicateReportException();

        // then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}