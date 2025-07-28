package com.cMall.feedShop.cart.application.service;
import com.cMall.feedShop.cart.application.dto.response.CartItemResponse;
import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CartItemResponse 테스트")
class CartItemResponseTest {

    private CartItem cartItem;
    private Cart cart;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testUser", "password", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        cart = Cart.builder().user(user).build();
        ReflectionTestUtils.setField(cart, "cartId", 1L);

        cartItem = CartItem.builder()
                .cart(cart)
                .optionId(1L)
                .imageId(1L)
                .quantity(5)
                .build();
        ReflectionTestUtils.setField(cartItem, "cartItemId", 1L);
        ReflectionTestUtils.setField(cartItem, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(cartItem, "updatedAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("CartItem에서 CartItemResponse 변환 성공")
    void from_Success() {
        // when
        CartItemResponse response = CartItemResponse.from(cartItem);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCartItemId()).isEqualTo(1L);
        assertThat(response.getCartId()).isEqualTo(1L);
        assertThat(response.getOptionId()).isEqualTo(1L);
        assertThat(response.getImageId()).isEqualTo(1L);
        assertThat(response.getQuantity()).isEqualTo(5);
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("null CartItem에서 null 반환")
    void from_NullCartItem_ReturnsNull() {
        // when
        CartItemResponse response = CartItemResponse.from(null);

        // then
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Cart가 null인 CartItem 처리")
    void from_NullCart() {
        // given
        CartItem cartItemWithNullCart = CartItem.builder()
                .cart(null)
                .optionId(1L)
                .imageId(1L)
                .quantity(3)
                .build();
        ReflectionTestUtils.setField(cartItemWithNullCart, "cartItemId", 2L);

        // when
        CartItemResponse response = CartItemResponse.from(cartItemWithNullCart);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCartItemId()).isEqualTo(2L);
        assertThat(response.getCartId()).isNull();
        assertThat(response.getOptionId()).isEqualTo(1L);
        assertThat(response.getImageId()).isEqualTo(1L);
        assertThat(response.getQuantity()).isEqualTo(3);
    }
}