package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 이벤트 관련 API 예외 처리 핸들러
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class EventExceptionHandler {

    /**
     * 이벤트를 찾을 수 없는 경우
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("이벤트 API 예외 발생: {}", e.getMessage());
        
        String message = e.getMessage();
        if (message.contains("이벤트를 찾을 수 없습니다")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("이벤트를 찾을 수 없습니다."));
        } else if (message.contains("이벤트 결과를 찾을 수 없습니다")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("이벤트 결과를 찾을 수 없습니다."));
        } else if (message.contains("참여자 결과를 찾을 수 없습니다")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("참여자 결과를 찾을 수 없습니다."));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("잘못된 요청입니다: " + message));
    }

    /**
     * 이벤트 상태 관련 예외
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException e) {
        log.error("이벤트 상태 예외 발생: {}", e.getMessage());
        
        String message = e.getMessage();
        if (message.contains("이미 결과가 존재합니다")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("이미 결과가 존재합니다. 강제 재계산을 원하면 forceRecalculate=true로 설정하세요."));
        } else if (message.contains("이미 결과가 발표된 이벤트입니다")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("이미 결과가 발표된 이벤트입니다."));
        } else if (message.contains("이벤트에 참여자가 없습니다")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("이벤트에 참여자가 없습니다."));
        } else if (message.contains("배틀 이벤트는 최소 2명의 참여자가 필요합니다")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("배틀 이벤트는 최소 2명의 참여자가 필요합니다."));
        } else if (message.contains("랭킹 이벤트에 참여자가 없습니다")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("랭킹 이벤트에 참여자가 없습니다."));
        } else if (message.contains("등 리워드 정보를 찾을 수 없습니다")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("리워드 정보를 찾을 수 없습니다."));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("이벤트 상태 오류: " + message));
    }

    /**
     * 지원하지 않는 이벤트 타입
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedOperationException(UnsupportedOperationException e) {
        log.error("지원하지 않는 이벤트 타입: {}", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("지원하지 않는 이벤트 타입입니다: " + e.getMessage()));
    }

    /**
     * 기타 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("이벤트 API 예상치 못한 예외 발생: {}", e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }
}
