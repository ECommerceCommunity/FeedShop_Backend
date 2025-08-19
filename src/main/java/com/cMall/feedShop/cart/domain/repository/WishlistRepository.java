package com.cMall.feedShop.cart.domain.repository;

import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.cart.infrastructure.repository.WishlistQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<WishList, Long>, WishlistQueryRepository {
}
