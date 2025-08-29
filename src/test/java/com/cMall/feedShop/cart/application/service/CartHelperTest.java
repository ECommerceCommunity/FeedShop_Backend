package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.repository.CartRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductImageRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.exception.UserException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartMapper 테스트")
class CartHelperTest {

    @Mock private UserRepository userRepository;
    @Mock private ProductOptionRepository productOptionRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private CartRepository cartRepository;

    @InjectMocks
    private CartHelper cartHelper;

    // 테스트 데이터
    private User user;
    private Store store;
    private Category category;
    private Product product;
    private ProductOption productOption;
    private ProductImage productImage;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // User 설정
        user = new User("test@test.com", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        // Store 설정
        store = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);

        // Category 설정
        category = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(category, "categoryId", 1L);

        // Product 설정
        product = Product.builder()
                .name("상품1")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(new BigDecimal("10"))
                .build();
        ReflectionTestUtils.setField(product, "productId", 1L);

        // ProductOption 설정
        productOption = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 100, product);
        ReflectionTestUtils.setField(productOption, "optionId", 1L);

        // ProductImage 설정
        productImage = new ProductImage("http://image1.jpg", ImageType.MAIN, product);
        ReflectionTestUtils.setField(productImage, "imageId", 1L);
    }

    @Test
    @DisplayName("현재 사용자 조회 성공")
    void getCurrentUser_Success() {
        // given
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));

        // when
        User result = cartHelper.getCurrentUser("test@test.com");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLoginId()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("현재 사용자 조회 실패 - 사용자 없음")
    void getCurrentUser_Fail_UserNotFound() {
        // given
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.empty());

        // when & then
        UserException thrown = assertThrows(UserException.class, () ->
                cartHelper.getCurrentUser("test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("상품 옵션 검증 성공")
    void validateProductOption_Success() {
        // given
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(productOption));

        // when
        ProductOption result = cartHelper.validateProductOption(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOptionId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("상품 옵션 검증 실패 - 옵션 없음")
    void validateProductOption_Fail_OptionNotFound() {
        // given
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.empty());

        // when & then
        ProductException thrown = assertThrows(ProductException.class, () ->
                cartHelper.validateProductOption(1L));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("상품 이미지 검증 성공")
    void validateProductImage_Success() {
        // given
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.of(productImage));

        // when
        ProductImage result = cartHelper.validateProductImage(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getImageId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("상품 이미지 검증 실패 - 이미지 없음")
    void validateProductImage_Fail_ImageNotFound() {
        // given
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.empty());

        // when & then
        ProductException thrown = assertThrows(ProductException.class, () ->
                cartHelper.validateProductImage(1L));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
    }

    @Test
    @DisplayName("재고 검증 성공")
    void validateStock_Success() {
        // given & when & then
        // 재고가 충분한 경우 예외가 발생하지 않아야 함
        cartHelper.validateStock(productOption, 50);
        cartHelper.validateStock(productOption, 100);
    }

    @Test
    @DisplayName("재고 검증 실패 - 재고 부족")
    void validateStock_Fail_InsufficientStock() {
        // given & when & then
        ProductException thrown = assertThrows(ProductException.class, () ->
                cartHelper.validateStock(productOption, 150));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("재고 검증 실패 - 재고 없음")
    void validateStock_Fail_NoStock() {
        // given
        ProductOption noStockOption = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 0, product);
        ReflectionTestUtils.setField(noStockOption, "optionId", 1L);

        // when & then
        ProductException thrown = assertThrows(ProductException.class, () ->
                cartHelper.validateStock(noStockOption, 1));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("장바구니 조회 성공 - 기존 장바구니")
    void getOrCreateCart_Success_ExistingCart() {
        // given
        Cart existingCart = Cart.builder().user(user).build();
        ReflectionTestUtils.setField(existingCart, "cartId", 1L);
        user.setCart(existingCart);

        // when
        Cart result = cartHelper.getOrCreateCart(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(1L);
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("장바구니 생성 성공 - 새 장바구니")
    void getOrCreateCart_Success_NewCart() {
        // given
        User userWithoutCart = new User("test@test.com", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(userWithoutCart, "id", 1L);

        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> {
            Cart savedCart = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedCart, "cartId", 2L);
            return savedCart;
        });

        // when
        Cart result = cartHelper.getOrCreateCart(userWithoutCart);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(2L);
        assertThat(userWithoutCart.getCart()).isEqualTo(result);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }
}