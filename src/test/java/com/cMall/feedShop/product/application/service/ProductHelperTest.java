package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.domain.repository.OrderItemRepository;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.CategoryRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductHelper 테스트")
class ProductHelperTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductImageService productImageService;

    @InjectMocks
    private ProductHelper productHelper;

    private User sellerUser;
    private User normalUser;
    private Store testStore;
    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 판매자 사용자
        sellerUser = new User("seller", "password", "seller@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(sellerUser, "id", 1L);

        // 일반 사용자
        normalUser = new User("user", "password", "user@test.com", UserRole.USER);
        ReflectionTestUtils.setField(normalUser, "id", 2L);

        // 테스트 스토어
        testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        // 테스트 카테고리
        testCategory = new Category(null, "테스트 카테고리");
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);

        // 테스트 상품
        testProduct = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("10000"))
                .store(testStore)
                .category(testCategory)
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);
    }

    @Test
    @DisplayName("현재 사용자 조회 성공")
    void getCurrentUser_Success() {
        // given
        given(userRepository.findByLoginId("seller")).willReturn(Optional.of(sellerUser));

        // when
        User result = productHelper.getCurrentUser("seller");

        // then
        assertThat(result).isEqualTo(sellerUser);
        verify(userRepository).findByLoginId("seller");
    }

    @Test
    @DisplayName("현재 사용자 조회 실패 - 사용자 없음")
    void getCurrentUser_Fail_UserNotFound() {
        // given
        given(userRepository.findByLoginId("nonexistent")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productHelper.getCurrentUser("nonexistent"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("판매자 권한 검증 성공")
    void validateSellerRole_Success() {
        // when & then
        productHelper.validateSellerRole(sellerUser);
        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("판매자 권한 검증 실패 - 일반 사용자")
    void validateSellerRole_Fail_NormalUser() {
        // when & then
        assertThatThrownBy(() -> productHelper.validateSellerRole(normalUser))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("스토어 조회 성공")
    void getUserStore_Success() {
        // given
        given(storeRepository.findBySellerId(1L)).willReturn(Optional.of(testStore));

        // when
        Store result = productHelper.getUserStore(1L);

        // then
        assertThat(result).isEqualTo(testStore);
    }

    @Test
    @DisplayName("스토어 조회 실패 - 스토어 없음")
    void getUserStore_Fail_StoreNotFound() {
        // given
        given(storeRepository.findBySellerId(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productHelper.getUserStore(999L))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("카테고리 조회 성공")
    void getCategory_Success() {
        // given
        given(categoryRepository.findById(1L)).willReturn(Optional.of(testCategory));

        // when
        Category result = productHelper.getCategory(1L);

        // then
        assertThat(result).isEqualTo(testCategory);
    }

    @Test
    @DisplayName("카테고리 조회 실패 - 카테고리 없음")
    void getCategory_Fail_CategoryNotFound() {
        // given
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productHelper.getCategory(999L))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("상품 소유권 검증 성공")
    void getProductOwnership_Success() {
        // given
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(testProduct));

        // when
        Product result = productHelper.getProductOwnership(1L, 1L);

        // then
        assertThat(result).isEqualTo(testProduct);
    }

    @Test
    @DisplayName("상품명 중복 검증 성공 - 중복 없음")
    void validateProductNameDuplication_Success() {
        // given
        given(productRepository.existsByStoreAndName(testStore, "새로운 상품")).willReturn(false);

        // when & then
        productHelper.validateProductNameDuplication(testStore, "새로운 상품");
        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("상품명 중복 검증 실패 - 중복 존재")
    void validateProductNameDuplication_Fail_Duplicate() {
        // given
        given(productRepository.existsByStoreAndName(testStore, "중복 상품")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productHelper.validateProductNameDuplication(testStore, "중복 상품"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_PRODUCT_NAME);
    }

    @Test
    @DisplayName("주문 내역 검증 성공 - 주문 없음")
    void validateProductNotInOrder_Success() {
        // given
        ProductOption option = new ProductOption();
        ReflectionTestUtils.setField(testProduct, "productOptions", Arrays.asList(option));
        given(orderItemRepository.existsByProductOption(option)).willReturn(false);

        // when & then
        productHelper.validateProductNotInOrder(testProduct);
        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("주문 내역 검증 실패 - 주문 존재")
    void validateProductNotInOrder_Fail_OrderExists() {
        // given
        ProductOption option = new ProductOption();
        ReflectionTestUtils.setField(testProduct, "productOptions", Arrays.asList(option));
        given(orderItemRepository.existsByProductOption(option)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productHelper.validateProductNotInOrder(testProduct))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_IN_ORDER);
    }
}