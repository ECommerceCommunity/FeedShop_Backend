package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.dto.OrderItemData;
import com.cMall.feedShop.order.application.dto.OrderRequestData;
import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import com.cMall.feedShop.order.application.dto.request.OrderStatusUpdateRequest;
import com.cMall.feedShop.order.application.dto.response.*;
import com.cMall.feedShop.order.application.calculator.OrderCalculation;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.cMall.feedShop.order.application.constants.OrderConstants.MAX_ORDER_QUANTITY;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderCommonService orderCommonService;

    /**
     * 주문 생성
     * @param request 주문 생성 요청 정보
     * @param loginId 현재 로그인된 사용자 정보
     * @return 주문 생성 응답 정보
     */
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, String loginId) {
        // 1. 현재 사용자 조회를 하고 사용자 권한을 검증
        User currentUser = orderCommonService.validateUser(loginId);

        // 2. 선택된 장바구니 아이템 조회 (selected = true 인 아이템들만)
        List<CartItem> selectedCartItems = getSelectedCartItems(currentUser.getId());
        validateCartItems(selectedCartItems);

        // 3. 어댑터로 변환해서 OrderCommonService 사용
        List<OrderItemData> adapters = OrderItemData.fromCartItems(selectedCartItems);
        Map<Long, ProductOption> optionMap = orderCommonService.getValidProductOptions(adapters);
        Map<Long, ProductImage> imageMap = orderCommonService.getProductImages(adapters);

        // 4. 주문 금액 계산
        OrderCalculation calculation = orderCommonService.calculateOrderAmount(adapters, optionMap, request.getUsedPoints());

        // 5. 포인트 사용 가능 여부 확인
        orderCommonService.validatePointUsage(currentUser, calculation.getActualUsedPoints());

        // 6. 주문 및 주문 아이템 생성
        Order order = orderCommonService.createAndSaveOrder(currentUser, OrderRequestData.from(request), calculation, adapters, optionMap, imageMap);

        // 7. 주문 후 처리
        orderCommonService.processPostOrder(currentUser, adapters, optionMap, calculation, order.getOrderId());

        // 8. 장바구니 아이템 삭제
        cartItemRepository.deleteAll(selectedCartItems);

        // 9. 주문 생성 응답 반환
        return OrderCreateResponse.from(order);
    }

    /**
     * 주문 목록 조회 (사용자)
     * @param page
     * @param size
     * @param status
     * @param loginId
     * @return
     */
    @Transactional(readOnly = true)
    public OrderPageResponse getOrderListForUser(int page, int size, String status, String loginId) {
        // 1. 현재 사용자 조회를 하고 사용자 권한을 검증
        User currentUser = orderCommonService.validateUser(loginId);

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
     * @param loginId
     * @return
     */
    @Transactional(readOnly = true)
    public OrderPageResponse getOrderListForSeller(int page, int size, String status, String loginId) {
        // 1. 현재 사용자 조회를 하고 판매자 권한을 검증
        User currentUser = validateSeller(loginId);

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
     * @param loginId
     * @return
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long orderId, String loginId) {
        // 1. 현재 사용자 조회 및 권한 검증
        User currentUser = orderCommonService.validateUser(loginId);

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
     * @param loginId
     * @return
     */
    @Transactional
    public OrderStatusUpdateResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, String loginId) {
        // 1. 판매자 권한 검증
        User seller = validateSeller(loginId);

        // 2. 주문 조회 및 권한 검증
        Order order = orderRepository.findByOrderIdAndSeller(orderId, seller)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));

        // 3. 상태 변경 가능 여부 검증
        validateStatusUpdate(order.getStatus(), request.getStatus());

        // 4. 주문 상태 업데이트
        order.updateStatus(request.getStatus());

        // 5. 주문 상태 변경 응답 반환
        return OrderStatusUpdateResponse.from(order);
    }

    /**
     * 사용자 주문 상태 변경
     * @param orderId
     * @param request
     * @param loginId
     * @return
     */
    @Transactional
    public OrderStatusUpdateResponse updateUserOrderStatus(Long orderId, OrderStatusUpdateRequest request, String loginId) {
        // 1. 현재 사용자 조회 및 권한 검증
        User user = validateUser(loginId);

        // 2. 주문 조회 및 권한 검증
        Order order = orderRepository.findByOrderIdAndUser(orderId, user)
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));

        // 3. 상태 변경 가능 여부 검증
        validateUserStatusUpdate(order.getStatus(), request.getStatus());

        // 4. 재고 복구 처리 (취소/반품 시)
        if (isStockRestoreRequired(request.getStatus())) {
            restoreStock(order);
        }

        // 4. 주문 상태 업데이트
        order.updateStatus(request.getStatus());

        // 5. 주문 상태 변경 응답 반환
        return OrderStatusUpdateResponse.from(order);
    }

    // 판매자가 주문 상태를 변경할 수 있는지 검증한다.
    private void validateStatusUpdate(OrderStatus currentStatus, OrderStatus newStatus) {
        if (!currentStatus.canChangeTo(newStatus)) {
            throw new OrderException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }

    // 유저가 주문 상태를 변경할 수 있는지 검증한다.
    private void validateUserStatusUpdate(OrderStatus currentStatus, OrderStatus newStatus) {
        if (!currentStatus.canUserChangeTo(newStatus)) {
            throw new OrderException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }

    /**
     * 현재 사용자 조회 및 사용자 권한 검증
     * @param loginId 현재 로그인된 사용자 정보
     * @return 검증된 사용자 정보
     */
    private User validateUser(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.USER) {
            throw new OrderException(ErrorCode.ORDER_FORBIDDEN);
        }

        return user;
    }

    /**
     * 현재 사용자 조회 및 판매자 권한 검증
     * @param loginId 현재 로그인된 사용자 정보
     * @return 검증된 사용자 정보
     */
    private User validateSeller(String loginId) {
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

        // 각 아이템별 검증
        for (CartItem item : selectedCartItems) {
            // 수량 검증
            if (item.getQuantity() <= 0 || item.getQuantity() > MAX_ORDER_QUANTITY) {
                throw new OrderException(ErrorCode.INVALID_ORDER_QUANTITY);
            }

            // 옵션 ID 검증
            if (item.getOptionId() == null || item.getOptionId() <= 0) {
                throw new OrderException(ErrorCode.INVALID_OPTION_ID);
            }
        }
    }

    // 재고 복구 처리가 필요한 상태인지 확인한다.
    private boolean isStockRestoreRequired(OrderStatus newStatus) {
        return newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.RETURNED;
    }

    // 주문 취소/반품 시 재고를 복구한다.
    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            ProductOption option = item.getProductOption();
            option.increaseStock(item.getQuantity());
        }
    }
}
