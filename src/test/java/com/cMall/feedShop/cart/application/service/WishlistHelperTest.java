package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistMapper 테스트")
class WishlistHelperTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistHelper wishlistHelper;

    private User testUser;
    private Product testProduct;
    private Store testStore;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testUser = new User("testLogin", "password123", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        testCategory = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);

        testProduct = Product.builder()
                .name("나이키 에어맥스")
                .price(new BigDecimal("150000"))
                .store(testStore)
                .category(testCategory)
                .discountType(DiscountType.NONE)
                .description("편안한 운동화")
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);
    }

    @Test
    @DisplayName("사용자 조회 성공")
    void getCurrentUser_Success() {
        // given
        String loginId = "testLogin";
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(testUser));

        // when
        User result = wishlistHelper.getCurrentUser(loginId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLoginId()).isEqualTo("testLogin");
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);

        verify(userRepository, times(1)).findByLoginId(loginId);
    }

    @Test
    @DisplayName("사용자 조회 실패 - 사용자 없음")
    void getCurrentUser_Fail_UserNotFound() {
        // given
        String loginId = "nonExistentUser";
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistHelper.getCurrentUser(loginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(userRepository, times(1)).findByLoginId(loginId);
    }

    @Test
    @DisplayName("사용자 조회 실패 - null loginId")
    void getCurrentUser_Fail_NullLoginId() {
        // given
        String loginId = null;
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistHelper.getCurrentUser(loginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(userRepository, times(1)).findByLoginId(loginId);
    }

    @Test
    @DisplayName("사용자 조회 실패 - 빈 문자열 loginId")
    void getCurrentUser_Fail_EmptyLoginId() {
        // given
        String loginId = "";
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistHelper.getCurrentUser(loginId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(userRepository, times(1)).findByLoginId(loginId);
    }

    @Test
    @DisplayName("상품 조회 성공")
    void getProduct_Success() {
        // given
        Long productId = 1L;
        given(productRepository.findByProductId(productId)).willReturn(Optional.of(testProduct));

        // when
        Product result = wishlistHelper.getProduct(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("나이키 에어맥스");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("150000"));
        assertThat(result.getStore()).isEqualTo(testStore);
        assertThat(result.getCategory()).isEqualTo(testCategory);
        assertThat(result.getDiscountType()).isEqualTo(DiscountType.NONE);

        verify(productRepository, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("상품 조회 실패 - 상품 없음")
    void getProduct_Fail_ProductNotFound() {
        // given
        Long productId = 999L;
        given(productRepository.findByProductId(productId)).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistHelper.getProduct(productId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        verify(productRepository, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("상품 조회 실패 - null productId")
    void getProduct_Fail_NullProductId() {
        // given
        Long productId = null;
        given(productRepository.findByProductId(productId)).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistHelper.getProduct(productId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        verify(productRepository, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("상품 조회 실패 - 음수 productId")
    void getProduct_Fail_NegativeProductId() {
        // given
        Long productId = -1L;
        given(productRepository.findByProductId(productId)).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistHelper.getProduct(productId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        verify(productRepository, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("상품 조회 실패 - 0인 productId")
    void getProduct_Fail_ZeroProductId() {
        // given
        Long productId = 0L;
        given(productRepository.findByProductId(productId)).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                wishlistHelper.getProduct(productId));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        verify(productRepository, times(1)).findByProductId(productId);
    }

    @Test
    @DisplayName("동시에 사용자와 상품 조회 성공")
    void getCurrentUserAndProduct_Success() {
        // given
        String loginId = "testLogin";
        Long productId = 1L;
        given(userRepository.findByLoginId(loginId)).willReturn(Optional.of(testUser));
        given(productRepository.findByProductId(productId)).willReturn(Optional.of(testProduct));

        // when
        User user = wishlistHelper.getCurrentUser(loginId);
        Product product = wishlistHelper.getProduct(productId);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(product).isNotNull();
        assertThat(product.getProductId()).isEqualTo(1L);

        verify(userRepository, times(1)).findByLoginId(loginId);
        verify(productRepository, times(1)).findByProductId(productId);
    }
}