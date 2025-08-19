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
    DATA_INTEGRITY_VIOLATION(409, "C004", "데이터 무결성 제약 조건을 위반했습니다."),

    // 인증/인가
    UNAUTHORIZED(401, "A001", "인증이 필요합니다."),
    FORBIDDEN(403, "A002", "권한이 없습니다."),

    // 사용자
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(409, "U002", "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(400, "U003", "비밀번호가 일치하지 않습니다."),
    USER_ALREADY_DELETED(409, "U004", "이미 탈퇴 처리된 계정입니다."),
    INVALID_VERIFICATION_TOKEN(400, "U005", "유효하지 않거나 찾을 수 없는 인증 토큰입니다."),
    ACCOUNT_ALREADY_VERIFIED(409, "U006", "이미 인증이 완료된 계정입니다."),
    VERIFICATION_TOKEN_EXPIRED(400, "U007", "인증 토큰이 만료되었습니다. 다시 회원가입을 시도하거나 인증 메일을 재발송해주세요."),
    OUT_OF_POINT(409, "U008", "사용 가능한 포인트가 부족합니다."),
    INVALID_POINT_AMOUNT(400, "U009", "유효하지 않은 포인트 금액입니다."),
    ACCOUNT_NOT_VERIFIED(403, "U010", "이메일 인증이 완료되지 않은 계정입니다."),
    DUPLICATE_LOGIN_ID(409, "U011", "이미 존재하는 로그인 ID입니다."),
    USER_ACCOUNT_NOT_ACTIVE(403, "U012", "계정이 활성화되어 있지 않습니다. 이메일 인증을 완료해주세요."),
    ADDRESS_NOT_FOUND(404, "A001", "주소 정보를 찾을 수 없습니다."),

    // 사용자 활동
    NO_ACTIVITY_DATA(404, "UA001", "해당 기간의 활동 내역이 없습니다."),

    // 비밀번호 재설정 관련 추가
    INVALID_TOKEN(400, "U100", "유효하지 않거나 찾을 수 없는 토큰입니다."), // 일반적인 토큰 유효성 검사 실패 (찾을 수 없거나 잘못된 형식)
    TOKEN_EXPIRED(400, "U101", "토큰이 만료되었습니다. 비밀번호 재설정을 다시 시도해주세요."), // 토큰 만료

    // 외부 API
    MAILGUN_API_FAILED(502, "E001", "이메일 전송 중 외부 API 오류가 발생했습니다."),

    // reCAPTCHA
    RECAPTCHA_VERIFICATION_FAILED(400, "A003", "reCAPTCHA 인증에 실패했습니다."),
    RECAPTCHA_SCORE_TOO_LOW(400, "A004", "비정상적인 접근으로 의심되어 요청을 차단합니다."),

    // 스토어
    STORE_FORBIDDEN(403, "S001", "해당 스토어의 관리자가 아닙니다."),
    STORE_NOT_FOUND(404, "S002", "스토어를 찾을 수 없습니다."),

    // 상품
    PRODUCT_NOT_FOUND(404, "P001", "상품을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(404, "P002", "카테고리를 찾을 수 없습니다."),
    OUT_OF_STOCK(409, "P003", "재고가 부족합니다."),
    PRODUCT_IN_ORDER(409, "P004", "상품이 주문에 포함되어 있어 삭제할 수 없습니다."),
    PRODUCT_OPTION_NOT_FOUND(404, "P005", "존재하지 않는 상품 옵션입니다."),
    PRODUCT_IMAGE_NOT_FOUND(404, "P006", "존재하지 않는 상품 이미지입니다."),
    DUPLICATE_PRODUCT_NAME(409, "P007", "이미 존재하는 상품 이름입니다."),
    INVALID_PRODUCT_FILTER_PRICE_RANGE(400, "P008", "잘못된 가격 범위입니다. 최소 가격은 최대 가격보다 작거나 같아야 합니다."),
    PRODUCT_NOT_BELONG_TO_STORE(403, "P009", "해당 상품은 현재 스토어에 속하지 않습니다."),
    DUPLICATE_PRODUCT_OPTION(409, "P010", "이미 존재하는 상품 옵션입니다."),
    FILE_DELETE_ERROR(400, "P011", "파일 삭제 중 오류가 발생했습니다."),

    // 장바구니
    ZERO_QUANTITY(400, "CA001", "수량은 1개 이상이어야 합니다."),
    CART_ITEM_NOT_FOUND(404, "CA002", "장바구니 아이템을 찾을 수 없습니다."),

    // 주문
    ORDER_NOT_FOUND(404, "O001", "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS(400, "O002", "잘못된 주문 상태입니다."),
    ORDER_FORBIDDEN(403, "O003", "주문은 일반 사용자만 가능합니다."),
    ORDER_CART_EMPTY(400, "O004", "장바구니가 비어 있습니다. 주문할 상품이 없습니다."),
    INVALID_POINT(400, "O005", "유효하지 않은 포인트입니다."),
    ORDER_CANCEL_FORBIDDEN(403, "O006", "취소할 수 없는 주문 상태입니다."),
    INVALID_ORDER_QUANTITY(400, "O007", "주문 수량이 유효하지 않습니다."),
    INVALID_OPTION_ID(400, "O008", "유효하지 않은 옵션 ID 입니다."),

    // 리뷰 관련
    REVIEW_NOT_FOUND(404, "R001", "리뷰를 찾을 수 없습니다."),
    DUPLICATE_REVIEW(409, "R002", "이미 해당 상품에 대한 리뷰를 작성하셨습니다."),
    REVIEW_ACCESS_DENIED(403, "R003", "해당 리뷰에 대한 권한이 없습니다."),
    INVALID_REVIEW_DATA(400, "R004", "잘못된 리뷰 데이터입니다."),
    FILE_UPLOAD_ERROR(400, "FILE_001", "파일 업로드에 실패했습니다."),
    INVALID_FILE_FORMAT(400, "FILE_002", "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(400, "FILE_003", "파일 크기가 너무 큽니다."),
    IMAGE_COUNT_EXCEEDED(400, "FILE_004", "이미지 개수가 제한을 초과했습니다."),
    IMAGE_NOT_FOUND(404, "FILE_005", "이미지를 찾을 수 없습니다."),
    REVIEW_ALREADY_DELETED(409, "R005", "이미 삭제된 리뷰입니다."),
    REVIEW_DELETE_FORBIDDEN(403, "R006", "해당 리뷰를 삭제할 권한이 없습니다."),
    REVIEW_DELETION_FAILED(500, "R007", "리뷰 삭제 중 오류가 발생했습니다."),

  
    // 이벤트 (현재 구현된 읽기 전용 API에서만 사용)
    EVENT_NOT_FOUND(404, "E001", "이벤트를 찾을 수 없습니다."),
    INVALID_EVENT_STATUS(400, "E002", "유효하지 않은 이벤트 상태입니다."),
    INVALID_EVENT_TYPE(400, "E003", "유효하지 않은 이벤트 타입입니다."),
    EVENT_NOT_AVAILABLE(400, "E004", "참여할 수 없는 이벤트입니다."),

    // 피드
    FEED_NOT_FOUND(404, "F001", "피드를 찾을 수 없습니다."),
    FEED_ACCESS_DENIED(403, "F002", "해당 피드에 대한 권한이 없습니다."),
    DUPLICATE_FEED(409, "F003", "이미 해당 주문 상품에 대한 피드를 작성하셨습니다."),
    ORDER_ITEM_NOT_FOUND(404, "F004", "주문 상품을 찾을 수 없습니다."),

    // 리워드
    REWARD_POLICY_NOT_FOUND(404, "RW001", "리워드 정책을 찾을 수 없습니다."),
    DAILY_REWARD_LIMIT_EXCEEDED(409, "RW002", "일일 리워드 획득 한도를 초과했습니다."),
    MONTHLY_REWARD_LIMIT_EXCEEDED(409, "RW003", "월간 리워드 획득 한도를 초과했습니다."),
    REWARD_ALREADY_GRANTED(409, "RW004", "이미 지급된 리워드입니다."),
    ACCESS_DENIED(403, "RW005", "접근 권한이 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
