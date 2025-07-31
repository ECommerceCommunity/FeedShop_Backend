package com.cMall.feedShop.review.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReviewElementsValidator.class)
@Documented
public @interface ValidReviewElements {
    String message() default "3요소 평가(사이즈 착용감, 쿠션감, 안정성)는 모두 필수입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}