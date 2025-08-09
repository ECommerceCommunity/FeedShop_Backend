package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
import com.cMall.feedShop.order.application.dto.response.OrderDetailResponse;
import com.cMall.feedShop.order.application.dto.response.OrderPageResponse;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductImageRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("주문 서비스 테스트")
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductOptionRepository productOptionRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DiscountCalculator discountCalculator;
    @Mock
    private OrderCommonService orderCommonService;
    @Mock
    private UserDetails userDetails;

    private User testUser;
    private Store testStore;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // UserDetails Mock 설정을 setUp에서 제거
        // when(userDetails.getUsername()).thenReturn("testuser");

        testStore = createTestStore();
        testCategory = createTestCategory();
        testUser = createTestUser();
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        // Given
        // 실제로 사용되는 테스트에서만 Mock 설정
        // when(userDetails.getUsername()).thenReturn("testuser");

        OrderCreateRequest request = createValidRequest();
        List<CartItem> cartItems = createMockCartItems();
        List<ProductOption> options = createMockOptions();
        List<ProductImage> images = createMockImages();
        Order savedOrder = createMockOrder();

        // OrderCommonService Mock 설정
        given(orderCommonService.validateUser(userDetails)).willReturn(testUser);
        given(orderCommonService.calculateActualUsedPoints(any(), any())).willReturn(0);
        given(orderCommonService.calculateFinalAmount(any(), any())).willReturn(BigDecimal.valueOf(50000));
        given(orderCommonService.calculateEarnedPoints(any())).willReturn(250);

        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(cartItems);
        given(productOptionRepository.findAllByOptionIdIn(any(Set.class))).willReturn(options);
        given(productImageRepository.findAllById(any(Set.class))).willReturn(images);
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
        given(discountCalculator.calculateDiscountPrice(any(), any(), any()))
                .willReturn(BigDecimal.valueOf(50000));

        // When
        OrderCreateResponse response = orderService.createOrder(request, userDetails);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ORDERED);

        verify(orderCommonService).validateUser(userDetails);
        verify(cartItemRepository).findByUserIdWithCart(1L);
        verify(orderCommonService).validatePointUsage(any(), any());
        verify(orderCommonService).processUserPoints(any(), any(), any());
        verify(orderRepository).save(any(Order.class));
        verify(cartItemRepository).deleteAll(cartItems);
    }

    @Test
    @DisplayName("주문 목록 조회 성공")
    void getOrderListForUser_Success() {
        // Given
        // when(userDetails.getUsername()).thenReturn("testuser");

        List<Order> orders = createMockOrders();
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 2);

        given(orderCommonService.validateUser(userDetails)).willReturn(testUser);
        given(orderRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class)))
                .willReturn(orderPage);

        // When
        OrderPageResponse response = orderService.getOrderListForUser(0, 10, null, userDetails);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElement()).isEqualTo(2);

        verify(orderCommonService).validateUser(userDetails);
        verify(orderRepository).findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class));
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_Success() {
        // Given
        // when(userDetails.getUsername()).thenReturn("testuser");

        Long orderId = 1L;
        Order order = createDetailOrder();

        given(orderCommonService.validateUser(userDetails)).willReturn(testUser);
        given(orderRepository.findByOrderIdAndUser(orderId, testUser)).willReturn(Optional.of(order));

        // When
        OrderDetailResponse response = orderService.getOrderDetail(orderId, userDetails);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ORDERED);

        verify(orderCommonService).validateUser(userDetails);
        verify(orderRepository).findByOrderIdAndUser(orderId, testUser);
    }

    // 테스트 데이터 생성 메서드들
    private User createTestUser() {
        User user = new User("testuser", "password123", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private Store createTestStore() {
        Store store = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .description("테스트 설명")
                .logo("http://logo.jpg")
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);
        return store;
    }

    private Category createTestCategory() {
        Category category = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(category, "categoryId", 1L);
        return category;
    }

    private OrderCreateRequest createValidRequest() {
        OrderCreateRequest request = new OrderCreateRequest();
        ReflectionTestUtils.setField(request, "deliveryAddress", "서울시 강남구");
        ReflectionTestUtils.setField(request, "deliveryDetailAddress", "테스트동 123-45");
        ReflectionTestUtils.setField(request, "postalCode", "12345");
        ReflectionTestUtils.setField(request, "recipientName", "홍길동");
        ReflectionTestUtils.setField(request, "recipientPhone", "010-1234-5678");
        ReflectionTestUtils.setField(request, "usedPoints", 0);
        ReflectionTestUtils.setField(request, "deliveryMessage", "문앞에 놓아주세요");
        ReflectionTestUtils.setField(request, "deliveryFee", BigDecimal.valueOf(3000));
        ReflectionTestUtils.setField(request, "paymentMethod", "카드");
        ReflectionTestUtils.setField(request, "cardNumber", "1234567890123456");
        ReflectionTestUtils.setField(request, "cardExpiry", "1225");
        ReflectionTestUtils.setField(request, "cardCvc", "123");
        return request;
    }

    private List<CartItem> createMockCartItems() {
        Product product = createTestProduct();
        Cart cart = createTestCart();

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .optionId(createTestProductOption(product).getOptionId())
                .imageId(createTestProductImage(product).getImageId())
                .quantity(2)
                .build();
        ReflectionTestUtils.setField(cartItem, "cartItemId", 1L);
        ReflectionTestUtils.setField(cartItem, "selected", true);

        return List.of(cartItem);
    }

    private Product createTestProduct() {
        Product product = Product.builder()
                .name("테스트 운동화")
                .description("테스트용 운동화입니다")
                .price(BigDecimal.valueOf(50000))
                .discountType(DiscountType.NONE)
                .discountValue(BigDecimal.ZERO)
                .category(testCategory)
                .store(testStore)
                .build();
        ReflectionTestUtils.setField(product, "productId", 1L);
        return product;
    }

    private ProductOption createTestProductOption(Product product) {
        ProductOption option = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 100, product);
        ReflectionTestUtils.setField(option, "optionId", 1L);
        return option;
    }

    private ProductImage createTestProductImage(Product product) {
        ProductImage image = new ProductImage("http://test-image.jpg", ImageType.MAIN, product);
        ReflectionTestUtils.setField(image, "imageId", 1L);
        return image;
    }

    private Cart createTestCart() {
        Cart cart = Cart.builder().user(testUser).build();
        ReflectionTestUtils.setField(cart, "cartId", 1L);
        return cart;
    }

    private List<ProductOption> createMockOptions() {
        Product product = createTestProduct();
        ProductOption option = createTestProductOption(product);
        return List.of(option);
    }

    private ProductOption createMockOption(Long id, int stock) {
        Product product = createTestProduct();
        ProductOption option = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, stock, product);
        ReflectionTestUtils.setField(option, "optionId", id);
        return option;
    }

    private List<ProductImage> createMockImages() {
        Product product = createTestProduct();
        ProductImage image = createTestProductImage(product);
        return List.of(image);
    }

    private Order createMockOrder() {
        Order order = Order.builder()
                .user(testUser)
                .status(OrderStatus.ORDERED)
                .totalPrice(BigDecimal.valueOf(50000))
                .finalPrice(BigDecimal.valueOf(53000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .usedPoints(0)
                .earnedPoints(250)
                .deliveryAddress("서울시 강남구")
                .deliveryDetailAddress("테스트동 123-45")
                .postalCode("12345")
                .recipientName("홍길동")
                .recipientPhone("010-1234-5678")
                .deliveryMessage("문앞에 놓아주세요")
                .paymentMethod("카드")
                .cardNumber("1234567890123456")
                .cardExpiry("1225")
                .cardCvc("123")
                .build();
        ReflectionTestUtils.setField(order, "orderId", 1L);
        return order;
    }

    private List<Order> createMockOrders() {
        Order order1 = createMockOrder();

        Order order2 = Order.builder()
                .user(testUser)
                .status(OrderStatus.ORDERED)
                .totalPrice(BigDecimal.valueOf(30000))
                .finalPrice(BigDecimal.valueOf(33000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .usedPoints(0)
                .earnedPoints(150)
                .deliveryAddress("서울시 강남구")
                .deliveryDetailAddress("테스트동 123-45")
                .postalCode("12345")
                .recipientName("홍길동")
                .recipientPhone("010-1234-5678")
                .paymentMethod("카드")
                .cardNumber("1234567890123456")
                .cardExpiry("1225")
                .cardCvc("123")
                .build();
        ReflectionTestUtils.setField(order2, "orderId", 2L);

        return List.of(order1, order2);
    }

    private Order createDetailOrder() {
        Order order = createMockOrder();

        Product product = createTestProduct();
        ProductOption option = createTestProductOption(product);
        ProductImage image = createTestProductImage(product);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .productOption(option)
                .productImage(image)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(50000))
                .finalPrice(BigDecimal.valueOf(50000))
                .build();
        ReflectionTestUtils.setField(orderItem, "orderItemId", 1L);

        ReflectionTestUtils.setField(order, "orderItems", List.of(orderItem));
        return order;
    }
}