package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import com.cMall.feedShop.order.application.dto.request.OrderItemRequest;
import com.cMall.feedShop.order.application.dto.request.OrderStatusUpdateRequest;
import com.cMall.feedShop.order.application.dto.response.*;
import com.cMall.feedShop.order.application.calculator.OrderCalculation;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductImageRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductOptionRepository productOptionRepository;
    private final DiscountCalculator discountCalculator;
    private final OrderRepository orderRepository;
    private final OrderCommonService orderCommonService;
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
        User currentUser = orderCommonService.validateUser(userDetails);

        // 2. 선택된 장바구니 아이템 조회 (selected = true 인 아이템들만)
        List<CartItem> selectedCartItems = getSelectedCartItems(currentUser.getId());
        validateCartItems(selectedCartItems);

        // 3. 상품 정보 조회 및 검증
        Map<Long, ProductOption> optionMap = validateCartOrderProductOptions(selectedCartItems);
        Map<Long, ProductImage> imageMap = getCartProductImages(selectedCartItems);

        // 4. 주문 금액 계산
        OrderCalculation calculation = calculateCartOrderAmount(selectedCartItems, optionMap, request.getUsedPoints());

        // 5. 포인트 사용 가능 여부 확인
        orderCommonService.validatePointUsage(currentUser, calculation.getActualUsedPoints());

        // 6. 주문 및 주문 아이템 생성
        Order order = createAndSaveCartOrder(currentUser, request, calculation, selectedCartItems, optionMap, imageMap);

        // 7. 재고 차감
        processPostCartOrder(currentUser, selectedCartItems, optionMap, calculation);

        // 8. 주문 생성 응답 반환
        return OrderCreateResponse.from(order);
    }

    /**
     * 주문 목록 조회 (사용자)
     * @param page
     * @param size
     * @param status
     * @param userDetails
     * @return
     */
    @Transactional(readOnly = true)
    public OrderPageResponse getOrderListForUser(int page, int size, String status, UserDetails userDetails) {
        // 1. 현재 사용자 조회를 하고 사용자 권한을 검증
        User currentUser = orderCommonService.validateUser(userDetails);

        // 2. 페이지 파라미터 검증
        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 100) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        // 3. 주문 목록 조회
        Page<Order> orderPage = getOrderPageForUser(currentUser, status, pageable);

        // 4. 주문 목록 응답 반환
        Page<OrderListResponse> response = orderPage.map(OrderListResponse::from);
        return OrderPageResponse.of(response);
    }

    /**
     * 주문 목록 조회 (판매자)
     * @param page
     * @param size
     * @param status
     * @param userDetails
     * @return
     */
    @Transactional(readOnly = true)
    public OrderPageResponse getOrderListForSeller(int page, int size, String status, UserDetails userDetails) {
        // 1. 현재 사용자 조회를 하고 판매자 권한을 검증
        User currentUser = validateSeller(userDetails);

        // 2. 페이지 파라미터 검증
        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 100) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        // 3. 주문 목록 조회
        Page<Order> orderPage = getOrderPageForSeller(currentUser, status, pageable);

        // 4. 주문 목록 응답 반환
        Page<OrderListResponse> response = orderPage.map(OrderListResponse::from);
        return OrderPageResponse.of(response);
    }

    // 주문 페이지 조회 (사용자)
    private Page<Order> getOrderPageForUser(User currentUser, String status, Pageable pageable) {
        if (status != null && !status.equalsIgnoreCase("all")) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                return orderRepository.findByUserAndStatusOrderByCreatedAtDesc(currentUser, orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new OrderException(ErrorCode.INVALID_ORDER_STATUS);
            }
        } else {
            // 전체 조회
            return orderRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);
        }
    }

    // 주문 페이지 조회 (판매자)
    private Page<Order> getOrderPageForSeller(User currentUser, String status, Pageable pageable) {
        if (status != null && !status.equalsIgnoreCase("all")) {
            try {
                // 특정 주문 상태 필터링 조회
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                return orderRepository.findOrdersBySellerIdAndStatus(currentUser.getId(), orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new OrderException(ErrorCode.INVALID_ORDER_STATUS);
            }
        } else {
            // 전체 조회
            return orderRepository.findOrdersBySellerId(currentUser.getId(), pageable);
        }
    }

    /**
     * 주문 상세 조회
     * @param orderId
     * @param userDetails
     * @return
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long orderId, UserDetails userDetails) {
        // 1. 현재 사용자 조회 및 권한 검증
        User currentUser = orderCommonService.validateUser(userDetails);

        // 2. 주문 조회 및 권한 검증
        Order order = orderRepository.findByOrderIdAndUser(orderId, currentUser)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));

        // 3. 주문 상세 조회 응답 반환
        return OrderDetailResponse.from(order);
    }

    /**
     * 판매자 주문 상태 변경
     * @param orderId
     * @param request
     * @param userDetails
     * @return
     */
    @Transactional
    public OrderStatusUpdateResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, UserDetails userDetails) {
        // 1. 판매자 권한 검증
        User seller = validateSeller(userDetails);

        // 2. 주문 조회 및 권한 검증
        Order order = orderRepository.findByOrderIdAndSellerId(orderId, seller.getId())
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));

        // 3. 상태 변경 가능 여부 검증
        validateStatusUpdate(order.getStatus(), request.getStatus());

        // 4. 주문 상태 업데이트
        order.updateStatus(request.getStatus());

        // 5. 주문 상태 변경 응답 반환
        return OrderStatusUpdateResponse.from(order);
    }

    /**
     * 주문 상태 변경 가능 여부 검증
     * - 현재 상태와 변경할 상태를 비교하여 유효성 검증
     * @param currentStatus 현재 주문 상태
     * @param newStatus 변경할 주문 상태
     */
    private void validateStatusUpdate(OrderStatus currentStatus, OrderStatus newStatus) {
        // 주문됨 -> 배송중, 취소로 변경 가능
        if (currentStatus == OrderStatus.ORDERED) {
            if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.CANCELLED) {
                throw new OrderException(ErrorCode.INVALID_ORDER_STATUS);
            }
        }
        // 배송중 -> 배송완료로 변경 가능
        else if (currentStatus == OrderStatus.SHIPPED) {
            if (newStatus != OrderStatus.DELIVERED) {
                throw new OrderException(ErrorCode.INVALID_ORDER_STATUS);
            }
        }
        // 배송완료, 취소됨, 반품됨 상태는 변경 불가
        else {
            throw new OrderException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }

    /**
     * 현재 사용자 조회 및 판매자 권한 검증
     * @param userDetails 현재 로그인된 사용자 정보
     * @return 검증된 사용자 정보
     */
    private User validateSeller(UserDetails userDetails) {
        String loginId = userDetails.getUsername();
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.SELLER) {
            throw new OrderException(ErrorCode.ORDER_FORBIDDEN);
        }

        return user;
    }

    /**
     * 선택된 장바구니 아이템 조회
     * - 현재 사용자의 장바구니에서 선택된 아이템들만 조회
     * @param userId 현재 사용자 ID
     * @return 선택된 장바구니 아이템 리스트
     */
    private List<CartItem> getSelectedCartItems(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithCart(userId);
        return cartItems.stream()
                .filter(CartItem::getSelected)
                .toList();
    }

    /**
     * 장바구니 아이템이 비어있는지 검증
     * - 선택된 장바구니 아이템이 없으면 예외 처리
     * @param selectedCartItems 선택된 장바구니 아이템 리스트
     */
    private void validateCartItems(List<CartItem> selectedCartItems) {
        if (selectedCartItems.isEmpty()) {
            throw new OrderException(ErrorCode.ORDER_CART_EMPTY);
        }
    }

    /**
     * 상품 이미지 조회
     * - 장바구니 아이템에서 이미지 ID를 추출하여 중복 제거 후 이미지 정보를 조회
     * @param cartItems 선택된 장바구니 아이템 리스트
     * @return 이미지 ID와 이미지 객체의 맵
     */
    private Map<Long, ProductImage> getCartProductImages(List<CartItem> cartItems) {
        // 장바구니 아이템에서 이미지 ID를 추출하여 중복 제거
        Set<Long> imageIds = cartItems.stream()
                .map(CartItem::getImageId)
                .collect(Collectors.toSet());

        return productImageRepository.findAllById(imageIds).stream()
                .collect(Collectors.toMap(ProductImage::getImageId, Function.identity()));
    }

    // 상품 옵션 검증
    private Map<Long, ProductOption> validateCartOrderProductOptions(List<CartItem> cartItems) {
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
        validateCartOrderStock(cartItems, optionMap);

        return optionMap;
    }

    // 재고를 확인한다.
    private void validateCartOrderStock(List<CartItem> cartItems, Map<Long, ProductOption> optionMap) {
        // 각 주문 아이템마다 재고를 확인한다.
        for (CartItem item : cartItems) {
            // 주문 아이템에 해당하는 상품 옵션을 가져온다.
            ProductOption option = optionMap.get(item.getOptionId());
            // 재고가 없거나 주문 수량보다 적은 경우
            if (!option.isInStock() || option.getStock() < item.getQuantity()) {
                throw new ProductException(ErrorCode.OUT_OF_STOCK,
                        String.format("상품 '%s'의 재고가 부족합니다. 현재 재고: %d, 요청 수량: %d",
                                option.getProduct().getName(), option.getStock(), item.getQuantity()));
            }
        }
    }

    // 주문 금액 계산
    // 포인트 주문 금액의 최대 10%까지만 사용 가능
    // 포인트 차감 (100 포인트 = 100 원)
    // 적립 포인트 (총 구매금액 1만원 당 50점)
    private OrderCalculation calculateCartOrderAmount(List<CartItem> cartItems, Map<Long, ProductOption> optionMap, Integer usedPoints) {
        // 총 상품 금액 계산
        BigDecimal totalAmount = calculateCartTotalAmount(cartItems, optionMap);

        // 실제 사용 가능한 포인트 계산
        Integer actualUsedPoints = orderCommonService.calculateActualUsedPoints(totalAmount, usedPoints);

        // 포인트 차감 후 최종 금액 계산
        BigDecimal finalAmount = orderCommonService.calculateFinalAmount(totalAmount, actualUsedPoints);

        // 최종 금액을 기준으로 적립 포인트를 계산한다. (총 구매금액 1만원 당 50점)
        Integer earnedPoints = orderCommonService.calculateEarnedPoints(finalAmount);

        // OrderCalculation 객체를 생성하여 반환한다.
        return OrderCalculation.builder()
                .totalAmount(totalAmount)
                .finalAmount(finalAmount)
                .actualUsedPoints(actualUsedPoints)
                .earnedPoints(earnedPoints)
                .build();
    }

    // 총 금액을 계산한다.
    private BigDecimal calculateCartTotalAmount(List<CartItem> cartItems, Map<Long, ProductOption> optionMap) {
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

    // 주문을 생성하고 저장한다.
    private Order createAndSaveCartOrder(User user, OrderCreateRequest request, OrderCalculation calculation,
                                         List<CartItem> cartItems, Map<Long, ProductOption> optionMap, Map<Long, ProductImage> imageMap) {
        // 주문 Entity 생성
        Order order = createCartOrderEntity(user, request, calculation);

        // 주문 아이템 생성
        createCartOrderItems(order, cartItems, optionMap, imageMap);

        // 주문 DB 저장
        return orderRepository.save(order);
    }

    // 주문 Entity 생성
    private Order createCartOrderEntity(User user, OrderCreateRequest request, OrderCalculation calculation) {
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
    private void createCartOrderItems(Order order, List<CartItem> cartItems, Map<Long, ProductOption> optionMap, Map<Long, ProductImage> imageMap) {
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

    /**
     * 주문 후 처리
     * - 재고 차감, 포인트 처리, 장바구니 아이템 삭제
     * @param user 주문을 요청한 사용자 정보
     * @param cartItems 선택된 장바구니 아이템 리스트
     * @param optionMap 상품 옵션 맵
     * @param calculation 주문 금액 계산 정보
     */
    private void processPostCartOrder(User user, List<CartItem> cartItems, Map<Long, ProductOption> optionMap, OrderCalculation calculation) {
        // 재고 차감
        decreaseCartStock(cartItems, optionMap);

        // 포인트 처리
        orderCommonService.processUserPoints(user, calculation.getActualUsedPoints(), calculation.getEarnedPoints());

        // 장바구니 아이템 삭제
        cartItemRepository.deleteAll(cartItems);
    }

    // 재고 차감
    private void decreaseCartStock(List<CartItem> cartItems, Map<Long, ProductOption> optionMap) {
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
}
