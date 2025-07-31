package com.cMall.feedShop.review.domain.validation;

import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReviewElementsValidator implements ConstraintValidator<ValidReviewElements, ReviewCreateRequest> {

    @Override
    public void initialize(ValidReviewElements constraintAnnotation) {
        // 초기화 로직 (필요시)
    }

    @Override
    public boolean isValid(ReviewCreateRequest request, ConstraintValidatorContext context) {
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