package com.cMall.feedShop.cart.infrastructure.repository;

import com.cMall.feedShop.cart.domain.model.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemQueryRepository {

    List<CartItem> findByUserIdWithCart(Long userId);

    Optional<CartItem> findByCartItemIdAndUserId(Long cartItemId, Long userId);

}
