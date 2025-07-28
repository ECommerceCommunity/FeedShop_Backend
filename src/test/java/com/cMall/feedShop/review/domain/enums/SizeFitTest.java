package com.cMall.feedShop.review.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SizeFit Enum 테스트")
class SizeFitTest {

    @Test
    @DisplayName("SizeFit enum의 모든 값과 설명을 확인할 수 있다")
    void checkAllSizeFitValues() {
        // when & then
        assertThat(SizeFit.VERY_SMALL.getDescription()).isEqualTo("매우 작음");
        assertThat(SizeFit.SMALL.getDescription()).isEqualTo("작음");
        assertThat(SizeFit.NORMAL.getDescription()).isEqualTo("보통");
        assertThat(SizeFit.BIG.getDescription()).isEqualTo("큼");
        assertThat(SizeFit.VERY_BIG.getDescription()).isEqualTo("매우 큼");
    }

    @Test
    @DisplayName("SizeFit enum의 개수를 확인할 수 있다")
    void checkSizeFitCount() {
        // when
        SizeFit[] values = SizeFit.values();

        // then
        assertThat(values).hasSize(5);
    }
}