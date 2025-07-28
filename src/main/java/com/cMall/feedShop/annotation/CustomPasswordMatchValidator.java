package com.cMall.feedShop.annotation;


import com.cMall.feedShop.user.application.dto.request.UserSignUpRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomPasswordMatchValidator implements ConstraintValidator<CustomPasswordMatch, UserSignUpRequest> {

    @Override
    public boolean isValid(UserSignUpRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // null 객체는 이 검증의 대상이 아니라고 판단할 수 있음
        }

        // 비밀번호 또는 비밀번호 확인 필드가 null인 경우 (NotBlank 등 다른 검증이 선행되어야 함)
        if (request.getPassword() == null || request.getConfirmPassword() == null) {
            return false; // 둘 중 하나라도 null이면 일치하지 않는 것으로 간주
        }

        // 실제 비밀번호 일치 여부 검사
        boolean isValid = request.getPassword().equals(request.getConfirmPassword());

        // 검증 실패 시 기본 메시지 대신 커스텀 메시지를 추가하고 싶다면
        if (!isValid) {
            context.disableDefaultConstraintViolation(); // 기본 메시지 비활성화
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword") // 어떤 필드에 에러를 추가할지 지정
                    .addConstraintViolation();
        }

        return isValid;
    }
}
