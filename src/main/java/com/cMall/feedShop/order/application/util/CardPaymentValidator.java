package com.cMall.feedShop.order.application.util;

import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CardPaymentValidator implements ConstraintValidator<ValidCardPayment, OrderCreateRequest> {

    @Override
    public boolean isValid(OrderCreateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        String paymentMethod = request.getPaymentMethod();

        // 결제 방법이 "카드"가 아닌 경우
        if (!"카드".equals(paymentMethod)) {
            // 카드 정보가 모두 빈 문자열이어야 함
            return isEmptyString(request.getCardNumber()) &&
                    isEmptyString(request.getCardExpiry()) &&
                    isEmptyString(request.getCardCvc());
        }

        // 결제 방법이 "카드"인 경우 카드 정보 유효성 검증
        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        // 카드 번호 검증 (10~14자리 숫자)
        if (!isValidCardNumber(request.getCardNumber())) {
            context.buildConstraintViolationWithTemplate("카드 번호는 10~14자리 숫자여야 합니다.")
                    .addPropertyNode("cardNumber")
                    .addConstraintViolation();
            isValid = false;
        }

        // 유효기간 검증 (4자리 숫자)
        if (!isValidCardExpiry(request.getCardExpiry())) {
            context.buildConstraintViolationWithTemplate("카드 유효기간은 4자리 숫자여야 합니다. (예: 1225)")
                    .addPropertyNode("cardExpiry")
                    .addConstraintViolation();
            isValid = false;
        }

        // CVC 검증 (3자리 숫자)
        if (!isValidCardCvc(request.getCardCvc())) {
            context.buildConstraintViolationWithTemplate("카드 CVC는 3자리 숫자여야 합니다.")
                    .addPropertyNode("cardCvc")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }

    private boolean isEmptyString(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) return false;
        String trimmed = cardNumber.trim();
        return trimmed.matches("\\d{10,14}");
    }

    private boolean isValidCardExpiry(String cardExpiry) {
        if (cardExpiry == null) return false;
        String trimmed = cardExpiry.trim();
        return trimmed.matches("\\d{4}");
    }

    private boolean isValidCardCvc(String cardCvc) {
        if (cardCvc == null) return false;
        String trimmed = cardCvc.trim();
        return trimmed.matches("\\d{3}");
    }
}
