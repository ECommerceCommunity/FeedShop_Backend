package com.cMall.feedShop.review.domain.validation;

import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("리뷰 요소 검증 테스트")
class ReviewElementsValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("모든 리뷰 요소가 입력된 경우 검증을 통과한다")
    void validReviewElements() {
        // given
        ReviewCreateRequest request = createValidRequest();

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("SizeFit이 누락된 경우 검증에 실패한다")
    void missingSizeFit() {
        // given
        ReviewCreateRequest request = createValidRequest();
        request.setSizeFit(null);

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(2); // @NotNull과 @ValidReviewElements 모두 위반
        assertThat(violations).anyMatch(v -> v.getMessage().contains("사이즈 착용감"));
    }

    @Test
    @DisplayName("모든 리뷰 요소가 누락된 경우 검증에 실패한다")
    void missingAllReviewElements() {
        // given
        ReviewCreateRequest request = createValidRequest();
        request.setSizeFit(null);
        request.setCushion(null);
        request.setStability(null);

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(6); // 각각 @NotNull + @ValidReviewElements
    }

    @Test
    @DisplayName("Cushion만 누락된 경우 검증에 실패한다")
    void missingCushion() {
        // given
        ReviewCreateRequest request = createValidRequest();
        request.setCushion(null);

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("쿠션감"));
    }

    @Test
    @DisplayName("Stability만 누락된 경우 검증에 실패한다")
    void missingStability() {
        // given
        ReviewCreateRequest request = createValidRequest();
        request.setStability(null);

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("안정성"));
    }

    private ReviewCreateRequest createValidRequest() {
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setTitle("좋은 신발입니다");
        request.setRating(5);
        request.setSizeFit(SizeFit.NORMAL);
        request.setCushion(Cushion.SOFT);
        request.setStability(Stability.STABLE);
        request.setContent("정말 편하고 좋습니다. 추천해요!");
        request.setProductId(1L);
        return request;
    }
}