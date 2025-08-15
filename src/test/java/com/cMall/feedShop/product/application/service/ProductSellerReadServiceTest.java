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
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductReadService 테스트")
class ProductSellerReadServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private ProductReadService productReadService;

    private Product product1;
    private Product product2;
    private Store store;
    private Category category;
    private User seller;
    private ProductListResponse listResponse1;
    private ProductListResponse listResponse2;
    private ProductDetailResponse detailResponse;

    @BeforeEach
    void setUp() {
        setupSeller();
        setupStore();
        setupCategory();
        setupProducts();
        setupResponses();
    }

    private void setupSeller() {
        // User 생성자를 올바르게 사용 (loginId, password, email, role 순서)
        seller = new User("seller123", "password", "seller123@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(seller, "id", 1L);
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
    @DisplayName("판매자 상품 목록 조회 성공")
    void getSellerProductList_Success() {
        // given
        String loginId = "seller123";
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product1, product2), PageRequest.of(0, 20), 2);

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(seller));
        given(storeRepository.findBySellerId(seller.getId())).willReturn(Optional.of(store));
        given(productRepository.countByStoreId(store.getStoreId())).willReturn(2L);
        given(productRepository.findByStoreIdOrderByCreatedAtDesc(eq(store.getStoreId()), any(Pageable.class)))
                .willReturn(productPage);
        given(productMapper.toListResponse(product1)).willReturn(listResponse1);
        given(productMapper.toListResponse(product2)).willReturn(listResponse2);

        // when
        ProductPageResponse response = productReadService.getSellerProductList(0, 20, loginId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent().get(0).getName()).isEqualTo("상품1");
        assertThat(response.getContent().get(1).getName()).isEqualTo("상품2");

        verify(userRepository, times(1)).findByLoginId(loginId);
        verify(storeRepository, times(1)).findBySellerId(seller.getId());
        verify(productRepository, times(1)).countByStoreId(store.getStoreId());
        verify(productRepository, times(1)).findByStoreIdOrderByCreatedAtDesc(eq(store.getStoreId()), any(Pageable.class));
    }

    @Test
    @DisplayName("판매자 상품 목록 조회 실패 - 존재하지 않는 사용자")
    void getSellerProductList_UserNotFound() {
        // given
        String loginId = "nonexistent";
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        // when & then
        assertThrows(ProductException.class,
                () -> productReadService.getSellerProductList(0, 20, loginId));

        verify(userRepository, times(1)).findByLoginId(loginId);
    }

    @Test
    @DisplayName("판매자 상품 목록 조회 실패 - 판매자 권한 없음")
    void getSellerProductList_NotSeller() {
        // given
        User normalUser = new User("user123", "password", "user123@test.com", UserRole.USER);
        ReflectionTestUtils.setField(normalUser, "id", 2L);
        String loginId = "user123";

        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(normalUser));

        // when & then
        assertThrows(ProductException.class,
                () -> productReadService.getSellerProductList(0, 20, loginId));

        verify(userRepository, times(1)).findByLoginId(loginId);
    }

    @Test
    @DisplayName("판매자 상품 목록 조회 실패 - 스토어 없음")
    void getSellerProductList_StoreNotFound() {
        // given
        String loginId = "seller123";
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(seller));
        given(storeRepository.findBySellerId(seller.getId())).willReturn(Optional.empty());

        // when & then
        assertThrows(ProductException.class,
                () -> productReadService.getSellerProductList(0, 20, loginId));

        verify(userRepository, times(1)).findByLoginId(loginId);
        verify(storeRepository, times(1)).findBySellerId(seller.getId());
    }
}