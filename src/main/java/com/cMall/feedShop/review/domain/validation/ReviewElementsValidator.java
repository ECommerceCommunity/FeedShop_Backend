package com.cMall.feedShop.review.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 리뷰 3요소 검증 Validator
 * - ReviewElements 인터페이스를 구현한 모든 객체를 지원
 * - SizeFit, Cushion, Stability가 모두 입력되었는지 검증
 */
public class ReviewElementsValidator implements ConstraintValidator<ValidReviewElements, ReviewElements> { // ✅ 타입 변경

    @Override
    public void initialize(ValidReviewElements constraintAnnotation) {
        // 초기화 로직 (필요시)
    }

    @Override
    public boolean isValid(ReviewElements request, ConstraintValidatorContext context) { // ✅ 파라미터 타입 변경
        if (request == null) {
            return true; // null 체크는 다른 어노테이션에서 처리
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        // SizeFit 검증
        if (request.getSizeFit() == null) {
            context.buildConstraintViolationWithTemplate("사이즈 착용감은 필수입니다.")
                    .addPropertyNode("sizeFit")
                    .addConstraintViolation();
            isValid = false;
        }

        // Cushion 검증
        if (request.getCushion() == null) {
            context.buildConstraintViolationWithTemplate("쿠션감은 필수입니다.")
                    .addPropertyNode("cushion")
                    .addConstraintViolation();
            isValid = false;
        }

        // Stability 검증
        if (request.getStability() == null) {
            context.buildConstraintViolationWithTemplate("안정성은 필수입니다.")
                    .addPropertyNode("stability")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}