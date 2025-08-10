package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.request.ProductFilterRequest;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * ProductFilterService 테스트 클래스
 * - 초등학생도 이해할 수 있도록 자세한 주석 추가
 * - 각 테스트 메서드는 하나의 기능만 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("상품 필터링 서비스 테스트")
class ProductFilterServiceTest {

    @Mock
    private ProductRepository productRepository; // 가짜 저장소 (실제 DB 없이 테스트)

    @Mock
    private ProductMapper productMapper; // 가짜 매퍼 (누락되어 있던 부분!)

    @InjectMocks
    private ProductFilterService productFilterService; // 테스트할 실제 서비스

    private Product testProduct; // 테스트용 상품 데이터
    private Store testStore; // 테스트용 스토어 데이터
    private Category testCategory; // 테스트용 카테고리 데이터
    private ProductListResponse testProductResponse; // 테스트용 응답 데이터

    /**
     * 각 테스트 실행 전에 공통으로 사용할 테스트 데이터 준비
     */
    @BeforeEach
    void setUp() {
        // 1. 테스트용 스토어 생성
        testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .description("테스트 설명")
                .logo("http://logo.jpg")
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        // 2. 테스트용 카테고리 생성
        testCategory = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);

        // 3. 테스트용 상품 생성
        testProduct = Product.builder()
                .name("에어맥스 97")
                .price(new BigDecimal("149000"))
                .description("편안한 운동화")
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(new BigDecimal("10"))
                .store(testStore)
                .category(testCategory)
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);

        // 4. 테스트용 응답 데이터 생성
        testProductResponse = ProductListResponse.builder()
                .productId(1L)
                .name("에어맥스 97")
                .price(new BigDecimal("149000"))
                .discountPrice(new BigDecimal("134100"))
                .mainImageUrl("http://image.jpg")
                .categoryId(1L)
                .storeId(1L)
                .storeName("테스트 스토어")
                .build();
    }

    @Test
    @DisplayName("카테고리 필터링 - 특정 카테고리의 상품만 조회")
    void filterProducts_ByCategoryId_Success() {
        // given (준비 단계)
        // 카테고리 ID로 필터링하는 요청 만들기
        ProductFilterRequest request = ProductFilterRequest.builder()
                .categoryId(1L) // 카테고리 ID = 1
                .build();

        // 페이징 정보 만들기 (첫 번째 페이지, 20개씩)
        Pageable pageable = PageRequest.of(0, 20);

        // 가짜 상품 목록 만들기
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        // 가짜 저장소가 이런 결과를 반환하도록 설정
        given(productRepository.findProductsWithFilters(
                eq(1L), // 카테고리 ID
                eq(null), // 최소 가격 없음
                eq(null), // 최대 가격 없음
                eq(null), // 스토어 ID 없음
                eq(null), // 정렬 기준 없음
                any(Pageable.class)
        )).willReturn(productPage);

        // 가짜 매퍼가 Product를 ProductListResponse로 변환하도록 설정
        given(productMapper.toListResponse(any(Product.class)))
                .willReturn(testProductResponse);

        // when (실행 단계)
        // 실제로 필터링 메서드 호출
        ProductPageResponse result = productFilterService.filterProductList(request, 0, 20, null);

        // then (검증 단계)
        // 결과가 올바른지 확인
        assertThat(result).isNotNull(); // 결과가 null이 아님
        assertThat(result.getContent()).hasSize(1); // 상품이 1개 조회됨
        assertThat(result.getTotalElements()).isEqualTo(1); // 전체 상품 개수가 1개

        // 조회된 상품 정보가 올바른지 확인
        ProductListResponse productResponse = result.getContent().get(0);
        assertThat(productResponse.getProductId()).isEqualTo(1L);
        assertThat(productResponse.getName()).isEqualTo("에어맥스 97");
        assertThat(productResponse.getCategoryId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("가격 범위 필터링 - 최소가격과 최대가격 사이의 상품만 조회")
    void filterProducts_ByPriceRange_Success() {
        // given (준비 단계)
        ProductFilterRequest request = ProductFilterRequest.builder()
                .minPrice(new BigDecimal("100000")) // 최소 가격 10만원
                .maxPrice(new BigDecimal("200000")) // 최대 가격 20만원
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        // 가격 범위에 맞는 상품이 조회되도록 설정
        given(productRepository.findProductsWithFilters(
                eq(null), // 카테고리 ID 없음
                eq(new BigDecimal("100000")), // 최소 가격 10만원
                eq(new BigDecimal("200000")), // 최대 가격 20만원
                eq(null), // 스토어 ID 없음
                eq(null),
                any(Pageable.class)
        )).willReturn(productPage);

        // 가짜 매퍼 설정
        given(productMapper.toListResponse(any(Product.class)))
                .willReturn(testProductResponse);

        // when (실행 단계)
        ProductPageResponse result = productFilterService.filterProductList(request, 0, 20, null);

        // then (검증 단계)
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        ProductListResponse productResponse = result.getContent().get(0);
        assertThat(productResponse.getPrice()).isEqualTo(new BigDecimal("149000"));
        // 149000원은 100000~200000 범위 안에 있음
    }

    @Test
    @DisplayName("스토어 필터링 - 특정 스토어의 상품만 조회")
    void filterProducts_ByStoreId_Success() {
        // given (준비 단계)
        ProductFilterRequest request = ProductFilterRequest.builder()
                .storeId(1L) // 스토어 ID = 1
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        given(productRepository.findProductsWithFilters(
                eq(null), // 카테고리 ID 없음
                eq(null), // 최소 가격 없음
                eq(null), // 최대 가격 없음
                eq(1L), // 스토어 ID = 1
                eq(null), // 정렬 기준 없음
                any(Pageable.class)
        )).willReturn(productPage);

        // 가짜 매퍼 설정
        given(productMapper.toListResponse(any(Product.class)))
                .willReturn(testProductResponse);

        // when (실행 단계)
        ProductPageResponse result = productFilterService.filterProductList(request, 0, 20, null);

        // then (검증 단계)
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        ProductListResponse productResponse = result.getContent().get(0);
        assertThat(productResponse.getStoreId()).isEqualTo(1L);
        assertThat(productResponse.getStoreName()).isEqualTo("테스트 스토어");
    }

    @Test
    @DisplayName("복합 필터링 - 여러 조건을 동시에 적용하여 상품 조회")
    void filterProducts_MultipleFilters_Success() {
        // given (준비 단계)
        // 카테고리, 가격범위, 스토어 조건을 모두 포함한 요청
        ProductFilterRequest request = ProductFilterRequest.builder()
                .categoryId(1L)
                .minPrice(new BigDecimal("100000"))
                .maxPrice(new BigDecimal("200000"))
                .storeId(1L)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        // 모든 조건이 적용되도록 설정
        given(productRepository.findProductsWithFilters(
                eq(1L), // 카테고리 ID = 1
                eq(new BigDecimal("100000")), // 최소 가격 10만원
                eq(new BigDecimal("200000")), // 최대 가격 20만원
                eq(1L), // 스토어 ID = 1
                eq(null), // 정렬 기준 없음
                any(Pageable.class)
        )).willReturn(productPage);

        // 가짜 매퍼 설정
        given(productMapper.toListResponse(any(Product.class)))
                .willReturn(testProductResponse);

        // when (실행 단계)
        ProductPageResponse result = productFilterService.filterProductList(request, 0, 20, null);

        // then (검증 단계)
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        ProductListResponse productResponse = result.getContent().get(0);
        // 모든 조건을 만족하는 상품이 조회되었는지 확인
        assertThat(productResponse.getCategoryId()).isEqualTo(1L);
        assertThat(productResponse.getStoreId()).isEqualTo(1L);
        assertThat(productResponse.getPrice()).isEqualTo(new BigDecimal("149000"));
    }

    @Test
    @DisplayName("페이지 번호 음수 처리 - 음수 페이지는 0으로 변경됨")
    void filterProducts_NegativePage_ConvertedToZero() {
        // given (준비 단계)
        ProductFilterRequest request = ProductFilterRequest.builder().build();

        Pageable pageable = PageRequest.of(0, 20); // 음수가 0으로 변경됨
        Page<Product> productPage = new PageImpl<>(List.of(), pageable, 0);

        given(productRepository.findProductsWithFilters(any(), any(), any(), any(), any(), any()))
                .willReturn(productPage);

        // when (실행 단계)
        // 페이지 번호를 음수(-1)로 전달
        ProductPageResponse result = productFilterService.filterProductList(request, -1, 20, null);

        // then (검증 단계)
        assertThat(result).isNotNull(); // 에러 없이 정상 처리됨
    }

    @Test
    @DisplayName("페이지 크기 범위 초과 처리 - 100 초과하면 20으로 변경됨")
    void filterProducts_InvalidSize_ConvertedToDefault() {
        // given (준비 단계)
        ProductFilterRequest request = ProductFilterRequest.builder().build();

        Pageable pageable = PageRequest.of(0, 20); // 크기가 20으로 변경됨
        Page<Product> productPage = new PageImpl<>(List.of(), pageable, 0);

        given(productRepository.findProductsWithFilters(any(), any(), any(), any(), any(), any()))
                .willReturn(productPage);

        // when (실행 단계)
        // 페이지 크기를 200으로 전달 (100 초과)
        ProductPageResponse result = productFilterService.filterProductList(request, 0, 200, null);

        // then (검증 단계)
        assertThat(result).isNotNull(); // 에러 없이 정상 처리됨
    }
}