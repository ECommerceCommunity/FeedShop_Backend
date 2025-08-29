package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.CartItemUpdateRequest;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartUpdateService 테스트")
class CartUpdateServiceTest {

    @Mock private CartItemRepository cartItemRepository;
    @Mock private CartHelper cartHelper;

    @InjectMocks
    private CartUpdateService cartUpdateService;

    // 테스트 데이터
    private User user;
    private Cart cart;
    private Store store;
    private Category category;
    private Product product;
    private ProductOption productOption;
    private CartItem cartItem;

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

        // Cart 설정
        cart = Cart.builder().user(user).build();
        ReflectionTestUtils.setField(cart, "cartId", 1L);
        user.setCart(cart);

        // CartItem 설정
        cartItem = CartItem.builder()
                .cart(cart)
                .optionId(1L)
                .imageId(1L)
                .quantity(2)
                .build();
        ReflectionTestUtils.setField(cartItem, "cartItemId", 1L);
        ReflectionTestUtils.setField(cartItem, "selected", true);
        ReflectionTestUtils.setField(cartItem, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(cartItem, "updatedAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("장바구니 아이템 수량 수정 성공")
    void updateCartItem_Success_UpdateQuantity() {
        // given
        CartItemUpdateRequest request = new CartItemUpdateRequest();
        ReflectionTestUtils.setField(request, "quantity", 5);

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByCartItemIdAndUserId(1L, 1L)).willReturn(Optional.of(cartItem));
        given(cartHelper.validateProductOption(1L)).willReturn(productOption);

        given(cartItemRepository.save(cartItem)).willReturn(cartItem);

        // when
        cartUpdateService.updateCartItem(1L, request, "test@test.com");

        // then
        assertThat(cartItem.getQuantity()).isEqualTo(5);
        verify(cartHelper, times(1)).validateStock(productOption, 5);
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    @DisplayName("장바구니 아이템 선택 상태 수정 성공")
    void updateCartItem_Success_UpdateSelected() {
        // given
        CartItemUpdateRequest request = new CartItemUpdateRequest();
        ReflectionTestUtils.setField(request, "selected", false);

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByCartItemIdAndUserId(1L, 1L)).willReturn(Optional.of(cartItem));
        given(cartItemRepository.save(cartItem)).willReturn(cartItem);

        // when
        cartUpdateService.updateCartItem(1L, request, "test@test.com");

        // then
        assertThat(cartItem.getSelected()).isFalse();
        verify(cartHelper, never()).validateProductOption(any());
        verify(cartHelper, never()).validateStock(any(), any());
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    @DisplayName("장바구니 아이템 수량과 선택 상태 동시 수정 성공")
    void updateCartItem_Success_UpdateBoth() {
        // given
        CartItemUpdateRequest request = new CartItemUpdateRequest();
        ReflectionTestUtils.setField(request, "quantity", 3);
        ReflectionTestUtils.setField(request, "selected", false);

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByCartItemIdAndUserId(1L, 1L)).willReturn(Optional.of(cartItem));
        given(cartHelper.validateProductOption(1L)).willReturn(productOption);
        given(cartItemRepository.save(cartItem)).willReturn(cartItem);

        // when
        cartUpdateService.updateCartItem(1L, request, "test@test.com");

        // then
        assertThat(cartItem.getQuantity()).isEqualTo(3);
        assertThat(cartItem.getSelected()).isFalse();
        verify(cartHelper, times(1)).validateStock(productOption, 3);
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    @DisplayName("장바구니 아이템 수정 실패 - 장바구니 아이템 없음")
    void updateCartItem_Fail_CartItemNotFound() {
        // given
        CartItemUpdateRequest request = new CartItemUpdateRequest();
        ReflectionTestUtils.setField(request, "quantity", 5);

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByCartItemIdAndUserId(1L, 1L)).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                cartUpdateService.updateCartItem(1L, request, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("장바구니 아이템 수정 실패 - 재고 부족")
    void updateCartItem_Fail_OutOfStock() {
        // given
        CartItemUpdateRequest request = new CartItemUpdateRequest();
        ReflectionTestUtils.setField(request, "quantity", 200);

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByCartItemIdAndUserId(1L, 1L)).willReturn(Optional.of(cartItem));
        given(cartHelper.validateProductOption(1L)).willReturn(productOption);
        doThrow(new ProductException(ErrorCode.OUT_OF_STOCK))
                .when(cartHelper).validateStock(productOption, 200);

        // when & then
        ProductException thrown = assertThrows(ProductException.class, () ->
                cartUpdateService.updateCartItem(1L, request, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("장바구니 아이템 수정 성공 - 아무것도 변경하지 않음")
    void updateCartItem_Success_NoChange() {
        // given
        CartItemUpdateRequest request = new CartItemUpdateRequest();

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByCartItemIdAndUserId(1L, 1L)).willReturn(Optional.of(cartItem));
        given(cartItemRepository.save(cartItem)).willReturn(cartItem);

        // when
        cartUpdateService.updateCartItem(1L, request, "test@test.com");

        // then
        verify(cartHelper, never()).validateProductOption(any());
        verify(cartHelper, never()).validateStock(any(), any());
        verify(cartItemRepository, times(1)).save(cartItem);
    }
}