package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cart 도메인 테스트")
class CartTest {

    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User("testUser", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        cart = Cart.builder()
                .user(user)
                .build();
        ReflectionTestUtils.setField(cart, "cartId", 1L);
    }

    @Test
    @DisplayName("Cart 생성 성공")
    void createCart_Success() {
        // when & then
        assertThat(cart.getCartId()).isEqualTo(1L);
        assertThat(cart.getUser()).isEqualTo(user);
        assertThat(cart.getCartItems()).isEmpty();
    }

    @Test
    @DisplayName("Cart에 아이템 추가 성공")
    void addCartItem_Success() {
        // given
        CartItem cartItem1 = CartItem.builder()
                .cart(cart)
                .optionId(1L)
                .imageId(1L)
                .quantity(2)
                .build();

        CartItem cartItem2 = CartItem.builder()
                .cart(cart)
                .optionId(2L)
                .imageId(2L)
                .quantity(3)
                .build();

        // when
        cart.getCartItems().add(cartItem1);
        cart.getCartItems().add(cartItem2);

        // then
        assertThat(cart.getCartItems()).hasSize(2);
        assertThat(cart.getCartItems()).containsExactly(cartItem1, cartItem2);
    }

    @Test
    @DisplayName("빈 Cart 확인")
    void emptyCart_Validation() {
        // when & then
        assertThat(cart.getCartItems()).isEmpty();
        assertThat(cart.getCartItems()).hasSize(0);
    }
}