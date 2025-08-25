package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommentNotFoundException 테스트")
class CommentNotFoundExceptionTest {

    @Test
    @DisplayName("기본 생성자로 예외를 생성할 수 있다")
    void createExceptionWithDefaultConstructor() {
        // when
        CommentNotFoundException exception = new CommentNotFoundException();

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("메시지를 포함하여 예외를 생성할 수 있다")
    void createExceptionWithMessage() {
        // given
        String message = "테스트 댓글을 찾을 수 없습니다.";

        // when
        CommentNotFoundException exception = new CommentNotFoundException(message);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("댓글 ID를 포함하여 예외를 생성할 수 있다")
    void createExceptionWithCommentId() {
        // given
        Long commentId = 1L;

        // when
        CommentNotFoundException exception = new CommentNotFoundException(commentId);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("댓글을 찾을 수 없습니다. (댓글 ID: " + commentId + ")");
    }
}
