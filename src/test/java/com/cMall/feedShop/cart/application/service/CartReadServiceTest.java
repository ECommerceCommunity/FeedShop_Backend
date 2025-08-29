package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.response.CartItemListResponse;
import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
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
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartReadService 테스트")
class CartReadServiceTest {

    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductOptionRepository productOptionRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private DiscountCalculator discountCalculator;
    @Mock private CartHelper cartHelper;

    @InjectMocks
    private CartReadService cartReadService;

    // 테스트 데이터
    private User user;
    private Cart cart;
    private Store store;
    private Category category;
    private Product product1, product2;
    private ProductOption productOption1, productOption2;
    private ProductImage productImage1, productImage2;

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
        product1 = Product.builder()
                .name("상품1")
                .price(new BigDecimal("50000"))
                .store(store)
                .category(category)
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(new BigDecimal("10"))
                .build();
        ReflectionTestUtils.setField(product1, "productId", 1L);

        product2 = Product.builder()
                .name("상품2")
                .price(new BigDecimal("30000"))
                .store(store)
                .category(category)
                .discountType(DiscountType.NONE)
                .build();
        ReflectionTestUtils.setField(product2, "productId", 2L);

        // ProductOption 설정
        productOption1 = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 100, product1);
        ReflectionTestUtils.setField(productOption1, "optionId", 1L);

        productOption2 = new ProductOption(Gender.UNISEX, Size.SIZE_260, Color.BLACK, 50, product2);
        ReflectionTestUtils.setField(productOption2, "optionId", 2L);

        // ProductImage 설정
        productImage1 = new ProductImage("http://image1.jpg", ImageType.MAIN, product1);
        ReflectionTestUtils.setField(productImage1, "imageId", 1L);

        productImage2 = new ProductImage("http://image2.jpg", ImageType.MAIN, product2);
        ReflectionTestUtils.setField(productImage2, "imageId", 2L);

        // Cart 설정
        cart = Cart.builder().user(user).build();
        ReflectionTestUtils.setField(cart, "cartId", 1L);
        user.setCart(cart);
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공")
    void getCartItems_Success() {
        // given
        CartItem cartItem1 = createCartItem(1L, 1L, 1L, 2, true);
        CartItem cartItem2 = createCartItem(2L, 2L, 2L, 1, true);

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(Arrays.asList(cartItem1, cartItem2));
        given(productOptionRepository.findAllByOptionIdIn(any())).willReturn(Arrays.asList(productOption1, productOption2));
        given(productImageRepository.findAllById(any())).willReturn(Arrays.asList(productImage1, productImage2));

        given(discountCalculator.calculateDiscountPrice(new BigDecimal("50000"), DiscountType.RATE_DISCOUNT, new BigDecimal("10")))
                .willReturn(new BigDecimal("45000"));
        given(discountCalculator.calculateDiscountPrice(new BigDecimal("30000"), DiscountType.NONE, null))
                .willReturn(new BigDecimal("30000"));

        // when
        CartItemListResponse response = cartReadService.getCartItems("test@test.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalItemCount()).isEqualTo(2);
        assertThat(response.getTotalOriginalPrice()).isEqualTo(new BigDecimal("130000")); // 50000*2 + 30000*1
        assertThat(response.getTotalDiscountPrice()).isEqualTo(new BigDecimal("120000")); // 45000*2 + 30000*1
        assertThat(response.getTotalSavings()).isEqualTo(new BigDecimal("10000"));

        verify(cartItemRepository, times(1)).findByUserIdWithCart(1L);
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공 - 빈 장바구니")
    void getCartItems_Success_EmptyCart() {
        // given
        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(List.of());

        // when
        CartItemListResponse response = cartReadService.getCartItems("test@test.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalItemCount()).isEqualTo(0);
        assertThat(response.getTotalOriginalPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getTotalDiscountPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getTotalSavings()).isEqualTo(BigDecimal.ZERO);

        verify(cartItemRepository, times(1)).findByUserIdWithCart(1L);
        verify(productOptionRepository, never()).findAllByOptionIdIn(any());
        verify(productImageRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공 - 선택되지 않은 아이템 포함")
    void getCartItems_Success_WithUnselectedItems() {
        // given
        CartItem selectedItem = createCartItem(1L, 1L, 1L, 2, true);
        CartItem unselectedItem = createCartItem(2L, 2L, 2L, 1, false);

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(Arrays.asList(selectedItem, unselectedItem));
        given(productOptionRepository.findAllByOptionIdIn(any())).willReturn(Arrays.asList(productOption1, productOption2));
        given(productImageRepository.findAllById(any())).willReturn(Arrays.asList(productImage1, productImage2));

        given(discountCalculator.calculateDiscountPrice(new BigDecimal("50000"), DiscountType.RATE_DISCOUNT, new BigDecimal("10")))
                .willReturn(new BigDecimal("45000"));
        given(discountCalculator.calculateDiscountPrice(new BigDecimal("30000"), DiscountType.NONE, null))
                .willReturn(new BigDecimal("30000"));

        // when
        CartItemListResponse response = cartReadService.getCartItems("test@test.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(2);
        // 선택된 아이템만 가격 계산에 포함
        assertThat(response.getTotalOriginalPrice()).isEqualTo(new BigDecimal("100000")); // 50000*2 (선택된 것만)
        assertThat(response.getTotalDiscountPrice()).isEqualTo(new BigDecimal("90000")); // 45000*2 (선택된 것만)
        assertThat(response.getTotalSavings()).isEqualTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("장바구니 목록 조회 실패 - 상품 옵션 없음")
    void getCartItems_Fail_ProductOptionNotFound() {
        // given
        CartItem cartItem1 = createCartItem(1L, 1L, 1L, 2, true);

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(List.of(cartItem1));
        given(productOptionRepository.findAllByOptionIdIn(any())).willReturn(List.of()); // 빈 리스트
        given(productImageRepository.findAllById(any())).willReturn(List.of(productImage1));

        // when & then
        ProductException thrown = assertThrows(ProductException.class, () ->
                cartReadService.getCartItems("test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("장바구니 목록 조회 실패 - 상품 이미지 없음")
    void getCartItems_Fail_ProductImageNotFound() {
        // given
        CartItem cartItem1 = createCartItem(1L, 1L, 1L, 2, true);

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(List.of(cartItem1));
        given(productOptionRepository.findAllByOptionIdIn(any())).willReturn(List.of(productOption1));
        given(productImageRepository.findAllById(any())).willReturn(List.of()); // 빈 리스트

        // when & then
        ProductException thrown = assertThrows(ProductException.class, () ->
                cartReadService.getCartItems("test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
    }

    // ==================== 헬퍼 메서드 ====================

    private CartItem createCartItem(Long cartItemId, Long optionId, Long imageId, Integer quantity, Boolean selected) {
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .optionId(optionId)
                .imageId(imageId)
                .quantity(quantity)
                .build();
        ReflectionTestUtils.setField(cartItem, "cartItemId", cartItemId);
        ReflectionTestUtils.setField(cartItem, "selected", selected);
        ReflectionTestUtils.setField(cartItem, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(cartItem, "updatedAt", LocalDateTime.now());
        return cartItem;
    }
}