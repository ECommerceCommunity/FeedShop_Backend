package com.cMall.feedShop.review.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Stability Enum 테스트")
class StabilityTest {

    @Test
    @DisplayName("Stability enum의 모든 값과 설명을 확인할 수 있다")
    void checkAllStabilityValues() {
        // when & then
        assertThat(Stability.VERY_UNSTABLE.getDescription()).isEqualTo("매우 불안정");
        assertThat(Stability.UNSTABLE.getDescription()).isEqualTo("불안정");
        assertThat(Stability.NORMAL.getDescription()).isEqualTo("보통");
        assertThat(Stability.STABLE.getDescription()).isEqualTo("안정적");
        assertThat(Stability.VERY_STABLE.getDescription()).isEqualTo("매우 안정적");
    }

    @Test
    @DisplayName("Stability enum의 개수를 확인할 수 있다")
    void checkStabilityCount() {
        // when
        Stability[] values = Stability.values();

        // then
        assertThat(values).hasSize(5);
    }
}