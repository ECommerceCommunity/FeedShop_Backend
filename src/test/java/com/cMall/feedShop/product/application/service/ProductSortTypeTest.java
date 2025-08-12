package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.domain.enums.ProductSortType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSortType enum 테스트")
class ProductSortTypeTest {

    @Test
    @DisplayName("fromCode 메서드 - latest 문자열을 LATEST enum으로 변환")
    void fromCode_Latest_ReturnsLatest() {
        // when
        ProductSortType result = ProductSortType.fromCode("latest");

        // then
        assertThat(result).isEqualTo(ProductSortType.LATEST);
    }

    @Test
    @DisplayName("fromCode 메서드 - popular 문자열을 POPULAR enum으로 변환")
    void fromCode_Popular_ReturnsPopular() {
        // when
        ProductSortType result = ProductSortType.fromCode("popular");

        // then
        assertThat(result).isEqualTo(ProductSortType.POPULAR);
    }

    @Test
    @DisplayName("fromCode 메서드 - 대소문자 구분 없이 처리")
    void fromCode_CaseInsensitive_ReturnsCorrectEnum() {
        // when & then
        assertThat(ProductSortType.fromCode("LATEST")).isEqualTo(ProductSortType.LATEST);
        assertThat(ProductSortType.fromCode("Popular")).isEqualTo(ProductSortType.POPULAR);
        assertThat(ProductSortType.fromCode("lAtEsT")).isEqualTo(ProductSortType.LATEST);
    }

    @Test
    @DisplayName("fromCode 메서드 - null 값은 LATEST 기본값 반환")
    void fromCode_Null_ReturnsLatest() {
        // when
        ProductSortType result = ProductSortType.fromCode(null);

        // then
        assertThat(result).isEqualTo(ProductSortType.LATEST);
    }

    @Test
    @DisplayName("fromCode 메서드 - 빈 문자열은 LATEST 기본값 반환")
    void fromCode_EmptyString_ReturnsLatest() {
        // when
        ProductSortType result1 = ProductSortType.fromCode("");
        ProductSortType result2 = ProductSortType.fromCode("   ");

        // then
        assertThat(result1).isEqualTo(ProductSortType.LATEST);
        assertThat(result2).isEqualTo(ProductSortType.LATEST);
    }

    @Test
    @DisplayName("fromCode 메서드 - 잘못된 값은 LATEST 기본값 반환")
    void fromCode_InvalidValue_ReturnsLatest() {
        // when
        ProductSortType result1 = ProductSortType.fromCode("invalid");
        ProductSortType result2 = ProductSortType.fromCode("random");
        ProductSortType result3 = ProductSortType.fromCode("123");

        // then
        assertThat(result1).isEqualTo(ProductSortType.LATEST);
        assertThat(result2).isEqualTo(ProductSortType.LATEST);
        assertThat(result3).isEqualTo(ProductSortType.LATEST);
    }

    @Test
    @DisplayName("getCode 메서드 - enum의 코드 값 반환")
    void getCode_ReturnsCorrectCode() {
        // when & then
        assertThat(ProductSortType.LATEST.getCode()).isEqualTo("latest");
        assertThat(ProductSortType.POPULAR.getCode()).isEqualTo("popular");
    }

    @Test
    @DisplayName("getDescription 메서드 - enum의 설명 반환")
    void getDescription_ReturnsCorrectDescription() {
        // when & then
        assertThat(ProductSortType.LATEST.getDescription()).isEqualTo("최신순");
        assertThat(ProductSortType.POPULAR.getDescription()).isEqualTo("인기순");
    }
}