package com.cMall.feedShop.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.UUID;

/**
 * 쇼핑몰 프로젝트용 통합 로깅 AOP
 * - 성능 모니터링 (느린 메서드 감지)
 * - 에러 추적 (예외 발생 지점)
 * - API 호출 추적 (요청별 추적 ID)
 * - 비즈니스 로직 흐름 추적
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final long SLOW_METHOD_THRESHOLD = 1000; // 1초 이상이면 느린 메서드로 간주

    // =========================== Pointcut 정의 ===========================

    // Controller 레이어
    @Pointcut("execution(* com.cMall.feedShop..*controller.*.*(..)) || execution(* com.cMall.feedShop..presentation.*.*(..))")
    private void controllerMethods() {}

    // Service 레이어 (핵심 비즈니스 로직)
    @Pointcut("execution(* com.cMall.feedShop..*service.*.*(..)) || execution(* com.cMall.feedShop..application.service.*.*(..))")
    private void serviceMethods() {}

    // Repository 레이어 (데이터베이스 접근)
    @Pointcut("execution(* com.cMall.feedShop..repository.*.*(..))")
    private void repositoryMethods() {}

    // =========================== Controller 로깅 ===========================

    @Around("controllerMethods()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = generateTraceId();
        MDC.put("traceId", traceId);

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        long start = System.currentTimeMillis();

        log.info("🌐 [API-START] {}.{}() | TraceID: {}", className, methodName, traceId);

        if (args.length > 0) {
            log.info("📥 [REQUEST] Args: {}", formatArgs(args));
        }

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            Object loggableResult = result;
            if (result instanceof ResponseEntity<?> responseEntity) {
                loggableResult = responseEntity.getBody();
            }

            log.info("📤 [RESPONSE] Return: {} | Duration: {}ms",
                    formatResult(loggableResult), duration);
            log.info("✅ [API-END] {}.{}() SUCCESS | TraceID: {}",
                    className, methodName, traceId);

            return result;
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - start;
            log.error("❌ [API-ERROR] {}.{}() | Duration: {}ms | Error: {} | TraceID: {}",
                    className, methodName, duration, throwable.getMessage(), traceId, throwable);
            throw throwable;
        } finally {
            MDC.clear();
        }
    }

    // =========================== Service 로깅 ===========================

    @Around("serviceMethods()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        long start = System.currentTimeMillis();
        String traceId = MDC.get("traceId");

        log.info("🔧 [SERVICE-START] {}.{}() | TraceID: {}", className, methodName, traceId);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            // 느린 메서드 감지
            if (duration > SLOW_METHOD_THRESHOLD) {
                log.warn("🐌 [SLOW-METHOD] {}.{}() took {}ms (>{} ms threshold)",
                        className, methodName, duration, SLOW_METHOD_THRESHOLD);
            }

            log.info("✅ [SERVICE-END] {}.{}() | Duration: {}ms | TraceID: {}",
                    className, methodName, duration, traceId);

            return result;
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - start;
            log.error("❌ [SERVICE-ERROR] {}.{}() | Duration: {}ms | Error: {} | TraceID: {}",
                    className, methodName, duration, throwable.getMessage(), traceId, throwable);
            throw throwable;
        }
    }

    // =========================== Repository 로깅 ===========================

    @Around("repositoryMethods()")
    public Object logRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        long start = System.currentTimeMillis();
        String traceId = MDC.get("traceId");

        log.debug("💾 [DB-START] {}.{}() | TraceID: {}", className, methodName, traceId);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            // DB 쿼리가 느린 경우 경고
            if (duration > 500) { // 0.5초 이상
                log.warn("🐌 [SLOW-QUERY] {}.{}() took {}ms | TraceID: {}",
                        className, methodName, duration, traceId);
            } else {
                log.debug("✅ [DB-END] {}.{}() | Duration: {}ms | TraceID: {}",
                        className, methodName, duration, traceId);
            }

            return result;
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - start;
            log.error("❌ [DB-ERROR] {}.{}() | Duration: {}ms | Error: {} | TraceID: {}",
                    className, methodName, duration, throwable.getMessage(), traceId, throwable);
            throw throwable;
        }
    }

    // =========================== 유틸리티 메서드 ===========================

    /**
     * 요청별 고유 추적 ID 생성
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 메서드 파라미터 포맷팅 (민감한 정보 마스킹)
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "[]";

        Object[] maskedArgs = Arrays.stream(args)
                .map(this::maskSensitiveData)
                .toArray();

        return Arrays.toString(maskedArgs);
    }

    /**
     * 반환값 포맷팅 (큰 객체는 요약)
     */
    private String formatResult(Object result) {
        if (result == null) return "null";

        Object loggableResult = result;
        if (result instanceof ResponseEntity<?> responseEntity) {
            loggableResult = responseEntity.getBody(); // 내부 body 추출
        }

        String resultStr = loggableResult.toString();
        // 너무 긴 결과는 요약
        if (resultStr.length() > 200) {
            return loggableResult.getClass().getSimpleName() + "[" + resultStr.substring(0, 100) + "...]";
        }

        return maskSensitiveData(loggableResult).toString();
    }

    /**
     * 민감한 정보 마스킹 (비밀번호, 카드번호 등)
     */
    private Object maskSensitiveData(Object obj) {
        if (obj == null) return null;

        String str = obj.toString();

        // reCAPTCHA 토큰 필드 마스킹
        if (str.contains("recaptchaToken=")) {
            str = str.replaceAll("recaptchaToken=[^,\\s)]+", "recaptchaToken=***");
        }

        // 비밀번호 필드 마스킹
        if (str.contains("password") || str.contains("pwd")) {
            return str.replaceAll("(password|pwd)=[^,\\s}]+", "$1=***");
        }

        // 카드번호 마스킹 (16자리 숫자)
        str = str.replaceAll("\\b\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}\\b",
                "****-****-****-****");

        // 이메일 부분 마스킹
        str = str.replaceAll("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})",
                "$1***@$2");

        return str;
    }
}