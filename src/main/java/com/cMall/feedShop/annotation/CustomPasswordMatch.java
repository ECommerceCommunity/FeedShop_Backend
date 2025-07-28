package com.cMall.feedShop.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE}) // 클래스, 인터페이스, Enum에 적용 가능
@Retention(RetentionPolicy.RUNTIME) // 런타임 시까지 유지
@Constraint(validatedBy = CustomPasswordMatchValidator.class)
public @interface CustomPasswordMatch {
    String message() default "비밀번호와 비밀번호 확인이 일치하지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
