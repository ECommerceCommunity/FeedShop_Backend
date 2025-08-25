package com.cMall.feedShop.cart.infrastructure.repository;

import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.cart.application.dto.response.info.WishlistInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface WishlistQueryRepository {

    // 찜 수 증가
    void increaseWishCount(Long productId);

    // 찜 수 감소
    void decreaseWishCount(Long productId);

    // 사용자 ID로 위시리스트 조회
    Page<WishlistInfo> findWishlistByUserId(Long userId, Pageable pageable);

    // 사용자 ID로 위시리스트 개수 조회
    long countWishlistByUserId(Long userId);

    // 사용자 ID와 상품 ID로 찜 목록 조회 (삭제되지 않은 것만)
    Optional<WishList> findByUserIdAndProductIdAndDeletedAtIsNull(Long userId, Long productId);

}

