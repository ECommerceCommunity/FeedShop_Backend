package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.response.info.ProductImageInfo;
import com.cMall.feedShop.product.application.dto.response.info.ProductOptionInfo;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Product DTO 테스트")
class ProductDtoTests {

    @Test
    @DisplayName("ProductImageInfo from 메서드 테스트")
    void productImageInfo_From_Test() {
        // given
        ProductImage productImage = new ProductImage("http://test.jpg", ImageType.MAIN, null);
        ReflectionTestUtils.setField(productImage, "imageId", 1L);

        // when
        ProductImageInfo imageInfo = ProductImageInfo.from(productImage);

        // then
        assertThat(imageInfo).isNotNull();
        assertThat(imageInfo.getImageId()).isEqualTo(1L);
        assertThat(imageInfo.getUrl()).isEqualTo("http://test.jpg");
        assertThat(imageInfo.getType()).isEqualTo(ImageType.MAIN);
    }

    @Test
    @DisplayName("ProductImageInfo fromList 메서드 테스트")
    void productImageInfo_FromList_Test() {
        // given
        ProductImage image1 = new ProductImage("http://main.jpg", ImageType.MAIN, null);
        ReflectionTestUtils.setField(image1, "imageId", 1L);

        ProductImage image2 = new ProductImage("http://detail.jpg", ImageType.DETAIL, null);
        ReflectionTestUtils.setField(image2, "imageId", 2L);

        List<ProductImage> images = Arrays.asList(image1, image2);

        // when
        List<ProductImageInfo> imageInfos = ProductImageInfo.fromList(images);

        // then
        assertThat(imageInfos).hasSize(2);
        assertThat(imageInfos.get(0).getImageId()).isEqualTo(1L);
        assertThat(imageInfos.get(0).getType()).isEqualTo(ImageType.MAIN);
        assertThat(imageInfos.get(1).getImageId()).isEqualTo(2L);
        assertThat(imageInfos.get(1).getType()).isEqualTo(ImageType.DETAIL);
    }

    @Test
    @DisplayName("ProductOptionInfo from 메서드 테스트")
    void productOptionInfo_From_Test() {
        // given
        ProductOption productOption = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 100, null);
        ReflectionTestUtils.setField(productOption, "optionId", 1L);

        // when
        ProductOptionInfo optionInfo = ProductOptionInfo.from(productOption);

        // then
        assertThat(optionInfo).isNotNull();
        assertThat(optionInfo.getOptionId()).isEqualTo(1L);
        assertThat(optionInfo.getGender()).isEqualTo(Gender.UNISEX);
        assertThat(optionInfo.getSize()).isEqualTo(Size.SIZE_250);
        assertThat(optionInfo.getColor()).isEqualTo(Color.WHITE);
        assertThat(optionInfo.getStock()).isEqualTo(100);
    }

    @Test
    @DisplayName("ProductOptionInfo fromList 메서드 테스트")
    void productOptionInfo_FromList_Test() {
        // given
        ProductOption option1 = new ProductOption(Gender.MEN, Size.SIZE_270, Color.BLACK, 50, null);
        ReflectionTestUtils.setField(option1, "optionId", 1L);

        ProductOption option2 = new ProductOption(Gender.WOMEN, Size.SIZE_240, Color.RED, 30, null);
        ReflectionTestUtils.setField(option2, "optionId", 2L);

        List<ProductOption> options = Arrays.asList(option1, option2);

        // when
        List<ProductOptionInfo> optionInfos = ProductOptionInfo.fromList(options);

        // then
        assertThat(optionInfos).hasSize(2);
        assertThat(optionInfos.get(0).getOptionId()).isEqualTo(1L);
        assertThat(optionInfos.get(0).getGender()).isEqualTo(Gender.MEN);
        assertThat(optionInfos.get(0).getSize()).isEqualTo(Size.SIZE_270);
        assertThat(optionInfos.get(1).getOptionId()).isEqualTo(2L);
        assertThat(optionInfos.get(1).getGender()).isEqualTo(Gender.WOMEN);
        assertThat(optionInfos.get(1).getColor()).isEqualTo(Color.RED);
    }

    @Test
    @DisplayName("ProductImageInfo builder 패턴 테스트")
    void productImageInfo_Builder_Test() {
        // given & when
        ProductImageInfo imageInfo = ProductImageInfo.builder()
                .imageId(1L)
                .url("http://test.jpg")
                .type(ImageType.MAIN)
                .build();

        // then
        assertThat(imageInfo.getImageId()).isEqualTo(1L);
        assertThat(imageInfo.getUrl()).isEqualTo("http://test.jpg");
        assertThat(imageInfo.getType()).isEqualTo(ImageType.MAIN);
    }

    @Test
    @DisplayName("ProductOptionInfo builder 패턴 테스트")
    void productOptionInfo_Builder_Test() {
        // given & when
        ProductOptionInfo optionInfo = ProductOptionInfo.builder()
                .optionId(1L)
                .gender(Gender.UNISEX)
                .size(Size.SIZE_250)
                .color(Color.WHITE)
                .stock(100)
                .build();

        // then
        assertThat(optionInfo.getOptionId()).isEqualTo(1L);
        assertThat(optionInfo.getGender()).isEqualTo(Gender.UNISEX);
        assertThat(optionInfo.getSize()).isEqualTo(Size.SIZE_250);
        assertThat(optionInfo.getColor()).isEqualTo(Color.WHITE);
        assertThat(optionInfo.getStock()).isEqualTo(100);
    }

    @Test
    @DisplayName("빈 리스트 변환 테스트")
    void empty_List_Conversion_Test() {
        // given
        List<ProductImage> emptyImages = List.of();
        List<ProductOption> emptyOptions = List.of();

        // when
        List<ProductImageInfo> imageInfos = ProductImageInfo.fromList(emptyImages);
        List<ProductOptionInfo> optionInfos = ProductOptionInfo.fromList(emptyOptions);

        // then
        assertThat(imageInfos).isEmpty();
        assertThat(optionInfos).isEmpty();
    }
}