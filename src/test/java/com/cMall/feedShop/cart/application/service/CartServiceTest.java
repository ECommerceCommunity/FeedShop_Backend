package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.CartItemCreateRequest;
import com.cMall.feedShop.cart.application.dto.response.CartItemListResponse;
import com.cMall.feedShop.cart.application.dto.response.CartItemResponse;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.cart.domain.repository.CartRepository;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductImageRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService 통합 테스트")
class CartServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ProductOptionRepository productOptionRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private DiscountCalculator discountCalculator;

    @InjectMocks
    private CartService cartService;

    // 테스트 데이터
    private User user;
    private Cart cart;
    private Store store;
    private Category category;
    private Product product1, product2;
    private ProductOption productOption1, productOption2;
    private ProductImage productImage1, productImage2;
    private CartItemCreateRequest createRequest;

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

        // Request 설정
        createRequest = new CartItemCreateRequest();
        ReflectionTestUtils.setField(createRequest, "optionId", 1L);
        ReflectionTestUtils.setField(createRequest, "imageId", 1L);
        ReflectionTestUtils.setField(createRequest, "quantity", 2);
    }

    // ==================== addCartItem 테스트 ====================

    @Test
    @DisplayName("장바구니에 새 상품 추가 성공")
    void addCartItem_Success_NewItem() {
        // given
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(productOption1));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.of(productImage1));
        given(cartItemRepository.findByCartAndOptionIdAndImageId(cart, 1L, 1L)).willReturn(Optional.empty());

        given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> {
            CartItem cartItem = invocation.getArgument(0);
            ReflectionTestUtils.setField(cartItem, "cartItemId", 1L);
            ReflectionTestUtils.setField(cartItem, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(cartItem, "updatedAt", LocalDateTime.now());
            return cartItem;
        });

        // when
        CartItemResponse response = cartService.addCartItem(createRequest, "test@test.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCartItemId()).isEqualTo(1L);
        assertThat(response.getCartId()).isEqualTo(1L);
        assertThat(response.getOptionId()).isEqualTo(1L);
        assertThat(response.getImageId()).isEqualTo(1L);
        assertThat(response.getQuantity()).isEqualTo(2);

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 추가 실패 - 상품 옵션 없음")
    void addCartItem_Fail_ProductOptionNotFound() {
        // given
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.empty());

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        cartService.addCartItem(createRequest, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 추가 실패 - 상품 이미지 없음")
    void addCartItem_Fail_ProductImageNotFound() {
        // given
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(productOption1));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.empty());

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        cartService.addCartItem(createRequest, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 추가 실패 - 재고 부족")
    void addCartItem_Fail_OutOfStock() {
        // given - 재고가 1개뿐인 옵션 생성
        ProductOption lowStockOption = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 1, product1);
        ReflectionTestUtils.setField(lowStockOption, "optionId", 1L);

        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(lowStockOption));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.of(productImage1));

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        cartService.addCartItem(createRequest, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 추가 실패 - 재고 없음")
    void addCartItem_Fail_NoStock() {
        // given
        ProductOption noStockOption = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 0, product1);
        ReflectionTestUtils.setField(noStockOption, "optionId", 1L);

        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(noStockOption));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.of(productImage1));

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        cartService.addCartItem(createRequest, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니가 없는 사용자 - 새 장바구니 생성")
    void addCartItem_Success_CreateNewCart() {
        // given
        User userWithoutCart = new User("testUser", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(userWithoutCart, "id", 1L);

        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(userWithoutCart));
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(productOption1));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.of(productImage1));

        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> {
            Cart savedCart = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedCart, "cartId", 2L);
            userWithoutCart.setCart(savedCart);
            return savedCart;
        });

        given(cartItemRepository.findByCartAndOptionIdAndImageId(any(Cart.class), eq(1L), eq(1L)))
                .willReturn(Optional.empty());

        given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> {
            CartItem cartItem = invocation.getArgument(0);
            ReflectionTestUtils.setField(cartItem, "cartItemId", 1L);
            ReflectionTestUtils.setField(cartItem, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(cartItem, "updatedAt", LocalDateTime.now());
            return cartItem;
        });

        // when
        CartItemResponse response = cartService.addCartItem(createRequest, "test@test.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(2L);
        assertThat(response.getQuantity()).isEqualTo(2);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("기존 아이템 수량 증가 시 재고 확인 - 실패")
    void addCartItem_Fail_ExistingItem_InsufficientStock() {
        // given
        CartItem existingCartItem = CartItem.builder()
                .cart(cart)
                .optionId(1L)
                .imageId(1L)
                .quantity(99)
                .build();
        ReflectionTestUtils.setField(existingCartItem, "cartItemId", 1L);

        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(productOption1));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.of(productImage1));
        given(cartItemRepository.findByCartAndOptionIdAndImageId(cart, 1L, 1L))
                .willReturn(Optional.of(existingCartItem));

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        cartService.addCartItem(createRequest, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    // ==================== getCartItems 테스트 ====================

    @Test
    @DisplayName("장바구니 목록 조회 성공")
    void getCartItems_Success() {
        // given
        CartItem cartItem1 = createCartItem(1L, 1L, 1L, 2, true);
        CartItem cartItem2 = createCartItem(2L, 2L, 2L, 1, true);

        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(Arrays.asList(cartItem1, cartItem2));
        given(productOptionRepository.findAllByOptionIdIn(any())).willReturn(Arrays.asList(productOption1, productOption2));
        given(productImageRepository.findAllById(any())).willReturn(Arrays.asList(productImage1, productImage2));

        given(discountCalculator.calculateDiscountPrice(new BigDecimal("50000"), DiscountType.RATE_DISCOUNT, new BigDecimal("10")))
                .willReturn(new BigDecimal("45000"));
        given(discountCalculator.calculateDiscountPrice(new BigDecimal("30000"), DiscountType.NONE, null))
                .willReturn(new BigDecimal("30000"));

        // when
        CartItemListResponse response = cartService.getCartItems("test@test.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalItemCount()).isEqualTo(2);
        assertThat(response.getTotalOriginalPrice()).isEqualTo(new BigDecimal("130000"));
        assertThat(response.getTotalDiscountPrice()).isEqualTo(new BigDecimal("120000"));
        assertThat(response.getTotalSavings()).isEqualTo(new BigDecimal("10000"));

        verify(cartItemRepository, times(1)).findByUserIdWithCart(1L);
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공 - 빈 장바구니")
    void getCartItems_Success_EmptyCart() {
        // given
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(List.of());

        // when
        CartItemListResponse response = cartService.getCartItems("test@test.com");

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
    @DisplayName("장바구니 목록 조회 실패 - 사용자 없음")
    void getCartItems_Fail_UserNotFound() {
        // given
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.empty());

        // when & then
        BusinessException thrown = assertThrows(BusinessException.class, () ->
                cartService.getCartItems("test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(cartItemRepository, never()).findByUserIdWithCart(any());
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공 - 선택되지 않은 아이템 포함")
    void getCartItems_Success_WithUnselectedItems() {
        // given
        CartItem selectedItem = createCartItem(1L, 1L, 1L, 2, true);
        CartItem unselectedItem = createCartItem(2L, 2L, 2L, 1, false);

        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(Arrays.asList(selectedItem, unselectedItem));
        given(productOptionRepository.findAllByOptionIdIn(any())).willReturn(Arrays.asList(productOption1, productOption2));
        given(productImageRepository.findAllById(any())).willReturn(Arrays.asList(productImage1, productImage2));

        given(discountCalculator.calculateDiscountPrice(new BigDecimal("50000"), DiscountType.RATE_DISCOUNT, new BigDecimal("10")))
                .willReturn(new BigDecimal("45000"));
        given(discountCalculator.calculateDiscountPrice(new BigDecimal("30000"), DiscountType.NONE, null))
                .willReturn(new BigDecimal("30000"));

        // when
        CartItemListResponse response = cartService.getCartItems("test@test.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalOriginalPrice()).isEqualTo(new BigDecimal("100000"));
        assertThat(response.getTotalDiscountPrice()).isEqualTo(new BigDecimal("90000"));
        assertThat(response.getTotalSavings()).isEqualTo(new BigDecimal("10000"));
    }

    // ==================== deleteCartItem 테스트 ====================

    @Test
    @DisplayName("장바구니 상품 삭제 성공")
    void deleteCartItem_Success() {
        // given
        Long cartItemId = 1L;
        CartItem cartItem = createCartItem(cartItemId, 1L, 1L, 2, true);

        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByCartItemIdAndUserId(cartItemId, 1L))
                .willReturn(Optional.of(cartItem));

        // when
        cartService.deleteCartItem(cartItemId, "test@test.com");

        // then
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartItemRepository, times(1)).findByCartItemIdAndUserId(cartItemId, 1L);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 실패 - 사용자 없음")
    void deleteCartItem_Fail_UserNotFound() {
        // given
        Long cartItemId = 1L;
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.empty());

        // when & then
        BusinessException thrown = assertThrows(BusinessException.class, () ->
                cartService.deleteCartItem(cartItemId, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(cartItemRepository, never()).findByCartItemIdAndUserId(any(), any());
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("장바구니 상품 삭제 실패 - 장바구니 아이템 없음")
    void deleteCartItem_Fail_CartItemNotFound() {
        // given
        Long cartItemId = 999L;

        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByCartItemIdAndUserId(cartItemId, 1L))
                .willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                cartService.deleteCartItem(cartItemId, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("장바구니 상품 삭제 실패 - 다른 사용자 아이템")
    void deleteCartItem_Fail_NotOwnerItem() {
        // given
        Long cartItemId = 1L;

        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByCartItemIdAndUserId(cartItemId, 1L))
                .willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                cartService.deleteCartItem(cartItemId, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
        verify(cartItemRepository, never()).delete(any());
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