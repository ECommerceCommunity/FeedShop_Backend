package com.cMall.feedShop.review.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewStatus Enum 테스트")
class ReviewStatusTest {

    @Test
    @DisplayName("ReviewStatus enum의 모든 값과 설명을 확인할 수 있다")
    void checkAllReviewStatusValues() {
        // when & then
        assertThat(ReviewStatus.ACTIVE.getDescription()).isEqualTo("활성");
        assertThat(ReviewStatus.HIDDEN.getDescription()).isEqualTo("숨김");
        assertThat(ReviewStatus.DELETED.getDescription()).isEqualTo("삭제됨");
    }

    @Test
    @DisplayName("ReviewStatus enum의 개수를 확인할 수 있다")
    void checkReviewStatusCount() {
        // when
        ReviewStatus[] values = ReviewStatus.values();

        // then
        assertThat(values).hasSize(3);
    }
}