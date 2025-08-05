package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
import com.cMall.feedShop.order.application.dto.response.OrderDetailResponse;
import com.cMall.feedShop.order.application.dto.response.OrderListResponse;
import com.cMall.feedShop.order.application.dto.response.OrderPageResponse;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.domain.enums.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductOptionRepository productOptionRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private UserPointRepository userPointRepository;
    @Mock private DiscountCalculator discountCalculator;

    @InjectMocks
    private OrderService orderService;

    private UserDetails userDetails;
    private User user;

    @BeforeEach
    void setUp() {
        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");

        user = createTestUser();
    }

    @Test
    @DisplayName("주문 생성 - 성공")
    void createOrder_Success() {
        // Given
        OrderCreateRequest request = createValidRequest();
        List<CartItem> cartItems = createMockCartItems();
        List<ProductOption> options = createMockOptions();
        List<ProductImage> images = createMockImages();
        Order savedOrder = createMockOrder();
        UserPoint userPoint = createMockUserPoint();

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdWithCart(1L)).thenReturn(cartItems);
        when(productOptionRepository.findAllByOptionIdIn(any())).thenReturn(options);
        when(productImageRepository.findAllById(any())).thenReturn(images);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(userPointRepository.findByUser(user)).thenReturn(Optional.of(userPoint));
        when(discountCalculator.calculateDiscountPrice(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(22500)); // 할인 적용된 가격

        // When
        OrderCreateResponse response = orderService.createOrder(request, userDetails);

        // Then
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ORDERED);
        verify(cartItemRepository).deleteAll(cartItems);
        verify(orderRepository).save(any(Order.class));
        verify(productOptionRepository).saveAll(any());
        verify(userPointRepository).save(any(UserPoint.class));
    }

    @Test
    @DisplayName("주문 생성 - 사용자 없음")
    void createOrder_UserNotFound() {
        // Given
        OrderCreateRequest request = createValidRequest();
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request, userDetails))
                .isInstanceOf(UserException.class);
    }

    @Test
    @DisplayName("주문 생성 - 권한 없음 (SELLER)")
    void createOrder_Forbidden() {
        // Given
        OrderCreateRequest request = createValidRequest();
        User sellerUser = new User("seller", "password", "seller@test.com", UserRole.SELLER);
        setField(sellerUser, "id", 1L);

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(sellerUser));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request, userDetails))
                .isInstanceOf(OrderException.class);
    }

    @Test
    @DisplayName("주문 생성 - 장바구니 비어있음")
    void createOrder_EmptyCart() {
        // Given
        OrderCreateRequest request = createValidRequest();
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdWithCart(1L)).thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request, userDetails))
                .isInstanceOf(OrderException.class);
    }

    @Test
    @DisplayName("주문 생성 - 상품 옵션 없음")
    void createOrder_ProductOptionNotFound() {
        // Given
        OrderCreateRequest request = createValidRequest();
        List<CartItem> cartItems = createMockCartItems();

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdWithCart(1L)).thenReturn(cartItems);
        when(productOptionRepository.findAllByOptionIdIn(any())).thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request, userDetails))
                .isInstanceOf(ProductException.class);
    }

    @Test
    @DisplayName("주문 생성 - 재고 부족")
    void createOrder_InsufficientStock() {
        // Given
        OrderCreateRequest request = createValidRequest();
        List<CartItem> cartItems = createMockCartItems();
        List<ProductOption> lowStockOptions = List.of(
                createMockOption(1L, 1) // 재고 1개만 있음
        );

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserIdWithCart(1L)).thenReturn(cartItems);
        when(productOptionRepository.findAllByOptionIdIn(any())).thenReturn(lowStockOptions);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request, userDetails))
                .isInstanceOf(ProductException.class);
    }

    // 테스트 헬퍼 메서드들
    private User createTestUser() {
        User user = new User("testuser", "password", "test@test.com", UserRole.USER);
        setField(user, "id", 1L);
        return user;
    }

    private OrderCreateRequest createValidRequest() {
        OrderCreateRequest request = new OrderCreateRequest();
        setField(request, "deliveryAddress", "서울시 강남구");
        setField(request, "deliveryDetailAddress", "테헤란로 123");
        setField(request, "postalCode", "12345");
        setField(request, "recipientName", "홍길동");
        setField(request, "recipientPhone", "010-1234-5678");
        setField(request, "usedPoints", 1000);
        setField(request, "deliveryFee", BigDecimal.valueOf(3000));
        setField(request, "paymentMethod", "카드");
        setField(request, "cardNumber", "1234567890123");
        setField(request, "cardExpiry", "1225");
        setField(request, "cardCvc", "123");
        return request;
    }

    private List<CartItem> createMockCartItems() {
        Cart cart = Cart.builder().user(user).build();

        CartItem item = CartItem.builder()
                .cart(cart)
                .optionId(1L)
                .imageId(1L)
                .quantity(2)
                .build();

        // selected 필드를 true로 설정
        setField(item, "selected", true);
        setField(item, "cartItemId", 1L);

        return List.of(item);
    }

    private List<ProductOption> createMockOptions() {
        return List.of(createMockOption(1L, 10));
    }

    private ProductOption createMockOption(Long optionId, Integer stock) {
        // Product 먼저 생성
        Product product = Product.builder()
                .name("테스트 상품")
                .price(BigDecimal.valueOf(25000))
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(BigDecimal.valueOf(10))
                .build();
        setField(product, "productId", 1L);

        // ProductOption 생성
        ProductOption option = new ProductOption(
                Gender.UNISEX,
                Size.SIZE_250,
                Color.BLACK,
                stock,
                product
        );
        setField(option, "optionId", optionId);

        return option;
    }

    private List<ProductImage> createMockImages() {
        ProductImage image = new ProductImage(
                "http://example.com/image.jpg",
                ImageType.MAIN,
                null
        );
        setField(image, "imageId", 1L);

        return List.of(image);
    }

    private Order createMockOrder() {
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.ORDERED)
                .totalPrice(BigDecimal.valueOf(45000))
                .finalPrice(BigDecimal.valueOf(48000)) // 배송비 포함
                .deliveryFee(BigDecimal.valueOf(3000))
                .usedPoints(1000)
                .earnedPoints(250)
                .deliveryAddress("서울시 강남구")
                .deliveryDetailAddress("테헤란로 123")
                .postalCode("12345")
                .recipientName("홍길동")
                .recipientPhone("010-1234-5678")
                .paymentMethod("카드")
                .build();
        setField(order, "orderId", 1L);

        return order;
    }

    private UserPoint createMockUserPoint() {
        UserPoint userPoint = UserPoint.builder()
                .user(user)
                .currentPoints(5000)
                .build();
        return userPoint;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =======================
// 주문 목록 조회 테스트 추가
// =======================

    @Test
    @DisplayName("주문 목록 조회 - 성공 (전체 조회)")
    void getOrderList_Success_All() {
        // Given
        List<Order> orders = createMockOrders();
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 2);

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class)))
                .thenReturn(orderPage);

        // When
        OrderPageResponse response = orderService.getOrderListForUser(0, 10, null, userDetails);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElement()).isEqualTo(2);
        assertThat(response.getTotalPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getNumber()).isEqualTo(0);

        // 첫 번째 주문 검증
        OrderListResponse firstOrder = response.getContent().get(0);
        assertThat(firstOrder.getOrderId()).isEqualTo(1L);
        assertThat(firstOrder.getStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(firstOrder.getItems()).hasSize(1);

        verify(userRepository).findByLoginId("testuser");
        verify(orderRepository).findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class));
    }

    @Test
    @DisplayName("주문 목록 조회 - 성공 (상태별 필터링)")
    void getOrderList_Success_WithStatusFilter() {
        // Given
        List<Order> orders = createMockOrders().stream()
                .filter(order -> order.getStatus() == OrderStatus.ORDERED)
                .toList();
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 1);

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserAndStatusOrderByCreatedAtDesc(eq(user), eq(OrderStatus.ORDERED), any(Pageable.class)))
                .thenReturn(orderPage);

        // When
        OrderPageResponse response = orderService.getOrderListForUser(0, 10, "ORDERED", userDetails);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getStatus()).isEqualTo(OrderStatus.ORDERED);

        verify(orderRepository).findByUserAndStatusOrderByCreatedAtDesc(eq(user), eq(OrderStatus.ORDERED), any(Pageable.class));
    }

    @Test
    @DisplayName("주문 목록 조회 - 성공 (status='all')")
    void getOrderList_Success_StatusAll() {
        // Given
        List<Order> orders = createMockOrders();
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 2);

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class)))
                .thenReturn(orderPage);

        // When
        OrderPageResponse response = orderService.getOrderListForUser(0, 10, "all", userDetails);

        // Then
        assertThat(response.getContent()).hasSize(2);
        verify(orderRepository).findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class));
    }

    @Test
    @DisplayName("주문 목록 조회 - 페이지 파라미터 검증 (음수 페이지)")
    void getOrderList_Success_NegativePageAdjusted() {
        // Given
        Page<Order> orderPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class)))
                .thenReturn(orderPage);

        // When
        OrderPageResponse response = orderService.getOrderListForUser(-5, 10, null, userDetails);

        // Then
        assertThat(response.getNumber()).isEqualTo(0);
        verify(orderRepository).findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class));
    }

    @Test
    @DisplayName("주문 목록 조회 - 페이지 사이즈 검증 (잘못된 사이즈)")
    void getOrderList_Success_InvalidSizeAdjusted() {
        // Given
        Page<Order> orderPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class)))
                .thenReturn(orderPage);

        // When
        OrderPageResponse response1 = orderService.getOrderListForUser(0, 0, null, userDetails);
        OrderPageResponse response2 = orderService.getOrderListForUser(0, 200, null, userDetails);

        // Then
        assertThat(response1.getSize()).isEqualTo(10); // 기본값으로 조정
        assertThat(response2.getSize()).isEqualTo(10); // 기본값으로 조정
    }

    @Test
    @DisplayName("주문 목록 조회 - 빈 목록 반환")
    void getOrderList_Success_EmptyList() {
        // Given
        Page<Order> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        OrderPageResponse response = orderService.getOrderListForUser(0, 10, null, userDetails);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElement()).isEqualTo(0);
    }

    @Test
    @DisplayName("주문 목록 조회 - 권한 없음 (SELLER)")
    void getOrderList_Forbidden() {
        // Given
        User sellerUser = new User("seller", "password", "seller@test.com", UserRole.SELLER);
        setField(sellerUser, "id", 2L);

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(sellerUser));

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderListForUser(0, 10, null, userDetails))
                .isInstanceOf(OrderException.class)
                .hasMessage(ErrorCode.ORDER_FORBIDDEN.getMessage());
    }

    @Test
    @DisplayName("주문 목록 조회 - 잘못된 주문 상태")
    void getOrderList_InvalidOrderStatus() {
        // Given
        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderListForUser(0, 10, "INVALID_STATUS", userDetails))
                .isInstanceOf(OrderException.class)
                .hasMessage(ErrorCode.INVALID_ORDER_STATUS.getMessage());
    }

