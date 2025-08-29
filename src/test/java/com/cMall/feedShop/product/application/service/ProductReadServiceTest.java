package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.request.ProductSearchRequest;
import com.cMall.feedShop.product.application.dto.response.ProductDetailResponse;
import com.cMall.feedShop.product.application.dto.response.ProductListResponse;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import com.cMall.feedShop.product.domain.enums.ProductSortType;
import com.cMall.feedShop.product.domain.exception.ProductException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("통합 ProductService 테스트")
class ProductReadServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductReadService productReadService;

    private Product testProduct1;
    private Product testProduct2;
    private Store testStore;
    private Category testCategory;
    private ProductListResponse testResponse1;
    private ProductListResponse testResponse2;
    private ProductDetailResponse detailResponse;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 테스트용 스토어 생성
        testStore = Store.builder()
                .storeName("테스트 스토어")
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        // 테스트용 카테고리 생성
        testCategory = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);

        // 테스트용 상품들 생성
        testProduct1 = Product.builder()
                .name("상품1")
                .price(new BigDecimal("50000"))
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(new BigDecimal("10"))
                .description("상품 1 설명")
                .store(testStore)
                .category(testCategory)
                .build();
        ReflectionTestUtils.setField(testProduct1, "productId", 1L);

        testProduct2 = Product.builder()
                .name("상품2")
                .price(new BigDecimal("80000"))
                .discountType(DiscountType.FIXED_DISCOUNT)
                .discountValue(new BigDecimal("5000"))
                .description("상품 2 설명")
                .store(testStore)
                .category(testCategory)
                .build();
        ReflectionTestUtils.setField(testProduct2, "productId", 2L);

        // 테스트용 응답 데이터 생성
        testResponse1 = ProductListResponse.builder()
                .productId(1L)
                .name("상품1")
                .price(new BigDecimal("50000"))
                .discountPrice(new BigDecimal("45000"))
                .categoryId(1L)
                .storeId(1L)
                .storeName("테스트 스토어")
                .build();

        testResponse2 = ProductListResponse.builder()
                .productId(2L)
                .name("상품2")
                .price(new BigDecimal("80000"))
                .discountPrice(new BigDecimal("75000"))
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

    // ===== 통합 상품 조회 테스트 =====

    @Test
    @DisplayName("전체 상품 목록 조회 성공")
    void getProducts_All_Success() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder().build();
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct1, testProduct2), PageRequest.of(0, 20), 2);

        given(productRepository.countWithAllConditions(any(ProductSearchRequest.class))).willReturn(2L);
        given(productRepository.findWithAllConditions(any(ProductSearchRequest.class), any(ProductSortType.class), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);
        given(productMapper.toListResponse(testProduct2)).willReturn(testResponse2);

        // when
        ProductPageResponse response = productReadService.getProductList(request, 0, 20, ProductSortType.LATEST);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent().get(0).getName()).isEqualTo("상품1");
        assertThat(response.getContent().get(1).getName()).isEqualTo("상품2");

        verify(productRepository, times(1)).countWithAllConditions(any(ProductSearchRequest.class));
        verify(productRepository, times(1)).findWithAllConditions(any(ProductSearchRequest.class), eq(ProductSortType.LATEST), any(Pageable.class));
    }

    @Test
    @DisplayName("키워드 검색 성공")
    void getProducts_WithKeyword_Success() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("상품1")
                .build();
        Page<Product> productPage = new PageImpl<>(List.of(testProduct1), PageRequest.of(0, 20), 1);

        given(productRepository.countWithAllConditions(request)).willReturn(1L);
        given(productRepository.findWithAllConditions(eq(request), any(ProductSortType.class), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);

        // when
        ProductPageResponse response = productReadService.getProductList(request, 0, 20, ProductSortType.LATEST);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("상품1");

        verify(productRepository).countWithAllConditions(request);
        verify(productRepository).findWithAllConditions(eq(request), eq(ProductSortType.LATEST), any(Pageable.class));
    }

    @Test
    @DisplayName("카테고리 필터링 성공")
    void getProducts_WithCategory_Success() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .categoryId(1L)
                .build();
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct1, testProduct2), PageRequest.of(0, 20), 2);

        given(productRepository.countWithAllConditions(request)).willReturn(2L);
        given(productRepository.findWithAllConditions(eq(request), any(ProductSortType.class), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);
        given(productMapper.toListResponse(testProduct2)).willReturn(testResponse2);

        // when
        ProductPageResponse response = productReadService.getProductList(request, 0, 20, ProductSortType.LATEST);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);

        verify(productRepository).countWithAllConditions(request);
        verify(productRepository).findWithAllConditions(eq(request), eq(ProductSortType.LATEST), any(Pageable.class));
    }

    @Test
    @DisplayName("가격 범위 필터링 성공")
    void getProducts_WithPriceRange_Success() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .minPrice(new BigDecimal("40000"))
                .maxPrice(new BigDecimal("60000"))
                .build();
        Page<Product> productPage = new PageImpl<>(List.of(testProduct1), PageRequest.of(0, 20), 1);

        given(productRepository.countWithAllConditions(request)).willReturn(1L);
        given(productRepository.findWithAllConditions(eq(request), any(ProductSortType.class), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);

        // when
        ProductPageResponse response = productReadService.getProductList(request, 0, 20, ProductSortType.LATEST);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("상품1");

        verify(productRepository).countWithAllConditions(request);
        verify(productRepository).findWithAllConditions(eq(request), eq(ProductSortType.LATEST), any(Pageable.class));
    }

    @Test
    @DisplayName("복합 조건 검색 성공 - 키워드 + 카테고리 + 가격")
    void getProducts_ComplexConditions_Success() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("상품")
                .categoryId(1L)
                .minPrice(new BigDecimal("30000"))
                .maxPrice(new BigDecimal("100000"))
                .storeId(1L)
                .build();
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct1, testProduct2), PageRequest.of(0, 20), 2);

        given(productRepository.countWithAllConditions(request)).willReturn(2L);
        given(productRepository.findWithAllConditions(eq(request), any(ProductSortType.class), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);
        given(productMapper.toListResponse(testProduct2)).willReturn(testResponse2);

        // when
        ProductPageResponse response = productReadService.getProductList(request, 0, 20, ProductSortType.POPULAR);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);

        verify(productRepository).countWithAllConditions(request);
        verify(productRepository).findWithAllConditions(eq(request), eq(ProductSortType.POPULAR), any(Pageable.class));
    }

    @Test
    @DisplayName("검색 결과 없음")
    void getProducts_NoResults() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("존재하지않는상품")
                .build();
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);

        given(productRepository.countWithAllConditions(request)).willReturn(0L);
        given(productRepository.findWithAllConditions(eq(request), any(ProductSortType.class), any(Pageable.class)))
                .willReturn(emptyPage);

        // when
        ProductPageResponse response = productReadService.getProductList(request, 0, 20, ProductSortType.LATEST);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);

        verify(productRepository).countWithAllConditions(request);
        verify(productRepository).findWithAllConditions(eq(request), eq(ProductSortType.LATEST), any(Pageable.class));
    }

    @Test
    @DisplayName("페이지 파라미터 검증 - 음수 페이지")
    void getProducts_NegativePage() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder().build();
        Page<Product> productPage = new PageImpl<>(List.of(testProduct1), PageRequest.of(0, 20), 1);

        given(productRepository.countWithAllConditions(any(ProductSearchRequest.class))).willReturn(1L);
        given(productRepository.findWithAllConditions(any(ProductSearchRequest.class), any(ProductSortType.class), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);

        // when
        ProductPageResponse response = productReadService.getProductList(request, -1, 20, ProductSortType.LATEST);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        // 음수 페이지가 0으로 보정되어 호출되었는지 확인
        verify(productRepository).findWithAllConditions(any(ProductSearchRequest.class), eq(ProductSortType.LATEST), eq(PageRequest.of(0, 20)));
    }

    @Test
    @DisplayName("페이지 크기 검증 - 범위 초과")
    void getProducts_InvalidSize() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder().build();
        Page<Product> productPage = new PageImpl<>(List.of(testProduct1), PageRequest.of(0, 20), 1);

        given(productRepository.countWithAllConditions(any(ProductSearchRequest.class))).willReturn(1L);
        given(productRepository.findWithAllConditions(any(ProductSearchRequest.class), any(ProductSortType.class), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);

        // when - 사이즈 150으로 호출 (최대 50 초과)
        ProductPageResponse response = productReadService.getProductList(request, 0, 150, ProductSortType.LATEST);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        // 사이즈가 20(기본값)으로 보정되어 호출되었는지 확인
        verify(productRepository).findWithAllConditions(any(ProductSearchRequest.class), eq(ProductSortType.LATEST), eq(PageRequest.of(0, 20)));
    }

    @Test
    @DisplayName("Page Overflow 처리")
    void getProducts_PageOverflow() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword("테스트")
                .build();
        Page<Product> productPage = new PageImpl<>(List.of(testProduct1), PageRequest.of(0, 20), 5); // 총 5개, 마지막 페이지는 0

        given(productRepository.countWithAllConditions(request)).willReturn(5L);
        given(productRepository.findWithAllConditions(eq(request), any(ProductSortType.class), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);

        // when - 10페이지 요청 (실제로는 0페이지까지만 존재)
        ProductPageResponse response = productReadService.getProductList(request, 10, 20, ProductSortType.LATEST);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        // Page Overflow로 인해 0페이지로 조정되어 호출되었는지 확인
        verify(productRepository).countWithAllConditions(request);
        verify(productRepository).findWithAllConditions(eq(request), eq(ProductSortType.LATEST), eq(PageRequest.of(0, 20)));
    }

    // ===== 상품 상세 조회 테스트 (기존 유지) =====

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductDetail_Success() {
        // given
        Long productId = 1L;
        given(productRepository.findByProductId(productId)).willReturn(Optional.of(testProduct1));
        given(productMapper.toDetailResponse(testProduct1)).willReturn(detailResponse);

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
        verify(productMapper, times(1)).toDetailResponse(testProduct1);
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

    // ===== 정렬 타입별 테스트 =====

    @Test
    @DisplayName("인기순 정렬")
    void getProducts_PopularSort() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder().build();
        Page<Product> productPage = new PageImpl<>(List.of(testProduct1), PageRequest.of(0, 20), 1);

        given(productRepository.countWithAllConditions(request)).willReturn(1L);
        given(productRepository.findWithAllConditions(eq(request), any(ProductSortType.class), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);

        // when
        ProductPageResponse response = productReadService.getProductList(request, 0, 20, ProductSortType.POPULAR);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        verify(productRepository).findWithAllConditions(eq(request), eq(ProductSortType.POPULAR), any(Pageable.class));
    }

    @Test
    @DisplayName("최신순 정렬")
    void getProducts_LatestSort() {
        // given
        ProductSearchRequest request = ProductSearchRequest.builder().build();
        Page<Product> productPage = new PageImpl<>(List.of(testProduct1), PageRequest.of(0, 20), 1);

        given(productRepository.countWithAllConditions(request)).willReturn(1L);
        given(productRepository.findWithAllConditions(eq(request), any(ProductSortType.class), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(testProduct1)).willReturn(testResponse1);

        // when
        ProductPageResponse response = productReadService.getProductList(request, 0, 20, ProductSortType.LATEST);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        verify(productRepository).findWithAllConditions(eq(request), eq(ProductSortType.LATEST), any(Pageable.class));
    }
}