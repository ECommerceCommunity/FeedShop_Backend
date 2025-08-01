package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.common.CartItemInfo;
import com.cMall.feedShop.cart.application.dto.request.CartItemCreateRequest;
import com.cMall.feedShop.cart.application.dto.request.CartItemUpdateRequest;
import com.cMall.feedShop.cart.application.dto.response.CartItemListResponse;
import com.cMall.feedShop.cart.application.dto.response.CartItemResponse;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemQueryRepository;
import com.cMall.feedShop.cart.domain.repository.CartItemQueryRepositoryImpl;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.cart.domain.repository.CartRepository;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.application.util.DiscountCalculator;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductImageRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * @param userDetails  현재 로그인한 사용자 정보
     * @return CartItemResponse 장바구니 아이템 응답
     */
    public CartItemResponse addCartItem(CartItemCreateRequest request, UserDetails userDetails) {
        // 1. 현재 사용자 조회
        User currentUser = getCurrentUser(userDetails);

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

        // 10. 응답값 리턴
        return CartItemResponse.from(cartItem);
    }

    /**
     * 장바구니에 있는 모든 아이템을 조회하는 서비스 메서드
     *
     * @param userDetails 현재 로그인한 사용자 정보
     * @return CartItemListResponse 장바구니 아이템 리스트 응답
     */
    @Transactional(readOnly = true)
    public CartItemListResponse getCartItems(UserDetails userDetails) {
        // 1. 현재 사용자 조회
        User currentUser = getCurrentUser(userDetails);

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
                        throw new ProductException.ProductOptionNotFoundException();
                    }

                    if (image == null) {
                        throw new ProductException.ProductImageNotFoundException();
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
        return calculateCartSummary(items);
    }

    /**
     * 장바구니 아이템을 업데이트하는 서비스 메서드
     *
     * @param cartItemId
     * @param request
     * @param userDetails
     */
    public void updateCartItem(Long cartItemId, CartItemUpdateRequest request, UserDetails userDetails) {
        // 1. 현재 사용자 조회
        User currentUser = getCurrentUser(userDetails);

        // 2. 장바구니 아이템 조회
        CartItem cartItem = cartItemRepository.findByCartItemIdAndUserId(cartItemId, currentUser.getId())
                .orElseThrow(() -> new CartException.CartItemNotFoundException());

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
    }

    /**
     * 장바구니 아이템을 삭제하는 서비스 메서드
     *
     * @param cartItemId 장바구니 아이템 ID
     * @param userDetails 현재 로그인한 사용자 정보
     */
    public void deleteCartItem(Long cartItemId, UserDetails userDetails) {
        // 1. 현재 사용자 조회
        User currentUser = getCurrentUser(userDetails);

        // 2. 장바구니 아이템 조회
        CartItem cartItem = cartItemRepository.findByCartItemIdAndUserId(cartItemId, currentUser.getId())
                .orElseThrow(() -> new CartException.CartItemNotFoundException());

        // 3. 장바구니 아이템 삭제
        cartItemRepository.delete(cartItem);
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
    private User getCurrentUser(UserDetails userDetails) {
        String login_id = userDetails.getUsername();
        return userRepository.findByLoginId(login_id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private ProductOption validateProductOption(Long optionId) {
        // ProductOption을 찾는다.
        ProductOption productOption = productOptionRepository.findByOptionId(optionId)
                .orElseThrow(() -> new ProductException.ProductOptionNotFoundException());

        return productOption;
    }

    private ProductImage validateProductImage(Long imageId) {
        // ProductImage를 찾는다.
        ProductImage productImage = productImageRepository.findByImageId(imageId)
                .orElseThrow(() -> new ProductException.ProductImageNotFoundException());
        return productImage;
    }

    private void validateStock(ProductOption productOption, Integer quantity) {
        // 재고가 충분한지 확인한다.
        if (!productOption.isInStock() || productOption.getStock() < quantity) {
            throw new ProductException.OutOfStockException();
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
