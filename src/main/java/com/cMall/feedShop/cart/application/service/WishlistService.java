package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.WishListRequest;
import com.cMall.feedShop.cart.application.dto.response.WishListAddResponse;
import com.cMall.feedShop.cart.application.dto.response.WishListResponse;
import com.cMall.feedShop.cart.application.dto.response.info.WishlistInfo;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.cart.domain.repository.WishlistRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.product.application.utils.PagingUtils;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    // 찜한 상품 목록 조회
    public WishListResponse getWishList(int page, int size, String loginId) {
        // 1. 사용자 조회
        User user = getCurrentUser(loginId);

        // 2. 전체 찜한 상품 개수 조회
        long totalElements = wishlistRepository.countWishlistByUserId(user.getId());

        // 3. 페이징 검증
        Pageable pageable = PagingUtils.createPageable(page, size, totalElements);

        // 4. 찜 목록 조회
        Page<WishlistInfo> wishlistPage = wishlistRepository.findWishlistByUserId(user.getId(), pageable);

        // 5. 결과 반환
        return WishListResponse.of(wishlistPage);
    }

    @Transactional
    public WishListAddResponse addWishList(WishListRequest request, String loginId) {
        // 1. 사용자 조회
        User user = getCurrentUser(loginId);

        // 2. 상품 조회
        Product product = getProduct(request.getProductId());

        // 4. 찜 생성
        try {
            WishList wishlist = WishList.builder()
                    .user(user)
                    .product(product)
                    .build();

            // DB 저장
            WishList savedWishList = wishlistRepository.save(wishlist);

            // 상품 찜 수 증가
            wishlistRepository.increaseWishCount(product.getProductId());

            // 찜 목록 응답 생성
            return WishListAddResponse.from(savedWishList);
        } catch (DataIntegrityViolationException e) {
            // 이미 찜 목록에 있는 경우 예외 처리
            throw new CartException(ErrorCode.ALREADY_WISHED_PRODUCT);
        }
    }

    @Transactional
    public void deleteWishList(Long productId, String loginId) {
        // 1. 사용자 조회
        User currentUser = getCurrentUser(loginId);

        // 2. 찜한 상품 조회
        WishList wishlist = wishlistRepository.findByUserIdAndProductIdAndDeletedAtIsNull(
                        currentUser.getId(), productId)
                .orElseThrow(() -> new CartException(ErrorCode.WISHLIST_ITEM_NOT_FOUND));

        // 3. 찜한 상품 삭제
        wishlist.delete();

        // 4. DB 저장
        wishlistRepository.save(wishlist);

        // 5. 상품 찜 수 감소
        wishlistRepository.decreaseWishCount(productId);
    }

    /**
     * 현재 로그인한 사용자의 정보를 가져옵니다.
     *
     * @param loginId 현재 로그인한 사용자의 ID
     * @return User 객체
     */
    private User getCurrentUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CartException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 주어진 상품 ID로 상품을 조회합니다.
     *
     * @param productId 조회할 상품의 ID
     * @return Product 객체
     */
    private Product getProduct(Long productId) {
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new CartException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
