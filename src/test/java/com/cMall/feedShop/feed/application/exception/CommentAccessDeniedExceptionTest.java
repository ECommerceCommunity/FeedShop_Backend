package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommentAccessDeniedException 테스트")
class CommentAccessDeniedExceptionTest {

    @Test
    @DisplayName("기본 생성자로 예외를 생성할 수 있다")
    void createExceptionWithDefaultConstructor() {
        // when
        CommentAccessDeniedException exception = new CommentAccessDeniedException();

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_ACCESS_DENIED);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.COMMENT_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("메시지를 포함하여 예외를 생성할 수 있다")
    void createExceptionWithMessage() {
        // given
        String message = "테스트 댓글에 대한 권한이 없습니다.";

        // when
        CommentAccessDeniedException exception = new CommentAccessDeniedException(message);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_ACCESS_DENIED);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("댓글 ID와 사용자 ID를 포함하여 예외를 생성할 수 있다")
    void createExceptionWithCommentIdAndUserId() {
        // given
        Long commentId = 1L;
        Long userId = 2L;

        // when
        CommentAccessDeniedException exception = new CommentAccessDeniedException(commentId, userId);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_ACCESS_DENIED);
        assertThat(exception.getMessage()).isEqualTo("댓글에 대한 권한이 없습니다. (댓글 ID: " + commentId + ", 사용자 ID: " + userId + ")");
    }
}
