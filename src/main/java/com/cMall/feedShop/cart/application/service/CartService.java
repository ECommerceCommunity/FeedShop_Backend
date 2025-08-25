package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.response.info.CartItemInfo;
import com.cMall.feedShop.cart.application.dto.request.CartItemCreateRequest;
import com.cMall.feedShop.cart.application.dto.request.CartItemUpdateRequest;
import com.cMall.feedShop.cart.application.dto.response.CartItemListResponse;
import com.cMall.feedShop.cart.application.dto.response.CartItemResponse;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.cart.domain.repository.CartRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductImageRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final UserRepository userRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final DiscountCalculator discountCalculator;

    /**
     * 장바구니에 상품을 추가하는 서비스 메서드
     *
     * @param request      장바구니 아이템 생성 요청
     * @param loginId  현재 로그인한 사용자 정보
     * @return CartItemResponse 장바구니 아이템 응답
     */
    public CartItemResponse addCartItem(CartItemCreateRequest request, String loginId) {
        log.info("장바구니 상품 추가 시작 - optionId: {}, quantity: {}", request.getOptionId(), request.getQuantity());
        
        // 1. 현재 사용자 조회
        User currentUser = getCurrentUser(loginId);

        // 2. 상품 옵션 검증
        ProductOption productOption = validateProductOption(request.getOptionId());

        // 3. 상품 이미지 검증
        validateProductImage(request.getImageId());

        // 4. 재고 확인
        validateStock(productOption, request.getQuantity());

        // 5. 장바구니 조회 또는 생성
        Cart cart = getOrCreateCart(currentUser);

        // 6. 해당 장바구니의 아이템에서 cart와 optionId와 imageId로 이미 저장된 같은 아이템이 있는지 조회
        Optional<CartItem> existingCartItem = cartItemRepository
                .findByCartAndOptionIdAndImageId(cart, request.getOptionId(), request.getImageId());

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            // 7. 기존 장바구니 아이템이 있으면 해당 아이템의 수량을 업데이트
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            // 증가된 수량으로 재고 재확인
            validateStock(productOption, newQuantity);

            // 수량 업데이트
            cartItem.updateQuantity(newQuantity);
        } else {
            // 8. 기존 장바구니 아이템이 없으면 새로운 장바구니 아이템 생성
            cartItem = CartItem.builder()
                    .cart(cart)
                    .optionId(request.getOptionId())
                    .imageId(request.getImageId())
                    .quantity(request.getQuantity())
                    .build();
        }

        // 9. DB에 저장
        cartItemRepository.save(cartItem);
        
        log.info("장바구니 상품 추가 완료 - cartItemId: {}, userId: {}, optionId: {}", 
                cartItem.getCartItemId(), currentUser.getId(), request.getOptionId());

        // 10. 응답값 리턴
        return CartItemResponse.from(cartItem);
    }

    /**
     * 장바구니에 있는 모든 아이템을 조회하는 서비스 메서드
     *
     * @param loginId 현재 로그인한 사용자 정보
     * @return CartItemListResponse 장바구니 아이템 리스트 응답
     */
    @Transactional(readOnly = true)
    public CartItemListResponse getCartItems(String loginId) {
        log.debug("장바구니 조회 시작");
        
        // 1. 현재 사용자 조회
        User currentUser = getCurrentUser(loginId);

        // 2. 사용자 ID로 장바구니 조회 (CartItem + Cart + User)
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithCart(currentUser.getId());

        if (cartItems.isEmpty()) {
            return CartItemListResponse.empty();
        }

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

        // 5. 장바구니 아이템 가격 계산 후 최종 리스트 응답 생성
        CartItemListResponse response = calculateCartSummary(items);
        log.debug("장바구니 조회 완료 - 아이템 수: {}, 총 금액: {}", items.size(), response.getTotalDiscountPrice());
        
        return response;
    }

    /**
     * 장바구니 아이템을 업데이트하는 서비스 메서드
     *
     * @param cartItemId
     * @param request
     * @param loginId
     */
    public void updateCartItem(Long cartItemId, CartItemUpdateRequest request, String loginId) {
        log.info("장바구니 아이템 업데이트 시작 - cartItemId: {}", cartItemId);
        
        // 1. 현재 사용자 조회
        User currentUser = getCurrentUser(loginId);

        // 2. 장바구니 아이템 조회
        CartItem cartItem = cartItemRepository.findByCartItemIdAndUserId(cartItemId, currentUser.getId())
                .orElseThrow(() -> new CartException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 3. 수량 변경 처리
        if (request.getQuantity() != null) {
            // 재고 확인
            ProductOption productOption = validateProductOption(cartItem.getOptionId());
            validateStock(productOption, request.getQuantity());

            // 수량 업데이트
            cartItem.updateQuantity(request.getQuantity());
        }

        // 4. 선택 상태 변경 처리
        if (request.getSelected() != null) {
            cartItem.updateSelected(request.getSelected());
        }

        // 5. DB에 저장 (트랜잭션 관리)
        cartItemRepository.save(cartItem);
        
        log.info("장바구니 아이템 업데이트 완료 - cartItemId: {}, userId: {}", cartItemId, currentUser.getId());
    }

    /**
     * 장바구니 아이템을 삭제하는 서비스 메서드
     *
     * @param cartItemId 장바구니 아이템 ID
     * @param loginId 현재 로그인한 사용자 정보
     */
    public void deleteCartItem(Long cartItemId, String loginId) {
        log.info("장바구니 아이템 삭제 시작 - cartItemId: {}", cartItemId);
        
        // 1. 현재 사용자 조회
        User currentUser = getCurrentUser(loginId);

        // 2. 장바구니 아이템 조회
        CartItem cartItem = cartItemRepository.findByCartItemIdAndUserId(cartItemId, currentUser.getId())
                .orElseThrow(() -> new CartException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 3. 장바구니 아이템 삭제
        cartItemRepository.delete(cartItem);
        
        log.info("장바구니 아이템 삭제 완료 - cartItemId: {}, userId: {}", cartItemId, currentUser.getId());
    }

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

    // JWT 에서 현재 사용자 ID 추출
    private User getCurrentUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }

    private ProductOption validateProductOption(Long optionId) {
        // ProductOption을 찾는다.
        ProductOption productOption = productOptionRepository.findByOptionId(optionId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

        return productOption;
    }

    private ProductImage validateProductImage(Long imageId) {
        // ProductImage를 찾는다.
        ProductImage productImage = productImageRepository.findByImageId(imageId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
        return productImage;
    }

    private void validateStock(ProductOption productOption, Integer quantity) {
        // 재고가 충분한지 확인한다.
        if (!productOption.isInStock() || productOption.getStock() < quantity) {
            log.warn("재고 부족 - optionId: {}, 요청수량: {}, 현재재고: {}", 
                    productOption.getOptionId(), quantity, productOption.getStock());
            throw new ProductException(ErrorCode.OUT_OF_STOCK);
        }
    }

    private Cart getOrCreateCart(User user) {
        return Optional.ofNullable(user.getCart())
                .orElseGet(() -> {
                    // user의 cart가 없으면 새로 생성
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();

                    // 새로 생성한 cart를 DB에 저장
                    Cart savedCart = cartRepository.save(newCart);
                    user.setCart(savedCart);

                    return savedCart;
                });
    }
}