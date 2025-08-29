package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.repository.CartRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductImageRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 장바구니 관련 공통 매핑 및 유틸리티 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartHelper {
    
    private final UserRepository userRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final CartRepository cartRepository;

    /**
     * JWT에서 현재 사용자 ID 추출
     */
    public User getCurrentUser(String loginId) {
        log.debug("사용자 조회 시작 - userId: {}", loginId);
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        log.debug("사용자 조회 완료 - userId: {}", loginId);
        return user;
    }

    /**
     * 상품 옵션 검증 및 조회
     */
    public ProductOption validateProductOption(Long optionId) {
        log.debug("상품 옵션 검증 시작 - optionId: {}", optionId);
        ProductOption productOption = productOptionRepository.findByOptionId(optionId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));
        log.debug("상품 옵션 검증 완료 - optionId: {}", optionId);
        return productOption;
    }

    /**
     * 상품 이미지 검증 및 조회
     */
    public ProductImage validateProductImage(Long imageId) {
        log.debug("상품 이미지 검증 시작 - imageId: {}", imageId);
        ProductImage productImage = productImageRepository.findByImageId(imageId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
        log.debug("상품 이미지 검증 완료 - imageId: {}", imageId);
        return productImage;
    }

    /**
     * 재고 확인
     */
    public void validateStock(ProductOption productOption, Integer quantity) {
        log.debug("재고 검증 시작 - optionId: {}, quantity: {}, stock: {}", 
                 productOption.getOptionId(), quantity, productOption.getStock());
        
        if (!productOption.isInStock() || productOption.getStock() < quantity) {
            log.warn("재고 부족 - optionId: {}, 요청 수량: {}, 현재 재고: {}", 
                    productOption.getOptionId(), quantity, productOption.getStock());
            throw new ProductException(ErrorCode.OUT_OF_STOCK);
        }
        
        log.debug("재고 검증 완료 - optionId: {}, quantity: {}", productOption.getOptionId(), quantity);
    }

    /**
     * 장바구니 조회 또는 생성
     */
    public Cart getOrCreateCart(User user) {
        log.debug("장바구니 조회 또는 생성 시작 - userId: {}", user.getId());
        
        Cart cart = Optional.ofNullable(user.getCart())
                .orElseGet(() -> {
                    log.debug("새 장바구니 생성 - userId: {}", user.getId());
                    
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();

                    Cart savedCart = cartRepository.save(newCart);
                    user.setCart(savedCart);
                    
                    log.debug("새 장바구니 생성 완료 - userId: {}, cartId: {}", user.getId(), savedCart.getCartId());
                    return savedCart;
                });
        
        log.debug("장바구니 조회 또는 생성 완료 - userId: {}, cartId: {}", user.getId(), cart.getCartId());
        return cart;
    }
}