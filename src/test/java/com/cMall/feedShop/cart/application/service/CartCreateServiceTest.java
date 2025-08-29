package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.CartItemCreateRequest;
import com.cMall.feedShop.cart.application.dto.response.CartItemResponse;
import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartCreateService 테스트")
class CartCreateServiceTest {

    @Mock private CartItemRepository cartItemRepository;
    @Mock private CartHelper cartHelper;

    @InjectMocks
    private CartCreateService cartCreateService;

    // 테스트 데이터
    private User user;
    private Cart cart;
    private Store store;
    private Category category;
    private Product product;
    private ProductOption productOption;
    private ProductImage productImage;
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

    @Test
    @DisplayName("장바구니에 새 상품 추가 성공")
    void addCartItem_Success_NewItem() {
        // given
        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartHelper.validateProductOption(1L)).willReturn(productOption);
        given(cartHelper.validateProductImage(1L)).willReturn(productImage);
        given(cartHelper.getOrCreateCart(user)).willReturn(cart);
        given(cartItemRepository.findByCartAndOptionIdAndImageId(cart, 1L, 1L)).willReturn(Optional.empty());

        given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> {
            CartItem cartItem = invocation.getArgument(0);
            ReflectionTestUtils.setField(cartItem, "cartItemId", 1L);
            ReflectionTestUtils.setField(cartItem, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(cartItem, "updatedAt", LocalDateTime.now());
            return cartItem;
        });

        // when
        CartItemResponse response = cartCreateService.addCartItem(createRequest, "test@test.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCartItemId()).isEqualTo(1L);
        assertThat(response.getCartId()).isEqualTo(1L);
        assertThat(response.getOptionId()).isEqualTo(1L);
        assertThat(response.getImageId()).isEqualTo(1L);
        assertThat(response.getQuantity()).isEqualTo(2);

        verify(cartHelper, times(1)).validateStock(productOption, 2);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("기존 장바구니 아이템 수량 증가 성공")
    void addCartItem_Success_ExistingItem() {
        // given
        CartItem existingCartItem = CartItem.builder()
                .cart(cart)
                .optionId(1L)
                .imageId(1L)
                .quantity(1)
                .build();
        ReflectionTestUtils.setField(existingCartItem, "cartItemId", 1L);

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartHelper.validateProductOption(1L)).willReturn(productOption);
        given(cartHelper.validateProductImage(1L)).willReturn(productImage);
        given(cartHelper.getOrCreateCart(user)).willReturn(cart);
        given(cartItemRepository.findByCartAndOptionIdAndImageId(cart, 1L, 1L))
                .willReturn(Optional.of(existingCartItem));

        given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> {
            CartItem cartItem = invocation.getArgument(0);
            ReflectionTestUtils.setField(cartItem, "updatedAt", LocalDateTime.now());
            return cartItem;
        });

        // when
        CartItemResponse response = cartCreateService.addCartItem(createRequest, "test@test.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getQuantity()).isEqualTo(3); // 기존 1 + 새로운 2 = 3

        verify(cartHelper, times(1)).validateStock(productOption, 2);    // 초기 검증
        verify(cartHelper, times(1)).validateStock(productOption, 3);    // 증가된 수량 검증
        verify(cartItemRepository, times(1)).save(existingCartItem);
    }

    @Test
    @DisplayName("장바구니 추가 실패 - 상품 옵션 없음")
    void addCartItem_Fail_ProductOptionNotFound() {
        // given
        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartHelper.validateProductOption(1L))
                .willThrow(new ProductException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        cartCreateService.addCartItem(createRequest, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 추가 실패 - 재고 부족")
    void addCartItem_Fail_OutOfStock() {
        // given
        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartHelper.validateProductOption(1L)).willReturn(productOption);
        given(cartHelper.validateProductImage(1L)).willReturn(productImage);
        doThrow(new ProductException(ErrorCode.OUT_OF_STOCK))
                .when(cartHelper).validateStock(productOption, 2);

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        cartCreateService.addCartItem(createRequest, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
        verify(cartItemRepository, never()).save(any(CartItem.class));
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

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartHelper.validateProductOption(1L)).willReturn(productOption);
        given(cartHelper.validateProductImage(1L)).willReturn(productImage);

        doThrow(new ProductException(ErrorCode.OUT_OF_STOCK))
                .when(cartHelper).validateStock(productOption, 2);

        // when & then
        ProductException thrown = assertThrows(
                ProductException.class, () ->
                        cartCreateService.addCartItem(createRequest, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }
}