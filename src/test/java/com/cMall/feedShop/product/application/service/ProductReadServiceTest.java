package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.response.ProductDetailResponse;
import com.cMall.feedShop.product.application.dto.response.ProductListResponse;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
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
import java.util.Arrays;
import java.util.Collections;
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

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductReadService productReadService;

    private Product product1;
    private Product product2;
    private Store store;
    private Category category;
    private ProductListResponse listResponse1;
    private ProductListResponse listResponse2;
    private ProductDetailResponse detailResponse;

    @BeforeEach
    void setUp() {
        setupStore();
        setupCategory();
        setupProducts();
        setupResponses();
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
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(new BigDecimal("10"))
                .description("상품 1 설명")
                .store(store)
                .category(category)
                .build();
        ReflectionTestUtils.setField(product1, "productId", 1L);

        product2 = Product.builder()
                .name("상품2")
                .price(new BigDecimal("30000"))
                .discountType(DiscountType.NONE)
                .description("상품 2 설명")
                .store(store)
                .category(category)
                .build();
        ReflectionTestUtils.setField(product2, "productId", 2L);
    }

    private void setupResponses() {
        listResponse1 = ProductListResponse.builder()
                .productId(1L)
                .name("상품1")
                .price(new BigDecimal("50000"))
                .discountPrice(new BigDecimal("45000"))
                .categoryId(1L)
                .storeId(1L)
                .storeName("테스트 스토어")
                .build();

        listResponse2 = ProductListResponse.builder()
                .productId(2L)
                .name("상품2")
                .price(new BigDecimal("30000"))
                .discountPrice(new BigDecimal("30000"))
                .categoryId(1L)
                .storeId(1L)
                .storeName("테스트 스토어")
                .build();

        detailResponse = ProductDetailResponse.builder()
                .productId(1L)
                .name("상품1")
                .price(new BigDecimal("50000"))
                .discountPrice(new BigDecimal("45000"))
                .storeName("테스트 스토어")
                .categoryName("운동화")
                .description("상품 1 설명")
                .build();
    }

    @Test
    @DisplayName("상품 목록 조회 성공")
    void getProductList_Success() {
        // given
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product1, product2), PageRequest.of(0, 20), 2);

        given(productRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(product1)).willReturn(listResponse1);
        given(productMapper.toListResponse(product2)).willReturn(listResponse2);

        // when
        ProductPageResponse response = productReadService.getProductList(0, 20, null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent().get(0).getName()).isEqualTo("상품1");
        assertThat(response.getContent().get(1).getName()).isEqualTo("상품2");

        verify(productRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("상품 목록 조회 - 잘못된 페이지 파라미터 처리")
    void getProductList_InvalidParameters() {
        // given
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product1), PageRequest.of(0, 20), 1);

        given(productRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(product1)).willReturn(listResponse1);

        // when - 음수 페이지와 큰 사이즈로 호출
        ProductPageResponse response = productReadService.getProductList(-1, 200, null);

        // then
        assertThat(response).isNotNull();
        verify(productRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("빈 상품 목록 조회")
    void getProductList_EmptyResult() {
        // given
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);

        given(productRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .willReturn(emptyPage);

        // when
        ProductPageResponse response = productReadService.getProductList(0, 20, null);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductDetail_Success() {
        // given
        Long productId = 1L;
        given(productRepository.findByProductId(productId)).willReturn(Optional.of(product1));
        given(productMapper.toDetailResponse(product1)).willReturn(detailResponse);

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

        verify(productRepository, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 존재하지 않는 상품")
    void getProductDetail_ProductNotFound() {
        // given
        Long productId = 999L;
        given(productRepository.findByProductId(productId)).willReturn(Optional.empty());

        // when & then
        assertThrows(ProductException.class,
                () -> productReadService.getProductDetail(productId));

        verify(productRepository, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("상품 목록 조회 - 최소 사이즈 테스트")
    void getProductList_MinSize() {
        // given
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product1), PageRequest.of(0, 20), 1);

        given(productRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(product1)).willReturn(listResponse1);

        // when - 사이즈 0으로 호출
        ProductPageResponse response = productReadService.getProductList(0, 0, null);

        // then
        assertThat(response).isNotNull();
        verify(productRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("상품 목록 조회 - 최대 사이즈 초과 테스트")
    void getProductList_MaxSizeExceeded() {
        // given
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product1), PageRequest.of(0, 20), 1);

        given(productRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(product1)).willReturn(listResponse1);

        // when - 사이즈 150으로 호출 (최대 50 초과)
        ProductPageResponse response = productReadService.getProductList(0, 150, null);

        // then
        assertThat(response).isNotNull();
        verify(productRepository, times(1)).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }
}