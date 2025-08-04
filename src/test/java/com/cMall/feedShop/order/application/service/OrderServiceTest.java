package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.product.application.util.DiscountCalculator;
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
}