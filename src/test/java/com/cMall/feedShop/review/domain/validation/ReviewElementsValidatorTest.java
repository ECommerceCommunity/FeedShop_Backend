package com.cMall.feedShop.review.domain.validation;

import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.request.ReviewUpdateRequest;
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

import java.util.List;
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

    // =================== ReviewCreateRequest 테스트 ===================

    @Test
    @DisplayName("모든 리뷰 요소가 입력된 생성 요청은 검증을 통과한다")
    void validReviewCreateElements() {
        // given
        ReviewCreateRequest request = createValidCreateRequest();

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("SizeFit이 누락된 생성 요청은 검증에 실패한다")
    void createRequest_MissingSizeFit() {
        // given
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(null) // 누락
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("정말 편하고 좋습니다. 추천해요!")
                .productId(1L)
                .build();

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2); // @NotNull과 @ValidReviewElements 모두 위반
        assertThat(violations).anyMatch(v -> v.getMessage().contains("사이즈 착용감"));
    }

    @Test
    @DisplayName("Cushion이 누락된 생성 요청은 검증에 실패한다")
    void createRequest_MissingCushion() {
        // given
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(null) // 누락
                .stability(Stability.STABLE)
                .content("정말 편하고 좋습니다. 추천해요!")
                .productId(1L)
                .build();

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("쿠션감"));
    }

    @Test
    @DisplayName("Stability가 누락된 생성 요청은 검증에 실패한다")
    void createRequest_MissingStability() {
        // given
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(null) // 누락
                .content("정말 편하고 좋습니다. 추천해요!")
                .productId(1L)
                .build();

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("안정성"));
    }

    @Test
    @DisplayName("모든 리뷰 요소가 누락된 생성 요청은 검증에 실패한다")
    void createRequest_MissingAllReviewElements() {
        // given
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(null) // 누락
                .cushion(null) // 누락
                .stability(null) // 누락
                .content("정말 편하고 좋습니다. 추천해요!")
                .productId(1L)
                .build();

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(6); // 각각 @NotNull + @ValidReviewElements
    }

    // =================== ReviewUpdateRequest 테스트 ===================

    @Test
    @DisplayName("모든 리뷰 요소가 입력된 수정 요청은 검증을 통과한다")
    void validReviewUpdateElements() {
        // given
        ReviewUpdateRequest request = createValidUpdateRequest();

        // when
        Set<ConstraintViolation<ReviewUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("SizeFit이 누락된 수정 요청은 검증에 실패한다")
    void updateRequest_MissingSizeFit() {
        // given
        ReviewUpdateRequest request = ReviewUpdateRequest.builder()
                .title("수정된 제목")
                .rating(4)
                .sizeFit(null) // 누락
                .cushion(Cushion.MEDIUM)
                .stability(Stability.STABLE)
                .content("수정된 내용입니다.")
                .deleteImageIds(List.of(1L, 2L))
                .build();

        // when
        Set<ConstraintViolation<ReviewUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("사이즈 착용감"));
    }

    @Test
    @DisplayName("Cushion이 누락된 수정 요청은 검증에 실패한다")
    void updateRequest_MissingCushion() {
        // given
        ReviewUpdateRequest request = ReviewUpdateRequest.builder()
                .title("수정된 제목")
                .rating(4)
                .sizeFit(SizeFit.BIG)
                .cushion(null) // 누락
                .stability(Stability.STABLE)
                .content("수정된 내용입니다.")
                .deleteImageIds(List.of(1L, 2L))
                .build();

        // when
        Set<ConstraintViolation<ReviewUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("쿠션감"));
    }

    @Test
    @DisplayName("Stability가 누락된 수정 요청은 검증에 실패한다")
    void updateRequest_MissingStability() {
        // given
        ReviewUpdateRequest request = ReviewUpdateRequest.builder()
                .title("수정된 제목")
                .rating(4)
                .sizeFit(SizeFit.BIG)
                .cushion(Cushion.FIRM)
                .stability(null) // 누락
                .content("수정된 내용입니다.")
                .deleteImageIds(List.of(1L, 2L))
                .build();

        // when
        Set<ConstraintViolation<ReviewUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("안정성"));
    }

    @Test
    @DisplayName("모든 리뷰 요소가 누락된 수정 요청은 검증에 실패한다")
    void updateRequest_MissingAllReviewElements() {
        // given
        ReviewUpdateRequest request = ReviewUpdateRequest.builder()
                .title("수정된 제목")
                .rating(4)
                .sizeFit(null) // 누락
                .cushion(null) // 누락
                .stability(null) // 누락
                .content("수정된 내용입니다.")
                .deleteImageIds(List.of(1L, 2L))
                .build();

        // when
        Set<ConstraintViolation<ReviewUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(6); // 각각 @NotNull + @ValidReviewElements
    }

    // =================== 경계값 테스트 ===================

    @Test
    @DisplayName("극값 리뷰 요소들도 검증을 통과한다")
    void extremeReviewElements() {
        // given - 생성 요청 테스트
        ReviewCreateRequest createRequest = ReviewCreateRequest.builder()
                .title("극값 테스트")
                .rating(1) // 최소값
                .sizeFit(SizeFit.VERY_SMALL) // 극값
                .cushion(Cushion.VERY_FIRM) // 극값
                .stability(Stability.VERY_UNSTABLE) // 극값
                .content("극값으로 테스트하는 내용입니다.")
                .productId(1L)
                .build();

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> createViolations = validator.validate(createRequest);

        // then
        assertThat(createViolations).isEmpty();

        // given - 수정 요청 테스트
        ReviewUpdateRequest updateRequest = ReviewUpdateRequest.builder()
                .title("극값 테스트")
                .rating(5) // 최대값
                .sizeFit(SizeFit.VERY_BIG) // 극값
                .cushion(Cushion.VERY_SOFT) // 극값
                .stability(Stability.VERY_STABLE) // 극값
                .content("극값으로 테스트하는 내용입니다.")
                .build();

        // when
        Set<ConstraintViolation<ReviewUpdateRequest>> updateViolations = validator.validate(updateRequest);

        // then
        assertThat(updateViolations).isEmpty();
    }

    @Test
    @DisplayName("제목이나 내용 검증 실패 시에도 리뷰 요소 검증은 별도로 동작한다")
    void invalidTitleAndContentButValidElements() {
        // given - 제목은 빈 문자열, 내용은 너무 짧음
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .title("") // 빈 제목 (검증 실패)
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("짧음") // 10자 미만 (검증 실패)
                .productId(1L)
                .build();

        // when
        Set<ConstraintViolation<ReviewCreateRequest>> violations = validator.validate(request);

        // then
        // 제목과 내용 검증 실패는 있지만, @ValidReviewElements는 통과해야 함
        assertThat(violations).hasSizeGreaterThan(0);
        assertThat(violations).anyMatch(v -> v.getMessage().contains("리뷰 제목은 필수입니다"));
        assertThat(violations).anyMatch(v -> v.getMessage().contains("10자 이상"));

        // @ValidReviewElements 관련 오류는 없어야 함 (3요소는 모두 입력됨)

        assertThat(violations).noneMatch(v ->
                v.getConstraintDescriptor().getAnnotation() instanceof ValidReviewElements);

    }

    // =================== 헬퍼 메서드 ===================

    private ReviewCreateRequest createValidCreateRequest() {
        return ReviewCreateRequest.builder()
                .title("좋은 신발입니다")
                .rating(5)
                .sizeFit(SizeFit.NORMAL)
                .cushion(Cushion.SOFT)
                .stability(Stability.STABLE)
                .content("정말 편하고 좋습니다. 추천해요!")
                .productId(1L)
                .build();
    }

    private ReviewUpdateRequest createValidUpdateRequest() {
        return ReviewUpdateRequest.builder()
                .title("수정된 제목")
                .rating(4)
                .sizeFit(SizeFit.BIG)
                .cushion(Cushion.MEDIUM)
                .stability(Stability.STABLE)
                .content("수정된 내용입니다.")
                .deleteImageIds(List.of(1L, 2L))
                .build();
    }
}