package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.CartItemCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CartItemCreateRequest 유효성 검증 테스트")
class CartItemValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 요청 - 검증 성공")
    void validRequest_Success() {
        // given
        CartItemCreateRequest request = new CartItemCreateRequest();
        ReflectionTestUtils.setField(request, "optionId", 1L);
        ReflectionTestUtils.setField(request, "imageId", 1L);
        ReflectionTestUtils.setField(request, "quantity", 5);

        // when
        Set<ConstraintViolation<CartItemCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("검증 실패 - optionId null")
    void validationFail_OptionIdNull() {
        // given
        CartItemCreateRequest request = new CartItemCreateRequest();
        ReflectionTestUtils.setField(request, "optionId", null);
        ReflectionTestUtils.setField(request, "imageId", 1L);
        ReflectionTestUtils.setField(request, "quantity", 5);

        // when
        Set<ConstraintViolation<CartItemCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("상품 옵션 ID는 필수입니다");
    }

    @Test
    @DisplayName("검증 실패 - imageId null")
    void validationFail_ImageIdNull() {
        // given
        CartItemCreateRequest request = new CartItemCreateRequest();
        ReflectionTestUtils.setField(request, "optionId", 1L);
        ReflectionTestUtils.setField(request, "imageId", null);
        ReflectionTestUtils.setField(request, "quantity", 5);

        // when
        Set<ConstraintViolation<CartItemCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("이미지 ID는 필수입니다");
    }

    @Test
    @DisplayName("검증 실패 - quantity null")
    void validationFail_QuantityNull() {
        // given
        CartItemCreateRequest request = new CartItemCreateRequest();
        ReflectionTestUtils.setField(request, "optionId", 1L);
        ReflectionTestUtils.setField(request, "imageId", 1L);
        ReflectionTestUtils.setField(request, "quantity", null);

        // when
        Set<ConstraintViolation<CartItemCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("수량은 필수입니다");
    }

    @Test
    @DisplayName("검증 실패 - quantity 0")
    void validationFail_QuantityZero() {
        // given
        CartItemCreateRequest request = new CartItemCreateRequest();
        ReflectionTestUtils.setField(request, "optionId", 1L);
        ReflectionTestUtils.setField(request, "imageId", 1L);
        ReflectionTestUtils.setField(request, "quantity", 0);

        // when
        Set<ConstraintViolation<CartItemCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("수량은 1개 이상이어야 합니다");
    }

    @Test
    @DisplayName("검증 실패 - quantity 음수")
    void validationFail_QuantityNegative() {
        // given
        CartItemCreateRequest request = new CartItemCreateRequest();
        ReflectionTestUtils.setField(request, "optionId", 1L);
        ReflectionTestUtils.setField(request, "imageId", 1L);
        ReflectionTestUtils.setField(request, "quantity", -1);

        // when
        Set<ConstraintViolation<CartItemCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("수량은 1개 이상이어야 합니다");
    }

    @Test
    @DisplayName("검증 실패 - 모든 필드 null")
    void validationFail_AllFieldsNull() {
        // given
        CartItemCreateRequest request = new CartItemCreateRequest();

        // when
        Set<ConstraintViolation<CartItemCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(3); // optionId, imageId, quantity 모두 필수
    }
}