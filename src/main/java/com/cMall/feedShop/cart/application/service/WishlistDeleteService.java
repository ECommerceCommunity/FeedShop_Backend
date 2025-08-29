package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.cart.domain.repository.WishlistRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistDeleteService {

    private final WishlistRepository wishlistRepository;
    private final WishlistHelper wishlistHelper;

    public void deleteWishList(Long productId, String loginId) {
        // 1. 사용자 조회
        User currentUser = wishlistHelper.getCurrentUser(loginId);

        // 2. 찜한 상품 조회
        WishList wishlist = wishlistRepository.findByUserIdAndProduct_ProductIdAndDeletedAtIsNull(
                        currentUser.getId(), productId)
                .orElseThrow(() -> new CartException(ErrorCode.WISHLIST_ITEM_NOT_FOUND));

        // 3. 찜한 상품 삭제
        wishlist.delete();

        // 4. DB 저장
        wishlistRepository.save(wishlist);

        // 5. 상품 찜 수 감소
        wishlistRepository.decreaseWishCount(productId);
    }
}