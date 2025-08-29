package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.CartItemUpdateRequest;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장바구니 상품 수정 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartUpdateService {
    
    private final CartItemRepository cartItemRepository;
    private final CartHelper cartHelper;

    /**
     * 장바구니 아이템을 업데이트하는 서비스 메서드
     *
     * @param cartItemId 장바구니 아이템 ID
     * @param request 업데이트 요청
     * @param loginId 현재 로그인한 사용자 정보
     */
    public void updateCartItem(Long cartItemId, CartItemUpdateRequest request, String loginId) {
        log.info("장바구니 아이템 수정 시작 - cartItemId: {}", cartItemId);
        
        // 1. 현재 사용자 조회
        User currentUser = cartHelper.getCurrentUser(loginId);

        // 2. 장바구니 아이템 조회
        CartItem cartItem = cartItemRepository.findByCartItemIdAndUserId(cartItemId, currentUser.getId())
                .orElseThrow(() -> new CartException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 3. 수량 변경 처리
        if (request.getQuantity() != null) {
            log.info("장바구니 아이템 수량 변경 - cartItemId: {}, 기존 수량: {}, 새 수량: {}", 
                    cartItemId, cartItem.getQuantity(), request.getQuantity());
            
            // 재고 확인
            ProductOption productOption = cartHelper.validateProductOption(cartItem.getOptionId());
            cartHelper.validateStock(productOption, request.getQuantity());

            // 수량 업데이트
            cartItem.updateQuantity(request.getQuantity());
        }

        // 4. 선택 상태 변경 처리
        if (request.getSelected() != null) {
            log.info("장바구니 아이템 선택 상태 변경 - cartItemId: {}, 기존 선택: {}, 새 선택: {}", 
                    cartItemId, cartItem.getSelected(), request.getSelected());
            
            cartItem.updateSelected(request.getSelected());
        }

        // 5. DB에 저장 (트랜잭션 관리)
        cartItemRepository.save(cartItem);
        
        log.info("장바구니 아이템 수정 완료 - cartItemId: {}", cartItemId);
    }
}