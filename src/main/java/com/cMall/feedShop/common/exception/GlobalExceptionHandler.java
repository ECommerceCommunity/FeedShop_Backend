package com.cMall.feedShop.common.exception;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.review.domain.exception.ReviewPurchaseRequiredException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 비즈니스 예외 - ApiResponse 형태로 반환
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(e.getErrorCode().getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    // 유효성 검사 예외 - 필드 에러 정보 포함
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ErrorResponse.FieldError>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.of(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ApiResponse<List<ErrorResponse.FieldError>> response = ApiResponse.<List<ErrorResponse.FieldError>>builder()
                .success(false)
                .message("입력값이 올바르지 않습니다.")
                .data(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }
    // 추가: Path Variable이나 Request Parameter의 타입 변환 실패
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.error("Type mismatch error: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("잘못된 형식의 파라미터입니다.")
                .data(null)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // 추가: Bean Validation 제약조건 위반 (@Validated 사용 시)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException e) {
        log.error("Constraint violation: {}", e.getMessage());

        String message = e.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(message.isEmpty() ? "입력값 검증에 실패했습니다." : message)
                .data(null)
                .build();

        return ResponseEntity.badRequest().body(response);
    }
    // 인증 예외
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.error("Authentication error: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("인증이 필요합니다.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 권한 예외
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("접근 권한이 없습니다.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 리뷰 구매이력 필수 예외
    @ExceptionHandler(ReviewPurchaseRequiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleReviewPurchaseRequiredException(
            ReviewPurchaseRequiredException e) {
        log.error("Review purchase required error: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(e.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 추가: IllegalArgumentException 처리 (서비스 레이어에서 발생하는 일반적인 예외)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument error: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(e.getMessage())
                .data(null)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.error("Missing required parameter: {}", e.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("필수 파라미터 '" + e.getParameterName() + "'가 누락되었습니다.")
                .data(null)
                .build();

        // 400 Bad Request 반환
        return ResponseEntity.badRequest().body(response);
    }

    // 일반 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("서버 오류가 발생했습니다.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}