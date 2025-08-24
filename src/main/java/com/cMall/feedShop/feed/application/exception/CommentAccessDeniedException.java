package com.cMall.feedShop.feed.application.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

public class CommentAccessDeniedException extends BusinessException {
    
    public CommentAccessDeniedException() {
        super(ErrorCode.COMMENT_ACCESS_DENIED);
    }
    
    public CommentAccessDeniedException(String message) {
        super(ErrorCode.COMMENT_ACCESS_DENIED, message);
    }
    
    public CommentAccessDeniedException(Long commentId, Long userId) {
        super(ErrorCode.COMMENT_ACCESS_DENIED, 
              "댓글에 대한 권한이 없습니다. (댓글 ID: " + commentId + ", 사용자 ID: " + userId + ")");
    }
}
