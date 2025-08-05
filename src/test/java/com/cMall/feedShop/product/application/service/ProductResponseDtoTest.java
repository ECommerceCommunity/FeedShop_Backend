package com.cMall.feedShop.product.application.dto.response;

import com.cMall.feedShop.product.domain.enums.CategoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Product Response DTO 테스트")
class ResponseDtoTests {

    @Test
    @DisplayName("ProductCreateResponse of 메서드 테스트")
    void productCreateResponse_Of_Test() {
        // when
        ProductCreateResponse response = ProductCreateResponse.of(1L);

        // then
        assertThat(response.getProductId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ProductListResponse of 메서드 테스트")
    void productListResponse_Of_Test() {
        // when
        ProductListResponse response = ProductListResponse.of(
                1L, "상품명", new BigDecimal("50000"), new BigDecimal("45000"),
                1L, 1L,"스토어명", 10, "http://image.jpg");

        // then
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("상품명");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("50000"));
        assertThat(response.getDiscountPrice()).isEqualTo(new BigDecimal("45000"));
        assertThat(response.getStoreId()).isEqualTo(1L);
        assertThat(response.getStoreName()).isEqualTo("스토어명");
        assertThat(response.getWishNumber()).isEqualTo(10);
        assertThat(response.getMainImageUrl()).isEqualTo("http://image.jpg");
    }

    @Test
    @DisplayName("ProductListResponse null wishNumber 처리 테스트")
    void productListResponse_NullWishNumber_Test() {
        // when
        ProductListResponse response = ProductListResponse.of(
                1L, "상품명", new BigDecimal("50000"), new BigDecimal("45000"),
                1L, 1L, "스토어명", null, "http://image.jpg");

        // then
        assertThat(response.getWishNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("ProductPageResponse of 메서드 테스트")
    void productPageResponse_Of_Test() {
        // given
        ProductListResponse product1 = ProductListResponse.of(
                1L, "상품1", new BigDecimal("50000"), new BigDecimal("45000"),
                1L, 1L,"스토어1", 5, "http://image1.jpg");

        ProductListResponse product2 = ProductListResponse.of(
                2L, "상품2", new BigDecimal("30000"), new BigDecimal("30000"),
                1L, 1L,"스토어1", 3, "http://image2.jpg");

        List<ProductListResponse> content = Arrays.asList(product1, product2);
        Page<ProductListResponse> page = new PageImpl<>(content, PageRequest.of(0, 20), 2);

        // when
        ProductPageResponse response = ProductPageResponse.of(page);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2L);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(20);
        assertThat(response.getNumber()).isEqualTo(0);
        assertThat(response.getContent().get(0).getName()).isEqualTo("상품1");
        assertThat(response.getContent().get(1).getName()).isEqualTo("상품2");
    }

    @Test
    @DisplayName("CategoryResponse from 메서드 테스트")
    void categoryResponse_From_Test() {
        // given
        com.cMall.feedShop.product.domain.model.Category category =
                new com.cMall.feedShop.product.domain.model.Category(CategoryType.SNEAKERS, "운동화");
        org.springframework.test.util.ReflectionTestUtils.setField(category, "categoryId", 1L);

        // when
        CategoryResponse response = CategoryResponse.from(category);

        // then
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getType()).isEqualTo(CategoryType.SNEAKERS);
        assertThat(response.getName()).isEqualTo("운동화");
    }

    @Test
    @DisplayName("ProductDetailResponse of 메서드 테스트")
    void productDetailResponse_Of_Test() {
        // when
        ProductDetailResponse response = ProductDetailResponse.of(
                1L, "상품명", new BigDecimal("50000"),
                com.cMall.feedShop.product.domain.enums.DiscountType.RATE_DISCOUNT,
                new BigDecimal("10"), new BigDecimal("45000"), null, "설명",
                1L, "스토어명", CategoryType.SNEAKERS, "운동화",
                List.of(), List.of(),
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now());

        // then
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("상품명");
        assertThat(response.getWishNumber()).isEqualTo(0); // null이 0으로 변환됨
        assertThat(response.getImages()).isEmpty();
        assertThat(response.getOptions()).isEmpty();
    }

    @Test
    @DisplayName("빈 페이지 응답 테스트")
    void emptyPage_Response_Test() {
        // given
        Page<ProductListResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        // when
        ProductPageResponse response = ProductPageResponse.of(emptyPage);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0L);
        assertThat(response.getTotalPages()).isEqualTo(0);
    }
}