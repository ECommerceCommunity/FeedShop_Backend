package com.cMall.feedShop.cart.domain.repository;

import com.cMall.feedShop.cart.domain.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

}
