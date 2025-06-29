package com.cMall.feedShop.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(405, "C002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(500, "C003", "서버 오류가 발생했습니다."),

    // 인증/인가
    UNAUTHORIZED(401, "A001", "인증이 필요합니다."),
    FORBIDDEN(403, "A002", "권한이 없습니다."),

    // 사용자
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(409, "U002", "이미 존재하는 이메일입니다."),

    // 상품
    PRODUCT_NOT_FOUND(404, "P001", "상품을 찾을 수 없습니다."),
    OUT_OF_STOCK(409, "P002", "재고가 부족합니다."),

    // 주문
    ORDER_NOT_FOUND(404, "O001", "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS(400, "O002", "잘못된 주문 상태입니다.");

    private final int status;
    private final String code;
    private final String message;
}