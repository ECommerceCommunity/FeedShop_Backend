package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FeedNotFoundException 테스트")
class FeedNotFoundExceptionTest {

    @Test
    @DisplayName("feedId만으로 예외 생성 시 올바른 메시지가 생성된다")
    void createWithFeedId() {
        // given
        Long feedId = 1L;

        // when
        FeedNotFoundException exception = new FeedNotFoundException(feedId);

        // then
        assertThat(exception.getMessage()).isEqualTo("피드를 찾을 수 없습니다. feedId: 1");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("메시지만으로 예외 생성 시 올바른 메시지가 생성된다")
    void createWithMessage() {
        // given
        String message = "테스트 메시지";

        // when
        FeedNotFoundException exception = new FeedNotFoundException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo("테스트 메시지");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("feedId와 메시지로 예외 생성 시 올바른 메시지가 생성된다")
    void createWithFeedIdAndMessage() {
        // given
        Long feedId = 1L;
        String message = "추가 메시지";

        // when
        FeedNotFoundException exception = new FeedNotFoundException(feedId, message);

        // then
        assertThat(exception.getMessage()).isEqualTo("피드를 찾을 수 없습니다. feedId: 1, 추가 메시지");
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("ErrorCode가 올바르게 설정된다")
    void errorCodeIsCorrect() {
        // when
        FeedNotFoundException exception = new FeedNotFoundException(1L);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FEED_NOT_FOUND);
        assertThat(exception.getErrorCode().getCode()).isEqualTo("F001");
        assertThat(exception.getErrorCode().getStatus()).isEqualTo(404);
    }
}
