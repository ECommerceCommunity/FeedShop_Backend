package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.CartItemCreateRequest;
import com.cMall.feedShop.cart.application.dto.response.CartItemResponse;
import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 장바구니 상품 추가 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartCreateService {
    
    private final CartItemRepository cartItemRepository;
    private final CartHelper cartHelper;

    /**
     * 장바구니에 상품을 추가하는 서비스 메서드
     *
     * @param request 장바구니 아이템 생성 요청
     * @param loginId 현재 로그인한 사용자 정보
     * @return CartItemResponse 장바구니 아이템 응답
     */
    public CartItemResponse addCartItem(CartItemCreateRequest request, String loginId) {
        log.info("장바구니 상품 추가 시작 - optionId: {}, quantity: {}", request.getOptionId(), request.getQuantity());
        
        // 1. 현재 사용자 조회
        User currentUser = cartHelper.getCurrentUser(loginId);

        // 2. 상품 옵션 검증
        ProductOption productOption = cartHelper.validateProductOption(request.getOptionId());

        // 3. 상품 이미지 검증
        cartHelper.validateProductImage(request.getImageId());

        // 4. 재고 확인
        cartHelper.validateStock(productOption, request.getQuantity());

        // 5. 장바구니 조회 또는 생성
        Cart cart = cartHelper.getOrCreateCart(currentUser);

        // 6. 해당 장바구니의 아이템에서 cart와 optionId와 imageId로 이미 저장된 같은 아이템이 있는지 조회
        Optional<CartItem> existingCartItem = cartItemRepository
                .findByCartAndOptionIdAndImageId(cart, request.getOptionId(), request.getImageId());

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            // 7. 기존 장바구니 아이템이 있으면 해당 아이템의 수량을 업데이트
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            log.info("기존 장바구니 아이템 수량 업데이트 - cartItemId: {}, 기존 수량: {}, 추가 수량: {}, 총 수량: {}", 
                    cartItem.getCartItemId(), cartItem.getQuantity(), request.getQuantity(), newQuantity);

            // 증가된 수량으로 재고 재확인
            cartHelper.validateStock(productOption, newQuantity);

            // 수량 업데이트
            cartItem.updateQuantity(newQuantity);
        } else {
            // 8. 기존 장바구니 아이템이 없으면 새로운 장바구니 아이템 생성
            log.info("새 장바구니 아이템 생성 - optionId: {}, imageId: {}, quantity: {}", 
                    request.getOptionId(), request.getImageId(), request.getQuantity());
            
            cartItem = CartItem.builder()
                    .cart(cart)
                    .optionId(request.getOptionId())
                    .imageId(request.getImageId())
                    .quantity(request.getQuantity())
                    .build();
        }

        // 9. DB에 저장
        cartItemRepository.save(cartItem);

        log.info("장바구니 상품 추가 완료 - cartItemId: {}, optionId: {}, quantity: {}", 
                cartItem.getCartItemId(), request.getOptionId(), request.getQuantity());

        // 10. 응답값 리턴
        return CartItemResponse.from(cartItem);
    }
}