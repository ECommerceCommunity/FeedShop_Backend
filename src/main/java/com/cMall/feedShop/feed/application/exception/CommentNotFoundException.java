package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class CommentNotFoundException extends BusinessException {
    
    public CommentNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
    
    public CommentNotFoundException(String message) {
        super(ErrorCode.COMMENT_NOT_FOUND, message);
    }
    
    public CommentNotFoundException(Long commentId) {
        super(ErrorCode.COMMENT_NOT_FOUND, "댓글을 찾을 수 없습니다. (댓글 ID: " + commentId + ")");
    }
}
