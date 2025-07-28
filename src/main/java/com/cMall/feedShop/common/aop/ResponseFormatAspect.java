package com.cMall.feedShop.common.aop;

import com.cMall.feedShop.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ResponseFormatAspect {

    @Around("@annotation(apiResponseFormat)")
    public Object formatResponse(ProceedingJoinPoint joinPoint, ApiResponseFormat apiResponseFormat) throws Throwable {
        try {
            Object result = joinPoint.proceed(); // 컨트롤러 메서드 실행 결과
            String customMessage = apiResponseFormat.message(); // 어노테이션에 정의된 메시지
            HttpStatus customStatus = HttpStatus.valueOf(apiResponseFormat.status()); // 어노테이션에 정의된 상태 코드

            // 1. 결과가 ResponseEntity인 경우
            if (result instanceof ResponseEntity<?> responseEntity) {
                Object body = responseEntity.getBody();
                if (body instanceof ApiResponse<?> apiResponse) {
                    if (apiResponse.isSuccess() && customMessage != null && !customMessage.isEmpty()) {
                        apiResponse.setMessage(customMessage);
                    }
                    return new ResponseEntity<>(apiResponse, responseEntity.getHeaders(), customStatus);
                }
                return responseEntity; // ApiResponse가 아닌 경우 원본 반환
            }
            // 2. 결과가 ApiResponse인 경우
            else if (result instanceof ApiResponse<?> apiResponse) {
                if (apiResponse.isSuccess() && customMessage != null && !customMessage.isEmpty()) {
                    apiResponse.setMessage(customMessage);
                }
                return apiResponse; // ResponseEntity로 래핑하지 않고 그대로 반환
            }
            // 3. 그 외의 경우
            else {
                return new ResponseEntity<>(ApiResponse.success(customMessage, result), customStatus);
            }
        } catch (Exception e) {
            log.error("API 실행 중 오류 발생 - Method: {}, Error: {}",
                    joinPoint.getSignature().getName(), e.getMessage());
            throw e;
        }
    }
}