// =======================
// 테스트 헬퍼 메서드 추가
// =======================

    private List<Order> createMockOrders() {
        // 첫 번째 주문 (ORDERED)
        Order order1 = Order.builder()
                .user(user)
                .status(OrderStatus.ORDERED)
                .totalPrice(BigDecimal.valueOf(45000))
                .finalPrice(BigDecimal.valueOf(48000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .usedPoints(1000)
                .earnedPoints(250)
                .deliveryAddress("서울시 강남구")
                .deliveryDetailAddress("테헤란로 123")
                .postalCode("12345")
                .recipientName("홍길동")
                .recipientPhone("010-1234-5678")
                .paymentMethod("카드")
                .build();
        setField(order1, "orderId", 1L);

        // 주문 아이템 추가
        OrderItem orderItem1 = createMockOrderItem(order1, 1L);
        order1.addOrderItem(orderItem1);

        // 두 번째 주문 (SHIPPED)
        Order order2 = Order.builder()
                .user(user)
                .status(OrderStatus.SHIPPED)
                .totalPrice(BigDecimal.valueOf(30000))
                .finalPrice(BigDecimal.valueOf(33000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .usedPoints(0)
                .earnedPoints(150)
                .deliveryAddress("서울시 서초구")
                .deliveryDetailAddress("강남대로 456")
                .postalCode("54321")
                .recipientName("김철수")
                .recipientPhone("010-9876-5432")
                .paymentMethod("카드")
                .build();
        setField(order2, "orderId", 2L);

        // 주문 아이템 추가
        OrderItem orderItem2 = createMockOrderItem(order2, 2L);
        order2.addOrderItem(orderItem2);

        return List.of(order1, order2);
    }

    private OrderItem createMockOrderItem(Order order, Long itemId) {
        // Product 생성
        Product product = Product.builder()
                .name("테스트 상품 " + itemId)
                .price(BigDecimal.valueOf(25000))
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(BigDecimal.valueOf(10))
                .build();
        setField(product, "productId", itemId);

        // ProductOption 생성
        ProductOption option = new ProductOption(
                Gender.UNISEX,
                Size.SIZE_250,
                Color.BLACK,
                10,
                product
        );
        setField(option, "optionId", itemId);

        // ProductImage 생성
        ProductImage image = new ProductImage(
                "http://example.com/image" + itemId + ".jpg",
                ImageType.MAIN,
                product
        );
        setField(image, "imageId", itemId);

        // OrderItem 생성
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .productOption(option)
                .productImage(image)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(50000))
                .finalPrice(BigDecimal.valueOf(45000))
                .build();
        setField(orderItem, "orderItemId", itemId);

        return orderItem;
    }

    // =======================
    // 주문 상세 조회 테스트 추가
    // =======================

    @Test
    @DisplayName("주문 상세 조회 - 성공")
    void getOrderDetail_Success() {
        // Given
        Long orderId = 1L;
        Order order = createDetailOrder();

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.findByOrderIdAndUser(orderId, user)).thenReturn(Optional.of(order));

        // When
        OrderDetailResponse response = orderService.getOrderDetail(orderId, userDetails);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(response.getUsedPoints()).isEqualTo(1000);
        assertThat(response.getEarnedPoints()).isEqualTo(250);
        assertThat(response.getFinalPrice()).isEqualTo(BigDecimal.valueOf(48000));

        // 배송 정보 검증
        assertThat(response.getShippingInfo()).isNotNull();
        assertThat(response.getShippingInfo().getRecipientName()).isEqualTo("홍길동");
        assertThat(response.getShippingInfo().getRecipientPhone()).isEqualTo("010-1234-5678");
        assertThat(response.getShippingInfo().getDeliveryAddress()).isEqualTo("서울시 강남구");

        // 결제 정보 검증
        assertThat(response.getPaymentInfo()).isNotNull();
        assertThat(response.getPaymentInfo().getPaymentMethod()).isEqualTo("카드");
        assertThat(response.getPaymentInfo().getCardNumber()).isEqualTo("**** **** **** 3456"); // 마스킹 확인

        // 주문 상품 정보 검증
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems().get(0).getProductName()).isEqualTo("테스트 상품1");
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);

        verify(userRepository).findByLoginId("testuser");
        verify(orderRepository).findByOrderIdAndUser(orderId, user);
    }

    @Test
    @DisplayName("주문 상세 조회 - 주문 없음")
    void getOrderDetail_OrderNotFound() {
        // Given
        Long orderId = 999L;

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(user));
        when(orderRepository.findByOrderIdAndUser(orderId, user)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderDetail(orderId, userDetails))
                .isInstanceOf(OrderException.class)
                .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());

        verify(userRepository).findByLoginId("testuser");
        verify(orderRepository).findByOrderIdAndUser(orderId, user);
    }

    @Test
    @DisplayName("주문 상세 조회 - 사용자 없음")
    void getOrderDetail_UserNotFound() {
        // Given
        Long orderId = 1L;

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderDetail(orderId, userDetails))
                .isInstanceOf(UserException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());

        verify(userRepository).findByLoginId("testuser");
        verify(orderRepository, never()).findByOrderIdAndUser(any(), any());
    }

    @Test
    @DisplayName("주문 상세 조회 - 권한 없음 (SELLER 역할)")
    void getOrderDetail_Forbidden() {
        // Given
        Long orderId = 1L;
        User sellerUser = new User("seller", "password", "seller@test.com", UserRole.SELLER);
        setField(sellerUser, "id", 2L);

        when(userRepository.findByLoginId("testuser")).thenReturn(Optional.of(sellerUser));

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderDetail(orderId, userDetails))
                .isInstanceOf(OrderException.class)
                .hasMessage(ErrorCode.ORDER_FORBIDDEN.getMessage());

        verify(userRepository).findByLoginId("testuser");
        verify(orderRepository, never()).findByOrderIdAndUser(any(), any());
    }

    // =======================
    // 테스트 헬퍼 메서드 추가
    // =======================

    private Order createDetailOrder() {
        // 상품 생성
        Product product1 = Product.builder()
                .name("테스트 상품1")
                .price(BigDecimal.valueOf(25000))
                .discountType(DiscountType.RATE_DISCOUNT)
                .discountValue(BigDecimal.valueOf(10))
                .build();
        setField(product1, "productId", 1L);

        Product product2 = Product.builder()
                .name("테스트 상품2")
                .price(BigDecimal.valueOf(20000))
                .discountType(DiscountType.FIXED_DISCOUNT)
                .discountValue(BigDecimal.valueOf(2000))
                .build();
        setField(product2, "productId", 2L);

        // 상품 옵션 생성
        ProductOption option1 = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.BLACK, 10, product1);
        setField(option1, "optionId", 1L);

        ProductOption option2 = new ProductOption(Gender.UNISEX, Size.SIZE_260, Color.WHITE, 5, product2);
        setField(option2, "optionId", 2L);

        // 상품 이미지 생성
        ProductImage image1 = new ProductImage("http://example.com/image1.jpg", ImageType.MAIN, null);
        setField(image1, "imageId", 1L);

        ProductImage image2 = new ProductImage("http://example.com/image2.jpg", ImageType.MAIN, null);
        setField(image2, "imageId", 2L);

        // 주문 생성
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.ORDERED)
                .totalPrice(BigDecimal.valueOf(45000))
                .finalPrice(BigDecimal.valueOf(48000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .usedPoints(1000)
                .earnedPoints(250)
                .deliveryAddress("서울시 강남구")
                .deliveryDetailAddress("테헤란로 123")
                .postalCode("12345")
                .recipientName("홍길동")
                .recipientPhone("010-1234-5678")
                .deliveryMessage("문 앞에 놓아주세요")
                .paymentMethod("카드")
                .cardNumber("1234567890123456")
                .cardExpiry("1225")
                .cardCvc("123")
                .build();
        setField(order, "orderId", 1L);

        // 주문 아이템 생성
        OrderItem orderItem1 = OrderItem.builder()
                .order(order)
                .productOption(option1)
                .productImage(image1)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(25000))
                .finalPrice(BigDecimal.valueOf(22500)) // 할인 적용
                .build();
        setField(orderItem1, "orderItemId", 1L);

        OrderItem orderItem2 = OrderItem.builder()
                .order(order)
                .productOption(option2)
                .productImage(image2)
                .quantity(1)
                .totalPrice(BigDecimal.valueOf(20000))
                .finalPrice(BigDecimal.valueOf(18000)) // 할인 적용
                .build();
        setField(orderItem2, "orderItemId", 2L);

        // 주문에 주문 아이템 추가
        setField(order, "orderItems", List.of(orderItem1, orderItem2));

        return order;
    }
}