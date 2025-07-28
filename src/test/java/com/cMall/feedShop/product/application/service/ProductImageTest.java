package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.domain.enums.ImageType;
import com.cMall.feedShop.product.domain.model.ProductImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductImage 도메인 테스트")
class ProductImageTest {

    @Test
    @DisplayName("ProductImage 생성 성공 - MAIN 타입")
    void createProductImage_Success_MainType() {
        // given & when
        ProductImage image = new ProductImage("http://main.jpg", ImageType.MAIN, null);
        ReflectionTestUtils.setField(image, "imageId", 1L);

        // then
        assertThat(image.getImageId()).isEqualTo(1L);
        assertThat(image.getUrl()).isEqualTo("http://main.jpg");
        assertThat(image.getType()).isEqualTo(ImageType.MAIN);
    }

    @Test
    @DisplayName("ProductImage 생성 성공 - DETAIL 타입")
    void createProductImage_Success_DetailType() {
        // given & when
        ProductImage image = new ProductImage("http://detail.jpg", ImageType.DETAIL, null);
        ReflectionTestUtils.setField(image, "imageId", 2L);

        // then
        assertThat(image.getImageId()).isEqualTo(2L);
        assertThat(image.getUrl()).isEqualTo("http://detail.jpg");
        assertThat(image.getType()).isEqualTo(ImageType.DETAIL);
    }

    @Test
    @DisplayName("ProductImage URL 유효성")
    void productImageUrl_Validation() {
        // given
        String longUrl = "https://example.com/very/long/path/to/product/image/with/many/directories/image.jpg";

        // when
        ProductImage image = new ProductImage(longUrl, ImageType.MAIN, null);

        // then
        assertThat(image.getUrl()).isEqualTo(longUrl);
        assertThat(image.getUrl()).contains("https://");
        assertThat(image.getUrl()).endsWith(".jpg");
    }
}