package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.CartItemCreateRequest;
import com.cMall.feedShop.cart.application.dto.response.CartItemListResponse;
import com.cMall.feedShop.cart.application.dto.response.CartItemResponse;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemQueryRepository;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.cart.domain.repository.CartRepository;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.application.util.DiscountCalculator;
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
import org.springframework.security.core.userdetails.UserDetails;
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
    @Mock private UserDetails userDetails;

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
        user = new User("testUser", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        // Cart 설정
        cart = Cart.builder().user(user).build();
        ReflectionTestUtils.setField(cart, "cartId", 1L);
        user.setCart(cart);

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
        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
//        given(userRepository.findById(1L)).willReturn(Optional.of(user)); // 추가
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(productOption1));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.of(productImage1));
        given(cartItemRepository.findByCartAndOptionIdAndImageId(cart, 1L, 1L)).willReturn(Optional.empty());

        CartItem savedCartItem = CartItem.builder()
                .cart(cart)
                .optionId(1L)
                .imageId(1L)
                .quantity(2)
                .build();
        ReflectionTestUtils.setField(savedCartItem, "cartItemId", 1L);
        ReflectionTestUtils.setField(savedCartItem, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(savedCartItem, "updatedAt", LocalDateTime.now());

        given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> {
            CartItem cartItem = invocation.getArgument(0);
            ReflectionTestUtils.setField(cartItem, "cartItemId", 1L);
            ReflectionTestUtils.setField(cartItem, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(cartItem, "updatedAt", LocalDateTime.now());
            return cartItem;
        });

        // when
        CartItemResponse response = cartService.addCartItem(createRequest, userDetails);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCartItemId()).isEqualTo(1L);
        assertThat(response.getCartId()).isEqualTo(1L);
        assertThat(response.getOptionId()).isEqualTo(1L);
        assertThat(response.getImageId()).isEqualTo(1L);
        assertThat(response.getQuantity()).isEqualTo(2);

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(userRepository, times(1)).findByLoginId("test@test.com");
        verify(productOptionRepository, times(1)).findByOptionId(1L);
        verify(productImageRepository, times(1)).findByImageId(1L);
    }

    @Test
    @DisplayName("장바구니 추가 실패 - 사용자 없음")
    void addCartItem_Fail_UserNotFound() {
        // given
        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.empty());

        // when & then
        BusinessException thrown = assertThrows(BusinessException.class, () ->
                cartService.addCartItem(createRequest, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(productOptionRepository, never()).findByOptionId(any());
    }

    @Test
    @DisplayName("장바구니 추가 실패 - 상품 옵션 없음")
    void addCartItem_Fail_ProductOptionNotFound() {
        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        // 불필요한 stubbing 제거
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.empty());

        ProductException.ProductOptionNotFoundException thrown = assertThrows(
                ProductException.ProductOptionNotFoundException.class, () ->
                        cartService.addCartItem(createRequest, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 추가 실패 - 상품 이미지 없음")
    void addCartItem_Fail_ProductImageNotFound() {
        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        // 불필요한 stubbing 제거
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(productOption1));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.empty());

        ProductException.ProductImageNotFoundException thrown = assertThrows(
                ProductException.ProductImageNotFoundException.class, () ->
                        cartService.addCartItem(createRequest, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 추가 실패 - 재고 부족")
    void addCartItem_Fail_OutOfStock() {
        ProductOption lowStockOption = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 1, product1);
        ReflectionTestUtils.setField(lowStockOption, "optionId", 1L);

        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        // 불필요한 stubbing 제거
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(lowStockOption));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.of(productImage1));

        ProductException.OutOfStockException thrown = assertThrows(
                ProductException.OutOfStockException.class, () ->
                        cartService.addCartItem(createRequest, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 추가 실패 - 재고 없음")
    void addCartItem_Fail_NoStock() {
        ProductOption noStockOption = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 0, product1);
        ReflectionTestUtils.setField(noStockOption, "optionId", 1L);

        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        // 불필요한 stubbing 제거
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(noStockOption));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.of(productImage1));

        ProductException.OutOfStockException thrown = assertThrows(
                ProductException.OutOfStockException.class, () ->
                        cartService.addCartItem(createRequest, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니가 없는 사용자 - 새 장바구니 생성")
    void addCartItem_Success_CreateNewCart() {
        // given
        User userWithoutCart = new User("testUser", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(userWithoutCart, "id", 1L);
        userWithoutCart.setCart(null);

        Cart newCart = Cart.builder()
                .user(userWithoutCart)
                .build();
        ReflectionTestUtils.setField(newCart, "cartId", 2L);

        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(userWithoutCart));
//        given(userRepository.findById(1L)).willReturn(Optional.of(userWithoutCart)); // 추가
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(productOption1));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.of(productImage1));
        given(cartRepository.save(any(Cart.class))).willReturn(newCart);
        given(cartItemRepository.findByCartAndOptionIdAndImageId(any(Cart.class), eq(1L), eq(1L)))
                .willReturn(Optional.empty());

        CartItem savedCartItem = CartItem.builder()
                .cart(newCart)
                .optionId(1L)
                .imageId(1L)
                .quantity(2)
                .build();
        ReflectionTestUtils.setField(savedCartItem, "cartItemId", 1L);
        given(cartItemRepository.save(any(CartItem.class))).willReturn(savedCartItem);

        // when
        CartItemResponse response = cartService.addCartItem(createRequest, userDetails);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(2L);
        assertThat(response.getQuantity()).isEqualTo(2);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    // ==================== getCartItems 테스트 ====================

    @Test
    @DisplayName("장바구니 목록 조회 성공")
    void getCartItems_Success() {
        // given
        CartItem cartItem1 = createCartItem(1L, 1L, 1L, 2, true);
        CartItem cartItem2 = createCartItem(2L, 2L, 2L, 1, true);

        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(Arrays.asList(cartItem1, cartItem2));
        given(productOptionRepository.findAllByOptionIdIn(any())).willReturn(Arrays.asList(productOption1, productOption2));
        given(productImageRepository.findAllById(any())).willReturn(Arrays.asList(productImage1, productImage2));

        // 할인가 계산 모킹
        given(discountCalculator.calculateDiscountPrice(new BigDecimal("50000"), DiscountType.RATE_DISCOUNT, new BigDecimal("10")))
                .willReturn(new BigDecimal("45000"));
        given(discountCalculator.calculateDiscountPrice(new BigDecimal("30000"), DiscountType.NONE, null))
                .willReturn(new BigDecimal("30000"));

        // when
        CartItemListResponse response = cartService.getCartItems(userDetails);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalItemCount()).isEqualTo(2);

        // 총 금액 계산 확인 (상품1: 50000*2 + 상품2: 30000*1 = 130000)
        assertThat(response.getTotalOriginalPrice()).isEqualTo(new BigDecimal("130000"));
        // 할인 적용 금액 (상품1: 45000*2 + 상품2: 30000*1 = 120000)
        assertThat(response.getTotalDiscountPrice()).isEqualTo(new BigDecimal("120000"));
        // 절약 금액 (130000 - 120000 = 10000)
        assertThat(response.getTotalSavings()).isEqualTo(new BigDecimal("10000"));

        verify(cartItemRepository, times(1)).findByUserIdWithCart(1L);
        verify(productOptionRepository, times(1)).findAllByOptionIdIn(any());
        verify(productImageRepository, times(1)).findAllById(any());
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공 - 빈 장바구니")
    void getCartItems_Success_EmptyCart() {
        // given
        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(List.of());

        // when
        CartItemListResponse response = cartService.getCartItems(userDetails);

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
        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.empty());

        // when & then
        BusinessException thrown = assertThrows(BusinessException.class, () ->
                cartService.getCartItems(userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(cartItemRepository, never()).findByUserIdWithCart(any());
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공 - 선택되지 않은 아이템 포함")
    void getCartItems_Success_WithUnselectedItems() {
        // given
        CartItem selectedItem = createCartItem(1L, 1L, 1L, 2, true);
        CartItem unselectedItem = createCartItem(2L, 2L, 2L, 1, false);

        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(Arrays.asList(selectedItem, unselectedItem));
        given(productOptionRepository.findAllByOptionIdIn(any())).willReturn(Arrays.asList(productOption1, productOption2));
        given(productImageRepository.findAllById(any())).willReturn(Arrays.asList(productImage1, productImage2));

        given(discountCalculator.calculateDiscountPrice(new BigDecimal("50000"), DiscountType.RATE_DISCOUNT, new BigDecimal("10")))
                .willReturn(new BigDecimal("45000"));
        given(discountCalculator.calculateDiscountPrice(new BigDecimal("30000"), DiscountType.NONE, null))
                .willReturn(new BigDecimal("30000"));

        // when
        CartItemListResponse response = cartService.getCartItems(userDetails);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(2);

        // 선택된 아이템만 계산 (selectedItem만 선택됨)
        assertThat(response.getTotalOriginalPrice()).isEqualTo(new BigDecimal("100000")); // 50000 * 2
        assertThat(response.getTotalDiscountPrice()).isEqualTo(new BigDecimal("90000")); // 45000 * 2
        assertThat(response.getTotalSavings()).isEqualTo(new BigDecimal("10000")); // 100000 - 90000
    }

    // ==================== 기존 수량 증가 시 재고 확인 테스트 ====================

    @Test
    @DisplayName("기존 아이템 수량 증가 시 재고 확인 - 실패")
    void addCartItem_Fail_ExistingItem_InsufficientStock() {
        // given
        CartItem existingCartItem = CartItem.builder()
                .cart(cart)
                .optionId(1L)
                .imageId(1L)
                .quantity(99) // 기존 수량
                .build();
        ReflectionTestUtils.setField(existingCartItem, "cartItemId", 1L);

        // 재고가 100개인 옵션에 99개가 이미 있고, 2개를 더 추가하려고 함 (총 101개 > 100개)
        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
//        given(userRepository.findById(1L)).willReturn(Optional.of(user)); // 추가
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(productOption1));
        given(productImageRepository.findByImageId(1L)).willReturn(Optional.of(productImage1));
        given(cartItemRepository.findByCartAndOptionIdAndImageId(cart, 1L, 1L))
                .willReturn(Optional.of(existingCartItem));

        // when & then
        ProductException.OutOfStockException thrown = assertThrows(
                ProductException.OutOfStockException.class, () ->
                        cartService.addCartItem(createRequest, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
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

    @Test
    @DisplayName("장바구니 상품 삭제 성공")
    void deleteCartItem_Success() {
        // given
        Long cartItemId = 1L;
        CartItem cartItem = createCartItem(cartItemId, 1L, 1L, 2, true);

        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByCartItemIdAndUserId(cartItemId, 1L))
                .willReturn(Optional.of(cartItem));

        // when
        cartService.deleteCartItem(cartItemId, userDetails);

        // then
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartItemRepository, times(1)).findByCartItemIdAndUserId(cartItemId, 1L);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 실패 - 사용자 없음")
    void deleteCartItem_Fail_UserNotFound() {
        // given
        Long cartItemId = 1L;

        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.empty());

        // when & then
        BusinessException thrown = assertThrows(BusinessException.class, () ->
                cartService.deleteCartItem(cartItemId, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(cartItemRepository, never()).findByCartItemIdAndUserId(any(), any());
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("장바구니 상품 삭제 실패 - 장바구니 아이템 없음")
    void deleteCartItem_Fail_CartItemNotFound() {
        // given
        Long cartItemId = 999L;

        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByCartItemIdAndUserId(cartItemId, 1L))
                .willReturn(Optional.empty());

        // when & then
        CartException.CartItemNotFoundException thrown = assertThrows(
                CartException.CartItemNotFoundException.class, () ->
                        cartService.deleteCartItem(cartItemId, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("장바구니 상품 삭제 실패 - 다른 사용자 아이템")
    void deleteCartItem_Fail_NotOwnerItem() {
        // given
        Long cartItemId = 1L;

        given(userDetails.getUsername()).willReturn("test@test.com");
        given(userRepository.findByLoginId("test@test.com")).willReturn(Optional.of(user));
        given(cartItemRepository.findByCartItemIdAndUserId(cartItemId, 1L))
                .willReturn(Optional.empty()); // 다른 사용자의 아이템이므로 조회되지 않음

        // when & then
        CartException.CartItemNotFoundException thrown = assertThrows(
                CartException.CartItemNotFoundException.class, () ->
                        cartService.deleteCartItem(cartItemId, userDetails));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
        verify(cartItemRepository, never()).delete(any());
    }
}