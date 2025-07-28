package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.model.ProductOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductOption 도메인 테스트")
class ProductOptionTest {

    @Test
    @DisplayName("ProductOption 생성 성공")
    void createProductOption_Success() {
        // given & when
        ProductOption option = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 100, null);
        ReflectionTestUtils.setField(option, "optionId", 1L);

        // then
        assertThat(option.getOptionId()).isEqualTo(1L);
        assertThat(option.getGender()).isEqualTo(Gender.UNISEX);
        assertThat(option.getSize()).isEqualTo(Size.SIZE_250);
        assertThat(option.getColor()).isEqualTo(Color.WHITE);
        assertThat(option.getStock()).isEqualTo(100);
    }

    @Test
    @DisplayName("재고 확인 - 재고 있음")
    void isInStock_True() {
        // given
        ProductOption option = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 10, null);

        // when & then
        assertThat(option.isInStock()).isTrue();
    }

    @Test
    @DisplayName("재고 확인 - 재고 없음 (0개)")
    void isInStock_False_ZeroStock() {
        // given
        ProductOption option = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 0, null);

        // when & then
        assertThat(option.isInStock()).isFalse();
    }

    @Test
    @DisplayName("재고 확인 - 재고 없음 (null)")
    void isInStock_False_NullStock() {
        // given
        ProductOption option = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, null, null);

        // when & then
        assertThat(option.isInStock()).isFalse();
    }

    @Test
    @DisplayName("재고 확인 - 경계값 테스트 (1개)")
    void isInStock_True_OneStock() {
        // given
        ProductOption option = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 1, null);

        // when & then
        assertThat(option.isInStock()).isTrue();
    }
}