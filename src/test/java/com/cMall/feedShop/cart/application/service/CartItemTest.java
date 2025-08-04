package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CartItem 도메인 테스트")
class CartItemTest {

    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        User user = new User("testUser", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        cart = Cart.builder()
                .user(user)
                .build();
        ReflectionTestUtils.setField(cart, "cartId", 1L);

        cartItem = CartItem.builder()
                .cart(cart)
                .optionId(1L)
                .imageId(1L)
                .quantity(5)
                .build();
        ReflectionTestUtils.setField(cartItem, "cartItemId", 1L);
    }

    @Test
    @DisplayName("CartItem 생성 성공")
    void createCartItem_Success() {
        // when & then
        assertThat(cartItem.getCartItemId()).isEqualTo(1L);
        assertThat(cartItem.getCart()).isEqualTo(cart);
        assertThat(cartItem.getOptionId()).isEqualTo(1L);
        assertThat(cartItem.getImageId()).isEqualTo(1L);
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("수량 업데이트 성공")
    void updateQuantity_Success() {
        // when
        cartItem.updateQuantity(10);

        // then
        assertThat(cartItem.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("수량 업데이트 실패 - 0개")
    void updateQuantity_Fail_ZeroQuantity() {
        // when & then
        CartException thrown = assertThrows(CartException.class, () -> cartItem.updateQuantity(0));

        assertThat(thrown.getErrorCode().getMessage()).contains("수량은 1개 이상이어야 합니다");
    }

    @Test
    @DisplayName("수량 업데이트 실패 - 음수")
    void updateQuantity_Fail_NegativeQuantity() {
        // when & then
        CartException thrown = assertThrows(CartException.class, () -> cartItem.updateQuantity(-1));

        assertThat(thrown.getErrorCode().getMessage()).contains("수량은 1개 이상이어야 합니다");
    }

    @Test
    @DisplayName("최소 수량 1개로 업데이트 성공")
    void updateQuantity_Success_MinQuantity() {
        // when
        cartItem.updateQuantity(1);

        // then
        assertThat(cartItem.getQuantity()).isEqualTo(1);
    }
}