package com.cMall.feedShop.review.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cushion Enum 테스트")
class CushionTest {

    @Test
    @DisplayName("Cushion enum의 모든 값과 설명을 확인할 수 있다")
    void checkAllCushionValues() {
        // when & then
        assertThat(Cushion.VERY_SOFT.getDescription()).isEqualTo("매우 부드러움");
        assertThat(Cushion.SOFT.getDescription()).isEqualTo("부드러움");
        assertThat(Cushion.MEDIUM.getDescription()).isEqualTo("보통");
        assertThat(Cushion.FIRM.getDescription()).isEqualTo("단단함");
        assertThat(Cushion.VERY_FIRM.getDescription()).isEqualTo("매우 단단함");
    }

    @Test
    @DisplayName("Cushion enum의 개수를 확인할 수 있다")
    void checkCushionCount() {
        // when
        Cushion[] values = Cushion.values();

        // then
        assertThat(values).hasSize(5);
    }
}
