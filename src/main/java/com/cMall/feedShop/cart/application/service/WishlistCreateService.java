package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.WishListRequest;
import com.cMall.feedShop.cart.application.dto.response.WishListCreateResponse;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.cart.domain.repository.WishlistRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistCreateService {

    private final WishlistRepository wishlistRepository;
    private final WishlistHelper wishlistHelper;

    public WishListCreateResponse addWishList(WishListRequest request, String loginId) {
        // 1. 사용자 조회
        User user = wishlistHelper.getCurrentUser(loginId);

        // 2. 상품 조회
        Product product = wishlistHelper.getProduct(request.getProductId());

        // 3. 중복 찜 검증
        if (wishlistRepository.existsByUserIdAndProduct_ProductIdAndDeletedAtIsNull(user.getId(), product.getProductId())) {
            throw new CartException(ErrorCode.ALREADY_WISHED_PRODUCT);
        }

        // 4. 찜 생성
        WishList wishlist = WishList.builder()
                .user(user)
                .product(product)
                .build();

        // 5. DB 저장
        WishList savedWishList = wishlistRepository.save(wishlist);

        // 6. 상품 찜 수 증가
        wishlistRepository.increaseWishCount(product.getProductId());

        // 7. 찜 목록 응답 생성
        return WishListCreateResponse.from(savedWishList);
    }
}