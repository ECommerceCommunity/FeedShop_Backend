package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
import com.cMall.feedShop.order.application.dto.response.OrderListResponse;
import com.cMall.feedShop.order.application.dto.response.OrderPageResponse;
import com.cMall.feedShop.order.application.util.OrderCalculation;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.product.application.util.DiscountCalculator;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductImageRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserPoint;
import com.cMall.feedShop.user.domain.repository.UserPointRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    // 구매 금액의 {POINT_USAGE_RATE}% 까지만 포인트 사용 가능
    private static final BigDecimal POINT_USAGE_RATE = BigDecimal.valueOf(0.1);

    // {POINT_REWARD_THRESHOLD}원 단위로 {POINT_REWARD_AMOUNT} 포인트를 적립
    private static final BigDecimal POINT_REWARD_THRESHOLD = BigDecimal.valueOf(10000);
    private static final BigDecimal POINT_REWARD_AMOUNT = BigDecimal.valueOf(50);

    // 포인트 사용 단위
    private static final int POINT_UNIT = 100;

    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductOptionRepository productOptionRepository;
    private final DiscountCalculator discountCalculator;
    private final OrderRepository orderRepository;
    private final UserPointRepository userPointRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * 주문 생성
     * @param request 주문 생성 요청 정보
     * @param userDetails 현재 로그인된 사용자 정보
     * @return 주문 생성 응답 정보
     */
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, UserDetails userDetails) {
        // 1. 현재 사용자 조회를 하고 사용자 권한을 검증
        User currentUser = getCurrentUserAndValidatePermission(userDetails);

        // 2. 선택된 장바구니 아이템 조회 (selected = true 인 아이템들만)
        List<CartItem> selectedCartItems = getSelectedCartItems(currentUser.getId());
        validateCartItems(selectedCartItems);

        // 3. 상품 정보 조회 및 검증
        Map<Long, ProductOption> optionMap = getAndValidateProductOptions(selectedCartItems);
        Map<Long, ProductImage> imageMap = getProductImages(selectedCartItems);

        // 4. 주문 금액 계산
        OrderCalculation calculation = calculateOrderAmount(selectedCartItems, optionMap, request.getUsedPoints());

        // 5. 포인트 사용 가능 여부 확인
        validatePointUsage(currentUser, calculation.getActualUsedPoints());

        // 6. 주문 및 주문 아이템 생성
        Order order = createAndSaveOrder(currentUser, request, calculation, selectedCartItems, optionMap, imageMap);

        // 7. 재고 차감
        processPostOrder(currentUser, selectedCartItems, optionMap, calculation);

        // 8. 주문 생성 응답 반환
        return OrderCreateResponse.from(order);
    }

    /**
     * 주문 목록 조회
     * @param page
     * @param size
     * @param status
     * @param userDetails
     * @return
     */
    @Transactional(readOnly = true)
    public OrderPageResponse getOrderList(int page, int size, String status, UserDetails userDetails) {
        // 1. 현재 사용자 조회를 하고 사용자 권한을 검증
        User currentUser = getCurrentUserAndValidatePermission(userDetails);

        // 2. 페이지 파라미터 검증
        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 100) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        // 3. 주문 목록 조회
        Page<Order> orderPage;
        if (status != null && !status.equalsIgnoreCase("all")) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orderPage = orderRepository.findByUserAndStatusOrderByCreatedAtDesc(currentUser, orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new OrderException(ErrorCode.INVALID_ORDER_STATUS);
            }
        } else {
            orderPage = orderRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);
        }

        // 4. 주문 목록 응답 반환
        Page<OrderListResponse> response = orderPage.map(OrderListResponse::from);
        return OrderPageResponse.of(response);
    }

    // JWT 에서 현재 사용자 정보 조회
    // 사용자 권한 검증
    private User getCurrentUserAndValidatePermission(UserDetails userDetails) {
        User user = userRepository.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.USER) {
            throw new OrderException(ErrorCode.ORDER_FORBIDDEN);
        }

        return user;
    }

    // 선택된 장바구니 아이템 조회 (selected = true 인 아이템들만)
    private List<CartItem> getSelectedCartItems(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithCart(userId);
        return cartItems.stream()
                .filter(CartItem::getSelected)
                .toList();
    }

    private void validateCartItems(List<CartItem> selectedCartItems) {
        if (selectedCartItems.isEmpty()) {
            throw new OrderException(ErrorCode.ORDER_CART_EMPTY);
        }
    }

    private Map<Long, ProductImage> getProductImages(List<CartItem> cartItems) {
        // 장바구니 아이템에서 이미지 ID를 추출하여 중복 제거
        Set<Long> imageIds = cartItems.stream()
                .map(CartItem::getImageId)
                .collect(Collectors.toSet());

        return productImageRepository.findAllById(imageIds).stream()
                .collect(Collectors.toMap(ProductImage::getImageId, Function.identity()));
    }

    // 상품 옵션 조회 및 재고 확인
    private Map<Long, ProductOption> getAndValidateProductOptions(List<CartItem> cartItems) {
        // 장바구니 아이템에서 옵션 ID를 추출하여 중복 제거
        Set<Long> optionIds = cartItems.stream()
                .map(CartItem::getOptionId)
                .collect(Collectors.toSet());

        // 옵션 ID가 비어있으면 예외 처리
        List<ProductOption> options = productOptionRepository.findAllByOptionIdIn(optionIds);
        if (options.size() != optionIds.size()) {
            throw new ProductException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }

        Map<Long, ProductOption> optionMap = options.stream()
                .collect(Collectors.toMap(ProductOption::getOptionId, Function.identity()));

        // 재고 검증
        validateStock(cartItems, optionMap);

        return optionMap;
    }

    // 재고를 확인한다.
    private void validateStock(List<CartItem> cartItems, Map<Long, ProductOption> optionMap) {
        for (CartItem cartItem : cartItems) {
            ProductOption option = optionMap.get(cartItem.getOptionId());
            if (!option.isInStock() || option.getStock() < cartItem.getQuantity()) {
                throw new ProductException(ErrorCode.OUT_OF_STOCK);
            }
        }
    }

    // 주문 금액 계산
    // 포인트 주문 금액의 최대 10%까지만 사용 가능
    // 포인트 차감 (100 포인트 = 100 원)
    // 적립 포인트 (총 구매금액 1만원 당 50점)
    private OrderCalculation calculateOrderAmount(List<CartItem> cartItems, Map<Long, ProductOption> optionMap, Integer usedPoints) {
        // 총 상품 금액 계산
        BigDecimal totalAmount = calculateTotalAmount(cartItems, optionMap);

        // 실제 사용 가능한 포인트 계산
        Integer actualUsedPoints = calculateActualUsedPoints(totalAmount, usedPoints);

        // 포인트 차감 후 최종 금액 계산
        BigDecimal finalAmount = calculateFinalAmount(totalAmount, actualUsedPoints);

        // 최종 금액을 기준으로 적립 포인트를 계산한다. (총 구매금액 1만원 당 50점)
        Integer earnedPoints = calculateEarnedPoints(finalAmount);

        // OrderCalculation 객체를 생성하여 반환한다.
        return OrderCalculation.builder()
                .totalAmount(totalAmount)
                .finalAmount(finalAmount)
                .actualUsedPoints(actualUsedPoints)
                .earnedPoints(earnedPoints)
                .build();
    }

    private BigDecimal calculateTotalAmount(List<CartItem> cartItems, Map<Long, ProductOption> optionMap) {
        return cartItems.stream()
                .map(cartItem -> {
                    // 장바구니 아이템에서 옵션 ID로 상품을 조회한다.
                    ProductOption option = optionMap.get(cartItem.getOptionId());
                    Product product = option.getProduct();

                    // 상품의 할인된 가격을 계산해서 알아낸다.
                    BigDecimal discountPrice = discountCalculator.calculateDiscountPrice(
                            product.getPrice(),
                            product.getDiscountType(),
                            product.getDiscountValue()
                    );

                    // 할인된 가격에 장바구니 아이템의 수량을 곱한다.
                    return discountPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                })
                // 수량이 곱해진 할인된 가격을 더한다.
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 사용 가능한 포인트 계산
    private Integer calculateActualUsedPoints(BigDecimal totalAmount, Integer requestedPoints) {
        if (requestedPoints == null || requestedPoints <= 0) {
            return 0;
        }

        BigDecimal maxPointUsage = totalAmount.multiply(POINT_USAGE_RATE)
                .setScale(0, BigDecimal.ROUND_DOWN);

        BigDecimal requestedPointAmount = BigDecimal.valueOf(requestedPoints);

        return requestedPointAmount.compareTo(maxPointUsage) <= 0
                ? requestedPoints
                : maxPointUsage.intValue();
    }

    // 포인트 차감 후 최종 금액 계산
    private BigDecimal calculateFinalAmount(BigDecimal totalAmount, Integer usedPoints) {
        BigDecimal pointDeduction = BigDecimal.valueOf(usedPoints);
        BigDecimal finalAmount = totalAmount.subtract(pointDeduction);

        return finalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmount;
    }

    // 구매 후 얻을 포인트를 계산한다.
    private Integer calculateEarnedPoints(BigDecimal finalAmount) {
        if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal units = finalAmount.divide(POINT_REWARD_THRESHOLD, 0, RoundingMode.DOWN);

        // 10,000원 단위로 50 포인트를 적립한다.
        return units.multiply(POINT_REWARD_AMOUNT).intValue();
    }

    // 포인트가 유효한지 검증한다.
    private void validatePointUsage(User user, Integer usedPoints) {
        // 사용할 포인트 검증
        if (usedPoints == null || usedPoints == 0) {
            return;
        }

        // 유효값 및 100 포인트 단위 검증 (100 포인트 단위여야 한다)
        if (usedPoints < 0 || usedPoints % 100 != 0) {
            throw new OrderException(ErrorCode.INVALID_POINT);
        }

        // 사용자 포인트 검증
        UserPoint userPoint = getUserPoint(user);
        if (!userPoint.canUsePoints(usedPoints)) {
            throw new OrderException(ErrorCode.OUT_OF_POINT);
        }
    }

    private UserPoint getUserPoint(User user) {
        return userPointRepository.findByUser(user)
                .orElse(UserPoint.builder()
                        .user(user)
                        .currentPoints(0)
                        .build());
    }

    private Order createAndSaveOrder(User user, OrderCreateRequest request, OrderCalculation calculation,
                                     List<CartItem> cartItems, Map<Long, ProductOption> optionMap, Map<Long, ProductImage> imageMap) {
        // 주문 Entity 생성
        Order order = createOrderEntity(user, request, calculation);

        // 주문 아이템 생성
        createOrderItems(order, cartItems, optionMap, imageMap);

        // 주문 DB 저장
        return orderRepository.save(order);
    }

    // 주문 Entity 생성
    private Order createOrderEntity(User user, OrderCreateRequest request, OrderCalculation calculation) {
        BigDecimal finalPrice = calculation.getFinalAmount().add(request.getDeliveryFee());

        return Order.builder()
                .user(user)
                .status(OrderStatus.ORDERED)
                .totalPrice(calculation.getTotalAmount())
                .finalPrice(finalPrice)
                .deliveryFee(request.getDeliveryFee())
                .usedPoints(calculation.getActualUsedPoints())
                .earnedPoints(calculation.getEarnedPoints())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryDetailAddress(request.getDeliveryDetailAddress())
                .postalCode(request.getPostalCode())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .deliveryMessage(request.getDeliveryMessage())
                .paymentMethod(request.getPaymentMethod())
                .cardNumber(request.getCardNumber())
                .cardExpiry(request.getCardExpiry())
                .cardCvc(request.getCardCvc())
                .build();
    }

    // 장바구니 아이템을 주문의 주문 아이템 Entity로 만든다.
    private void createOrderItems(Order order, List<CartItem> cartItems, Map<Long, ProductOption> optionMap, Map<Long, ProductImage> imageMap) {
        for (CartItem cartItem : cartItems) {
            ProductOption option = optionMap.get(cartItem.getOptionId());
            ProductImage image = imageMap.get(cartItem.getImageId());
            Product product = option.getProduct();

            // totalPrice : 상품의 원래 가격
            BigDecimal totalPrice = product.getPrice();
            // finalPrice : 상품의 할인된 최종 가격
            BigDecimal finalPrice = discountCalculator.calculateDiscountPrice(
                    totalPrice,
                    product.getDiscountType(),
                    product.getDiscountValue()
            );

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productOption(option)
                    .productImage(image)
                    .quantity(cartItem.getQuantity())
                    .totalPrice(totalPrice)
                    .finalPrice(finalPrice)
                    .build();

            order.addOrderItem(orderItem);
        }
    }

    private void processPostOrder(User user, List<CartItem> cartItems, Map<Long, ProductOption> optionMap, OrderCalculation calculation) {
        // 재고 차감
        decreaseStock(cartItems, optionMap);

        // 포인트 처리
        processUserPoints(user, calculation.getActualUsedPoints(), calculation.getEarnedPoints());

        // 장바구니 아이템 삭제
        cartItemRepository.deleteAll(cartItems);
    }

    // 재고 차감
    private void decreaseStock(List<CartItem> cartItems, Map<Long, ProductOption> optionMap) {
        // 모든 장바구니 아이템들의 option을 조회한다.
        // option의 stock 에서 장바구니 아이템의 quantity를 뺀다.
        List<ProductOption> optionsToUpdate = cartItems.stream()
                .map(cartItem -> {
                    ProductOption option = optionMap.get(cartItem.getOptionId());
                    option.decreaseStock(cartItem.getQuantity());
                    return option;
                }).toList();

        productOptionRepository.saveAll(optionsToUpdate);
    }

    // UserPoint에 적립 및 사용
    private void processUserPoints(User user, Integer usedPoints, Integer earnedPoints) {
        // UserPoint를 조회한다.
        UserPoint userPoint = getUserPoint(user);

        // 포인트 사용
        if (usedPoints != null && usedPoints > 0) {
            userPoint.usePoints(usedPoints);
        }

        // 포인트 적립
        if (earnedPoints != null && earnedPoints > 0) {
            userPoint.earnPoints(earnedPoints);
        }

        // DB에 저장
        userPointRepository.save(userPoint);
    }
}
