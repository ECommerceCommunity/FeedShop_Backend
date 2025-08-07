package com.cMall.feedShop.review.domain.exception;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;

/**
 * 리뷰 접근 권한 예외
 *
 * 🔍설명:
 * - 사용자가 다른 사람의 리뷰를 수정하려고 할 때 발생하는 예외입니다
 * - 예를 들어, A가 작성한 리뷰를 B가 수정하려고 하면 이 예외가 발생합니다
 * - 보안을 위해 본인이 작성한 리뷰만 수정할 수 있도록 막는 역할을 합니다
 */
public class ReviewAccessDeniedException extends BusinessException {

    public ReviewAccessDeniedException() {
        super(ErrorCode.FORBIDDEN, "리뷰에 대한 권한이 없습니다.");
    }

    public ReviewAccessDeniedException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }

    /**
     * 리뷰 수정 권한 없음 (가장 많이 사용될 케이스)
     */
    public static ReviewAccessDeniedException forUpdate() {
        return new ReviewAccessDeniedException("본인이 작성한 리뷰만 수정할 수 있습니다.");
    }

    /**
     * 리뷰 삭제 권한 없음
     */
    public static ReviewAccessDeniedException forDelete() {
        return new ReviewAccessDeniedException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
    }

    /**
     * 특정 작업에 대한 권한 없음
     */
    public static ReviewAccessDeniedException forAction(String action) {
        return new ReviewAccessDeniedException(String.format("본인이 작성한 리뷰만 %s할 수 있습니다.", action));
    }
}