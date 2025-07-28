package com.cMall.feedShop.product.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Product Enum 테스트")
class ProductEnumTests {

    @Test
    @DisplayName("Size enum 값과 문자열 변환 테스트")
    void size_Value_Conversion_Test() {
        // given & when & then
        assertThat(Size.SIZE_230.getValue()).isEqualTo("230");
        assertThat(Size.SIZE_250.getValue()).isEqualTo("250");
        assertThat(Size.SIZE_300.getValue()).isEqualTo("300");
        assertThat(Size.SIZE_230.toString()).isEqualTo("230");
    }

    @Test
    @DisplayName("Size fromValue 정상 변환 테스트")
    void size_FromValue_Success_Test() {
        // given & when & then
        assertThat(Size.fromValue("230")).isEqualTo(Size.SIZE_230);
        assertThat(Size.fromValue("250")).isEqualTo(Size.SIZE_250);
        assertThat(Size.fromValue("300")).isEqualTo(Size.SIZE_300);
    }

    @Test
    @DisplayName("Size fromValue 잘못된 값 예외 테스트")
    void size_FromValue_Invalid_Exception_Test() {
        // when & then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                Size.fromValue("999"));

        assertThat(thrown.getMessage()).contains("Invalid size: 999");
    }

    @Test
    @DisplayName("Size 모든 값 존재 확인 테스트")
    void size_All_Values_Test() {
        // given & when
        Size[] allSizes = Size.values();

        // then
        assertThat(allSizes).hasSize(15);
        assertThat(allSizes).contains(
                Size.SIZE_230, Size.SIZE_235, Size.SIZE_240, Size.SIZE_245, Size.SIZE_250,
                Size.SIZE_255, Size.SIZE_260, Size.SIZE_265, Size.SIZE_270, Size.SIZE_275,
                Size.SIZE_280, Size.SIZE_285, Size.SIZE_290, Size.SIZE_295, Size.SIZE_300
        );
    }

    @Test
    @DisplayName("Gender enum 모든 값 테스트")
    void gender_All_Values_Test() {
        // given & when
        Gender[] allGenders = Gender.values();

        // then
        assertThat(allGenders).hasSize(3);
        assertThat(allGenders).contains(Gender.MEN, Gender.WOMEN, Gender.UNISEX);
    }

    @Test
    @DisplayName("Color enum 주요 색상 테스트")
    void color_Main_Colors_Test() {
        // given & when
        Color[] allColors = Color.values();

        // then
        assertThat(allColors).contains(
                Color.WHITE, Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
                Color.YELLOW, Color.PINK, Color.ORANGE, Color.PURPLE, Color.BROWN
        );
        assertThat(allColors).hasSize(44); // 전체 색상 개수 확인
    }

    @Test
    @DisplayName("ImageType enum 값 테스트")
    void imageType_Values_Test() {
        // given & when
        ImageType[] allTypes = ImageType.values();

        // then
        assertThat(allTypes).hasSize(2);
        assertThat(allTypes).contains(ImageType.MAIN, ImageType.DETAIL);
    }

    @Test
    @DisplayName("DiscountType enum 값 테스트")
    void discountType_Values_Test() {
        // given & when
        DiscountType[] allTypes = DiscountType.values();

        // then
        assertThat(allTypes).hasSize(3);
        assertThat(allTypes).contains(
                DiscountType.NONE,
                DiscountType.FIXED_DISCOUNT,
                DiscountType.RATE_DISCOUNT
        );
    }
}