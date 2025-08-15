package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.response.ProductListResponse;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.product.domain.enums.DiscountType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchService 테스트")
class ProductSearchServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductSearchService productSearchService;

    private Product testProduct1;
    private Product testProduct2;
    private Store testStore;
    private Category testCategory;
    private ProductListResponse testResponse1;
    private ProductListResponse testResponse2;

    @BeforeEach
    void setUp() {
        setupStore();
        setupCategory();
        setupProducts();
        setupResponses();
    }

    private void setupStore() {
        testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);
    }

    private void setupCategory() {
        testCategory = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);
    }

    private void setupProducts() {
        testProduct1 = Product.builder()
                .name("나이키 에어맥스")
                .price(new BigDecimal("150000"))
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(new BigDecimal("10"))
                .description("나이키 운동화")
                .store(testStore)
                .category(testCategory)
                .build();
        ReflectionTestUtils.setField(testProduct1, "productId", 1L);

        testProduct2 = Product.builder()
                .name("아디다스 스탠스미스")
                .price(new BigDecimal("80000"))
                .discountType(DiscountType.NONE)
                .description("아디다스 운동화")
                .store(testStore)
                .category(testCategory)
                .build();
        ReflectionTestUtils.setField(testProduct2, "productId", 2L);
    }

    private void setupResponses() {
        testResponse1 = ProductListResponse.builder()
                .productId(1L)
                .name("나이키 에어맥스")
                .price(new BigDecimal("150000"))
                .discountPrice(new BigDecimal("135000"))
                .mainImageUrl("http://image1.jpg")
                .categoryId(1L)
                .storeId(1L)
                .storeName("테스트 스토어")
                .build();

        testResponse2 = ProductListResponse.builder()
                .productId(2L)
                .name("아디다스 스탠스미스")
                .price(new BigDecimal("80000"))
                .discountPrice(new BigDecimal("80000"))
                .mainImageUrl("http://image2.jpg")
                .categoryId(1L)
                .storeId(1L)
                .storeName("테스트 스토어")
                .build();
    }

    @Test
    @DisplayName("키워드로 상품 검색 성공")
    void searchProductList_WithKeyword_Success() {
        // given
        String keyword = "나이키";
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct1), PageRequest.of(0, 10), 1);

        given(productRepository.searchProductsByName(anyString(), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1))
                .willReturn(testResponse1);

        // when
        ProductPageResponse response = productSearchService.searchProductList(keyword, 0, 10);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("나이키 에어맥스");

        verify(productRepository, times(1)).searchProductsByName(keyword, PageRequest.of(0, 10));
        verify(productMapper, times(1)).toListResponse(testProduct1);
    }

    @Test
    @DisplayName("빈 키워드로 전체 상품 목록 조회")
    void searchProductList_WithEmptyKeyword_Success() {
        // given
        String keyword = "";
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct1, testProduct2), PageRequest.of(0, 10), 2);

        given(productRepository.searchProductsByName(anyString(), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);
        given(productMapper.toListResponse(testProduct2)).willReturn(testResponse2);

        // when
        ProductPageResponse response = productSearchService.searchProductList(keyword, 0, 10);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);

        verify(productRepository, times(1)).searchProductsByName(keyword, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("null 키워드로 전체 상품 목록 조회")
    void searchProductList_WithNullKeyword_Success() {
        // given
        String keyword = null;
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct1, testProduct2), PageRequest.of(0, 10), 2);

        given(productRepository.searchProductsByName(any(), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);
        given(productMapper.toListResponse(testProduct2)).willReturn(testResponse2);

        // when
        ProductPageResponse response = productSearchService.searchProductList(keyword, 0, 10);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);

        verify(productRepository, times(1)).searchProductsByName(keyword, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("검색 결과가 없는 경우")
    void searchProductList_NoResults() {
        // given
        String keyword = "존재하지않는상품";
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        given(productRepository.searchProductsByName(anyString(), any(Pageable.class)))
                .willReturn(emptyPage);

        // when
        ProductPageResponse response = productSearchService.searchProductList(keyword, 0, 10);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);

        verify(productRepository, times(1)).searchProductsByName(keyword, PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("다양한 페이지 크기로 검색")
    void searchProductList_DifferentPageSizes() {
        // given
        String keyword = "운동화";
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct1), PageRequest.of(0, 5), 1);

        given(productRepository.searchProductsByName(anyString(), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);

        // when
        ProductPageResponse response = productSearchService.searchProductList(keyword, 0, 5);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        verify(productRepository, times(1)).searchProductsByName(keyword, PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("두 번째 페이지 검색")
    void searchProductList_SecondPage() {
        // given
        String keyword = "운동화";
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct2), PageRequest.of(1, 10), 2);

        given(productRepository.searchProductsByName(anyString(), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct2)).willReturn(testResponse2);

        // when
        ProductPageResponse response = productSearchService.searchProductList(keyword, 1, 10);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("아디다스 스탠스미스");

        verify(productRepository, times(1)).searchProductsByName(keyword, PageRequest.of(1, 10));
    }

    @Test
    @DisplayName("특정 브랜드 키워드로 검색")
    void searchProductList_ByBrand() {
        // given
        String keyword = "아디다스";
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct2), PageRequest.of(0, 20), 1);

        given(productRepository.searchProductsByName(anyString(), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct2)).willReturn(testResponse2);

        // when
        ProductPageResponse response = productSearchService.searchProductList(keyword, 0, 20);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).contains("아디다스");

        verify(productRepository, times(1)).searchProductsByName(keyword, PageRequest.of(0, 20));
    }
}