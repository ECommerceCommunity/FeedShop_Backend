package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.domain.converter.SizeConverter;
import com.cMall.feedShop.product.domain.enums.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SizeConverter 테스트")
class SizeConverterTest {

    private SizeConverter sizeConverter;

    @BeforeEach
    void setUp() {
        sizeConverter = new SizeConverter();
    }

    @Test
    @DisplayName("Size를 DB 컬럼 값으로 변환 성공")
    void convertToDatabaseColumn_Success() {
        // given
        Size size250 = Size.SIZE_250;
        Size size270 = Size.SIZE_270;

        // when
        String dbValue250 = sizeConverter.convertToDatabaseColumn(size250);
        String dbValue270 = sizeConverter.convertToDatabaseColumn(size270);

        // then
        assertThat(dbValue250).isEqualTo("250");
        assertThat(dbValue270).isEqualTo("270");
    }

    @Test
    @DisplayName("null Size를 DB 컬럼 값으로 변환 시 null 반환")
    void convertToDatabaseColumn_Null() {
        // when
        String dbValue = sizeConverter.convertToDatabaseColumn(null);

        // then
        assertThat(dbValue).isNull();
    }

    @Test
    @DisplayName("DB 컬럼 값을 Size enum으로 변환 성공")
    void convertToEntityAttribute_Success() {
        // given
        String dbValue250 = "250";
        String dbValue270 = "270";

        // when
        Size size250 = sizeConverter.convertToEntityAttribute(dbValue250);
        Size size270 = sizeConverter.convertToEntityAttribute(dbValue270);

        // then
        assertThat(size250).isEqualTo(Size.SIZE_250);
        assertThat(size270).isEqualTo(Size.SIZE_270);
    }

    @Test
    @DisplayName("null DB 값을 Size enum으로 변환 시 null 반환")
    void convertToEntityAttribute_Null() {
        // when
        Size size = sizeConverter.convertToEntityAttribute(null);

        // then
        assertThat(size).isNull();
    }

    @Test
    @DisplayName("모든 Size enum 값에 대한 변환 테스트")
    void convertAll_SizeValues() {
        // given
        Size[] allSizes = Size.values();

        // when & then
        for (Size size : allSizes) {
            String dbValue = sizeConverter.convertToDatabaseColumn(size);
            Size convertedBack = sizeConverter.convertToEntityAttribute(dbValue);

            assertThat(convertedBack).isEqualTo(size);
            assertThat(dbValue).isEqualTo(size.getValue());
        }
    }
}