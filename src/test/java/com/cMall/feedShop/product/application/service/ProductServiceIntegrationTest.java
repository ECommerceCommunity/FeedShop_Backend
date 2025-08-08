package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.request.ProductImageRequest;
import com.cMall.feedShop.product.application.dto.request.ProductOptionRequest;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.store.domain.model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductService 통합 테스트")
class ProductServiceIntegrationTest {

    private ProductService productService;
    private Store store;
    private Category category;

    @BeforeEach
    void setUp() {
        productService = new ProductService(null, null, null, null, null, null, null);

        store = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);

        category = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(category, "categoryId", 1L);
    }

    @Test
    @DisplayName("상품 이미지 생성 메서드 테스트")
    void createProductImages_MethodTest() throws Exception {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        ProductImageRequest imageRequest1 = new ProductImageRequest();
        ReflectionTestUtils.setField(imageRequest1, "url", "http://main.jpg");
        ReflectionTestUtils.setField(imageRequest1, "type", ImageType.MAIN);

        ProductImageRequest imageRequest2 = new ProductImageRequest();
        ReflectionTestUtils.setField(imageRequest2, "url", "http://detail.jpg");
        ReflectionTestUtils.setField(imageRequest2, "type", ImageType.DETAIL);

        List<ProductImageRequest> requests = Arrays.asList(imageRequest1, imageRequest2);

        // when - 리플렉션을 사용하여 private 메서드 호출
        java.lang.reflect.Method method = ProductService.class
                .getDeclaredMethod("createProductImages", Product.class, List.class);
        method.setAccessible(true);
        method.invoke(productService, product, requests);

        // then
        assertThat(product.getProductImages()).hasSize(2);
        assertThat(product.getProductImages().get(0).getUrl()).isEqualTo("http://main.jpg");
        assertThat(product.getProductImages().get(0).getType()).isEqualTo(ImageType.MAIN);
        assertThat(product.getProductImages().get(1).getUrl()).isEqualTo("http://detail.jpg");
        assertThat(product.getProductImages().get(1).getType()).isEqualTo(ImageType.DETAIL);
    }

    @Test
    @DisplayName("상품 옵션 생성 메서드 테스트")
    void createProductOptions_MethodTest() throws Exception {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        ProductOptionRequest optionRequest1 = new ProductOptionRequest();
        ReflectionTestUtils.setField(optionRequest1, "gender", Gender.MEN);
        ReflectionTestUtils.setField(optionRequest1, "size", Size.SIZE_270);
        ReflectionTestUtils.setField(optionRequest1, "color", Color.BLACK);
        ReflectionTestUtils.setField(optionRequest1, "stock", 50);

        ProductOptionRequest optionRequest2 = new ProductOptionRequest();
        ReflectionTestUtils.setField(optionRequest2, "gender", Gender.WOMEN);
        ReflectionTestUtils.setField(optionRequest2, "size", Size.SIZE_240);
        ReflectionTestUtils.setField(optionRequest2, "color", Color.WHITE);
        ReflectionTestUtils.setField(optionRequest2, "stock", 30);

        List<ProductOptionRequest> requests = Arrays.asList(optionRequest1, optionRequest2);

        // when - 리플렉션을 사용하여 public 메서드 호출
        productService.createProductOptions(product, requests);

        // then
        assertThat(product.getProductOptions()).hasSize(2);
        assertThat(product.getProductOptions().get(0).getGender()).isEqualTo(Gender.MEN);
        assertThat(product.getProductOptions().get(0).getSize()).isEqualTo(Size.SIZE_270);
        assertThat(product.getProductOptions().get(0).getColor()).isEqualTo(Color.BLACK);
        assertThat(product.getProductOptions().get(0).getStock()).isEqualTo(50);

        assertThat(product.getProductOptions().get(1).getGender()).isEqualTo(Gender.WOMEN);
        assertThat(product.getProductOptions().get(1).getSize()).isEqualTo(Size.SIZE_240);
        assertThat(product.getProductOptions().get(1).getColor()).isEqualTo(Color.WHITE);
        assertThat(product.getProductOptions().get(1).getStock()).isEqualTo(30);
    }

    @Test
    @DisplayName("빈 이미지 리스트로 상품 이미지 생성")
    void createProductImages_EmptyList() throws Exception {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        List<ProductImageRequest> emptyRequests = List.of();

        // when
        java.lang.reflect.Method method = ProductService.class
                .getDeclaredMethod("createProductImages", Product.class, List.class);
        method.setAccessible(true);
        method.invoke(productService, product, emptyRequests);

        // then
        assertThat(product.getProductImages()).isEmpty();
    }

    @Test
    @DisplayName("빈 옵션 리스트로 상품 옵션 생성")
    void createProductOptions_EmptyList() {
        // given
        Product product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .build();

        List<ProductOptionRequest> emptyRequests = List.of();

        // when
        productService.createProductOptions(product, emptyRequests);

        // then
        assertThat(product.getProductOptions()).isEmpty();
    }
}