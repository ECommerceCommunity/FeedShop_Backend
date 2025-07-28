package com.cMall.feedShop.common.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiResponseFormat {
    /**
     * 성공 시 반환할 메시지
     */
    String message() default "요청이 성공적으로 처리되었습니다.";
    
    /**
     * HTTP 상태 코드 (기본값: 200)
     */
    int status() default 200;
}