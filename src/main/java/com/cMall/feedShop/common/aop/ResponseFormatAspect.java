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

            // 1. 결과가 ResponseEntity 인 경우
            if (result instanceof ResponseEntity<?> responseEntity) {
                Object body = responseEntity.getBody();

                // ResponseEntity의 바디가 ApiResponse인 경우
                if (body instanceof ApiResponse<?> apiResponse) {
                    // 성공 응답일 때만 메시지와 상태 코드를 적용
                    if (apiResponse.isSuccess()) {
                        // 어노테이션에 메시지가 명시되어 있다면 덮어씌웁니다.
                        if (customMessage != null && !customMessage.isEmpty()) {
                            apiResponse.setMessage(customMessage);
                        }
                        // 어노테이션에 명시된 HTTP 상태 코드를 ResponseEntity에 적용
                        // 단, ApiResponse는 내부적으로 isSuccess()만 가지고 있으므로,
                        // 응답 메시지에 따라 상태코드를 변경하는 로직이 필요할 수 있습니다.
                        // 여기서는 ApiResponseFormat의 status를 우선적으로 적용합니다.
                        return new ResponseEntity<>(apiResponse, responseEntity.getHeaders(), customStatus);
                    }
                    // 실패 응답이라면 기존 ApiResponse와 상태 코드를 유지 (메시지 덮어쓰지 않음)
                    return responseEntity; // 기존 ResponseEntity 반환
                }
                // ResponseEntity의 바디가 ApiResponse가 아니라면, 원본 ResponseEntity 그대로 반환
                return responseEntity;
            }
            // 2. 결과가 ApiResponse 인 경우 (ResponseEntity로 래핑되지 않은 경우)
            else if (result instanceof ApiResponse<?> apiResponse) {
                // 성공 응답일 때만 메시지 적용
                if (apiResponse.isSuccess()) {
                    if (customMessage != null && !customMessage.isEmpty()) {
                        apiResponse.setMessage(customMessage);
                    }
                }
                // 이 경우, HTTP 상태 코드는 200 OK로 반환됩니다.
                // 만약 ApiResponseFormat의 status 값을 여기에서도 HTTP 상태 코드로 반영하고 싶다면,
                // ResponseEntity로 다시 래핑하여 반환해야 합니다.
                // return new ResponseEntity<>(apiResponse, customStatus);
                // 현재 코드에서는 ApiResponse만 반환하므로 HTTP 상태 코드는 200 OK가 됩니다.
                return apiResponse;
            }
            // 3. 그 외의 경우 (ApiResponse나 ResponseEntity도 아닌 경우)
            // 예를 들어 컨트롤러가 String이나 다른 객체를 직접 반환하는 경우
            else {
                // AOP가 원본 결과를 ApiResponse로 래핑하고 메시지를 적용합니다.
                // 이 경우, HTTP 상태 코드는 ApiResponseFormat의 status를 따르도록 할 수 있습니다.
                return new ResponseEntity<>(ApiResponse.success(customMessage, result), customStatus);
            }

        } catch (Exception e) {
            log.error("API 실행 중 오류 발생 - Method: {}, Error: {}",
                    joinPoint.getSignature().getName(), e.getMessage());
            // 예외 발생 시 GlobalExceptionHandler가 처리하도록 예외를 다시 던집니다.
            throw e;
        }
    }
}