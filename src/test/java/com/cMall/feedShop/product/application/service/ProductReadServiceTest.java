package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.response.ProductDetailResponse;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductReadService 테스트")
class ProductReadServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private DiscountCalculator discountCalculator;

    @InjectMocks
    private ProductReadService productReadService;

    private Product product1;
    private Product product2;
    private Store store;
    private Category category;

    @BeforeEach
    void setUp() {
        setupStore();
        setupCategory();
        setupProducts();
    }

    private void setupStore() {
        store = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);
    }

    private void setupCategory() {
        category = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(category, "categoryId", 1L);
    }

    private void setupProducts() {
        product1 = Product.builder()
                .name("상품1")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(new BigDecimal("10"))
                .description("상품1 설명")
                .build();
        ReflectionTestUtils.setField(product1, "productId", 1L);
        ReflectionTestUtils.setField(product1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(product1, "updatedAt", LocalDateTime.now());

        // 상품1에 이미지 추가
        ProductImage mainImage1 = new ProductImage("http://main1.jpg", ImageType.MAIN, product1);
        product1.getProductImages().add(mainImage1);

        product2 = Product.builder()
                .name("상품2")
                .price(new BigDecimal("30000"))
                .store(store)
                .category(category)
                .discountType(DiscountType.NONE)
                .build();
        ReflectionTestUtils.setField(product2, "productId", 2L);
        ReflectionTestUtils.setField(product2, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(product2, "updatedAt", LocalDateTime.now());

        // 상품2에 이미지 추가
        ProductImage mainImage2 = new ProductImage("http://main2.jpg", ImageType.MAIN, product2);
        product2.getProductImages().add(mainImage2);
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - 기본 파라미터")
    void getProductList_Success_DefaultParams() {
        // given
        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 20), 2);

        given(productRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).willReturn(productPage);
        given(discountCalculator.calculateDiscountPrice(any(), any(), any()))
                .willReturn(new BigDecimal("45000"))
                .willReturn(new BigDecimal("30000"));

        // when
        ProductPageResponse response = productReadService.getProductList(0, 20, null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getNumber()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(20);

        verify(productRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - 음수 페이지는 0으로 조정")
    void getProductList_Success_NegativePageAdjusted() {
        // given
        Page<Product> productPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        given(productRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).willReturn(productPage);

        // when
        ProductPageResponse response = productReadService.getProductList(-5, 20, null);

        // then
        assertThat(response.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("상품 목록 조회 성공 - 잘못된 사이즈는 20으로 조정")
    void getProductList_Success_InvalidSizeAdjusted() {
        // given
        Page<Product> productPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        given(productRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).willReturn(productPage);

        // when
        ProductPageResponse response1 = productReadService.getProductList(0, 0, null);
        ProductPageResponse response2 = productReadService.getProductList(0, 150, null);

        // then
        assertThat(response1.getSize()).isEqualTo(20);
        assertThat(response2.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductDetail_Success() {
        // given
        Long productId = 1L;

        // 상품 옵션 추가
        ProductOption option1 = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 10, product1);
        ProductOption option2 = new ProductOption(Gender.UNISEX, Size.SIZE_255, Color.BLACK, 5, product1);
        product1.getProductOptions().addAll(Arrays.asList(option1, option2));

        given(productRepository.findByProductId(productId)).willReturn(Optional.of(product1));
        given(discountCalculator.calculateDiscountPrice(any(), any(), any()))
                .willReturn(new BigDecimal("45000"));

        // when
        ProductDetailResponse response = productReadService.getProductDetail(productId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(productId);
        assertThat(response.getName()).isEqualTo("상품1");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("50000"));
        assertThat(response.getDiscountPrice()).isEqualTo(new BigDecimal("45000"));
        assertThat(response.getStoreName()).isEqualTo("테스트 스토어");
        assertThat(response.getCategoryName()).isEqualTo("운동화");
        assertThat(response.getImages()).hasSize(1);
        assertThat(response.getOptions()).hasSize(2);

        verify(productRepository, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 존재하지 않는 상품")
    void getProductDetail_Fail_ProductNotFound() {
        // given
        Long productId = 999L;
        given(productRepository.findByProductId(productId)).willReturn(Optional.empty());

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class,
                () -> productReadService.getProductDetail(productId)
        );

        assertThat(thrown.getErrorCode().getMessage()).contains("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 상세 조회 성공 - 옵션과 이미지가 없는 경우")
    void getProductDetail_Success_NoOptionsNoImages() {
        // given
        Long productId = 2L;
        product2.getProductImages().clear();

        given(productRepository.findByProductId(productId)).willReturn(Optional.of(product2));
        given(discountCalculator.calculateDiscountPrice(any(), any(), any()))
                .willReturn(new BigDecimal("30000"));

        // when
        ProductDetailResponse response = productReadService.getProductDetail(productId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getImages()).isEmpty();
        assertThat(response.getOptions()).isEmpty();
        assertThat(response.getWishNumber()).isEqualTo(0);
    }
}