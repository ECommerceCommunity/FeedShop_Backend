package com.cMall.feedShop.cart.infrastructure.repository;

public interface WishlistQueryRepository {

    // 찜 수 증가
    void increaseWishCount(Long productId);

}
