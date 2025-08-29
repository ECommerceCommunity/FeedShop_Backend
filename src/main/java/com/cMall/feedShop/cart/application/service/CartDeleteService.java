package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장바구니 상품 삭제 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartDeleteService {
    
    private final CartItemRepository cartItemRepository;
    private final CartHelper cartHelper;

    /**
     * 장바구니 아이템을 삭제하는 서비스 메서드
     *
     * @param cartItemId 장바구니 아이템 ID
     * @param loginId 현재 로그인한 사용자 정보
     */
    public void deleteCartItem(Long cartItemId, String loginId) {
        log.info("장바구니 아이템 삭제 시작 - cartItemId: {}", cartItemId);
        
        // 1. 현재 사용자 조회
        User currentUser = cartHelper.getCurrentUser(loginId);

        // 2. 장바구니 아이템 조회
        CartItem cartItem = cartItemRepository.findByCartItemIdAndUserId(cartItemId, currentUser.getId())
                .orElseThrow(() -> new CartException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 3. 장바구니 아이템 삭제
        cartItemRepository.delete(cartItem);
        
        log.info("장바구니 아이템 삭제 완료 - cartItemId: {}", cartItemId);
    }
}