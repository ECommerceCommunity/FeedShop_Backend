package com.cMall.feedShop.cart.domain.repository;

import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.cart.infrastructure.repository.WishlistQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishList, Long>, WishlistQueryRepository {

    // 사용자 ID와 상품 ID로 찜 목록 조회
    Optional<WishList> findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(Long userId, Long productId);

    // 중복 찜 검증
    boolean existsByUserIdAndProduct_ProductIdAndDeletedAtIsNull(Long userId, Long productId);
}
