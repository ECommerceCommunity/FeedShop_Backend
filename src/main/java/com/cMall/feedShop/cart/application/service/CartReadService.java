package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.response.CartItemListResponse;
import com.cMall.feedShop.cart.application.dto.response.info.CartItemInfo;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductImageRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 장바구니 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartReadService {
    
    private final CartItemRepository cartItemRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final DiscountCalculator discountCalculator;
    private final CartHelper cartHelper;

    /**
     * 장바구니에 있는 모든 아이템을 조회하는 서비스 메서드
     *
     * @param loginId 현재 로그인한 사용자 정보
     * @return CartItemListResponse 장바구니 아이템 리스트 응답
     */
    public CartItemListResponse getCartItems(String loginId) {
        log.info("장바구니 목록 조회 시작");
        
        // 1. 현재 사용자 조회
        User currentUser = cartHelper.getCurrentUser(loginId);

        // 2. 사용자 ID로 장바구니 조회 (CartItem + Cart + User)
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithCart(currentUser.getId());

        if (cartItems.isEmpty()) {
            log.info("장바구니가 비어있음");
            return CartItemListResponse.empty();
        }

        log.info("장바구니 아이템 조회 완료 - 아이템 개수: {}", cartItems.size());

        // 3. 상품 옵션 추출 (ProductOption + Product + Store + Category)
        Set<Long> optionIds = cartItems.stream()
                .map(CartItem::getOptionId)
                .collect(Collectors.toSet());

        Map<Long, ProductOption> optionMap = productOptionRepository
                .findAllByOptionIdIn(optionIds).stream()
                .collect(Collectors.toMap(ProductOption::getOptionId, Function.identity()));

        // 4. 상품 이미지 추출 (ProductImage)
        Set<Long> imageIds = cartItems.stream()
                .map(CartItem::getImageId)
                .collect(Collectors.toSet());

        Map<Long, ProductImage> imageMap = productImageRepository
                .findAllById(imageIds).stream()
                .collect(Collectors.toMap(ProductImage::getImageId, Function.identity()));

        // 5. DTO 변환
        List<CartItemInfo> items = cartItems.stream()
                .map(cartItem -> {
                    ProductOption option = optionMap.get(cartItem.getOptionId());
                    ProductImage image = imageMap.get(cartItem.getImageId());

                    if (option == null) {
                        throw new ProductException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
                    }

                    if (image == null) {
                        throw new ProductException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
                    }

                    Product product = option.getProduct();

                    BigDecimal discountPrice = discountCalculator
                            .calculateDiscountPrice(
                                    product.getPrice(),
                                    product.getDiscountType(),
                                    product.getDiscountValue());

                    return CartItemInfo.from(
                            cartItem,
                            product,
                            option,
                            image,
                            discountPrice);
                })
                .toList();

        // 6. 장바구니 아이템 가격 계산 후 최종 리스트 응답 생성
        CartItemListResponse response = calculateCartSummary(items);
        
        log.info("장바구니 목록 조회 완료 - 총 상품 개수: {}, 총 금액: {}", 
                items.size(), response.getTotalDiscountPrice());
        
        return response;
    }

    /**
     * 장바구니 요약 정보 계산
     */
    private CartItemListResponse calculateCartSummary(List<CartItemInfo> items) {
        // 선택된 아이템들만 계산한다. (실제 결제 대상)
        List<CartItemInfo> selectedItems = items.stream()
                .filter(CartItemInfo::getSelected)
                .toList();

        // 상품 가격을 수량과 곱해서 전체 가격을 구한다.
        BigDecimal totalOriginalPrice = selectedItems.stream()
                .map(item -> item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 할인 가격을 수량과 곱해서 전체 할인 가격을 구한다.
        BigDecimal totalDiscountPrice = selectedItems.stream()
                .map(item -> item.getDiscountPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 전체 가격 - 할인 가격을 해서 총 절약 금액을 계산한다.
        BigDecimal totalSavings = totalOriginalPrice.subtract(totalDiscountPrice);

        // CartItemListResponse 객체를 생성하여 반환한다.
        return CartItemListResponse.of(items, totalOriginalPrice, totalDiscountPrice, totalSavings);
    }
}