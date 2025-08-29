package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.response.WishListResponse;
import com.cMall.feedShop.cart.application.dto.response.info.WishlistInfo;
import com.cMall.feedShop.cart.domain.repository.WishlistRepository;
import com.cMall.feedShop.product.application.utils.PagingUtils;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistReadService {

    private final WishlistRepository wishlistRepository;
    private final WishlistHelper wishlistHelper;

    public WishListResponse getWishList(int page, int size, String loginId) {
        // 1. 사용자 조회
        User user = wishlistHelper.getCurrentUser(loginId);

        // 2. 전체 찜한 상품 개수 조회
        long totalElements = wishlistRepository.countWishlistByUserId(user.getId());

        // 3. 페이징 검증
        Pageable pageable = PagingUtils.createPageable(page, size, totalElements);

        // 4. 찜 목록 조회
        Page<WishlistInfo> wishlistPage = wishlistRepository.findWishlistByUserId(user.getId(), pageable);

        // 5. 결과 반환
        return WishListResponse.of(wishlistPage);
    }
}