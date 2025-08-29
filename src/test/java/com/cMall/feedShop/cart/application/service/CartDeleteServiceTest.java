package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartDeleteService 테스트")
class CartDeleteServiceTest {

    @Mock private CartItemRepository cartItemRepository;
    @Mock private CartHelper cartHelper;

    @InjectMocks
    private CartDeleteService cartDeleteService;

    // 테스트 데이터
    private User user;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // User 설정
        user = new User("test@test.com", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

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
    @DisplayName("장바구니 상품 삭제 성공")
    void deleteCartItem_Success() {
        // given
        Long cartItemId = 1L;

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByCartItemIdAndUserId(cartItemId, 1L))
                .willReturn(Optional.of(cartItem));

        // when
        cartDeleteService.deleteCartItem(cartItemId, "test@test.com");

        // then
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartItemRepository, times(1)).findByCartItemIdAndUserId(cartItemId, 1L);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 실패 - 장바구니 아이템 없음")
    void deleteCartItem_Fail_CartItemNotFound() {
        // given
        Long cartItemId = 999L;

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByCartItemIdAndUserId(cartItemId, 1L))
                .willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                cartDeleteService.deleteCartItem(cartItemId, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("장바구니 상품 삭제 실패 - 다른 사용자 아이템")
    void deleteCartItem_Fail_NotOwnerItem() {
        // given
        Long cartItemId = 1L;

        given(cartHelper.getCurrentUser("test@test.com")).willReturn(user);
        given(cartItemRepository.findByCartItemIdAndUserId(cartItemId, 1L))
                .willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(CartException.class, () ->
                cartDeleteService.deleteCartItem(cartItemId, "test@test.com"));

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
        verify(cartItemRepository, never()).delete(any());
    }
}