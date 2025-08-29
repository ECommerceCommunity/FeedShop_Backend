package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.cart.domain.repository.CartItemRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.calculator.OrderCalculation;
import com.cMall.feedShop.order.application.dto.request.OrderCreateRequest;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
import com.cMall.feedShop.order.application.dto.response.OrderDetailResponse;
import com.cMall.feedShop.order.application.dto.response.OrderPageResponse;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * OrderService 테스트 클래스
 * - 장바구니를 통한 주문 생성 기능을 테스트합니다
 * - OrderCommonService를 사용하는 새로운 구조에 맞춰 완전히 새로 작성되었습니다
 * - 모든 테스트는 Mock을 사용하여 외부 의존성 없이 실행됩니다
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("주문 서비스 테스트 - 새 버전")
class OrderServiceTest {

    // 테스트할 실제 서비스 (Mock 객체들이 주입됨)
    @InjectMocks
    private OrderService orderService;

    // Mock 객체들 (실제 DB나 외부 서비스 대신 가짜 객체 사용)
    @Mock
    private CartItemRepository cartItemRepository; // 장바구니 아이템 저장소
    @Mock
    private OrderRepository orderRepository; // 주문 저장소
    @Mock
    private OrderHelper orderHelper; // 공통 주문 서비스

    // 테스트에서 공통으로 사용할 데이터들
    private User testUser; // 테스트용 사용자
    private OrderCreateRequest testRequest; // 테스트용 주문 요청
    private List<CartItem> testCartItems; // 테스트용 장바구니 아이템들
    private ProductOption testProductOption; // 테스트용 상품 옵션
    private ProductImage testProductImage; // 테스트용 상품 이미지
    private OrderCalculation testCalculation; // 테스트용 주문 계산 결과
    private Order testOrder; // 테스트용 주문

    @BeforeEach
    void setUp() {
        // 1. 테스트용 사용자 생성
        testUser = createTestUser();

        // 2. 테스트용 상품 옵션 생성
        testProductOption = createTestProductOption();

        // 3. 테스트용 상품 이미지 생성
        testProductImage = createTestProductImage();

        // 4. 테스트용 장바구니 아이템들 생성
        testCartItems = createTestCartItems();

        // 5. 테스트용 주문 요청 생성
        testRequest = createTestOrderRequest();

        // 6. 테스트용 주문 계산 결과 생성
        testCalculation = createTestOrderCalculation();

        // 7. 테스트용 주문 생성
        testOrder = createTestOrder();
    }

    /**
     * 테스트 1: 정상적인 주문 생성
     */
    @Test
    @DisplayName("정상적인 주문 생성 성공")
    void createOrder_Success() {
        // Given: 테스트 준비 단계

        // 사용자 검증이 성공한다고 설정
        given(orderHelper.validateUser("testUser")).willReturn(testUser);

        // 선택된 장바구니 아이템 조회가 성공한다고 설정
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(testCartItems);

        // 상품 옵션 조회가 성공한다고 설정
        Map<Long, ProductOption> optionMap = Map.of(1L, testProductOption);
        given(orderHelper.getValidProductOptions(any())).willReturn(optionMap);

        // 상품 이미지 조회가 성공한다고 설정
        Map<Long, ProductImage> imageMap = Map.of(1L, testProductImage);
        given(orderHelper.getProductImages(any())).willReturn(imageMap);

        // 주문 금액 계산이 성공한다고 설정
        given(orderHelper.calculateOrderAmount(any(), any(), anyInt())).willReturn(testCalculation);

        // 포인트 검증이 통과한다고 설정
        willDoNothing().given(orderHelper).validatePointUsage(any(), anyInt());

        // 주문 생성 및 저장이 성공한다고 설정
        given(orderHelper.createAndSaveOrder(any(), any(), any(), any(), any(), any())).willReturn(testOrder);

        // 주문 후 처리가 성공한다고 설정
        willDoNothing().given(orderHelper).processPostOrder(any(), any(), any(), any(), any());

        // 장바구니 아이템 삭제가 성공한다고 설정
        willDoNothing().given(cartItemRepository).deleteAll(testCartItems);

        // When: 실제 테스트 실행
        OrderCreateResponse response = orderService.createOrder(testRequest, "testUser");

        // Then: 결과 검증

        // 응답이 null이 아닌지 확인
        assertThat(response).isNotNull();

        // 주문 ID가 올바른지 확인
        assertThat(response.getOrderId()).isEqualTo(1L);

        // 주문 상태가 ORDERED인지 확인
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ORDERED);

        // 모든 메서드가 올바른 순서로 호출되었는지 검증
        verify(orderHelper).validateUser("testUser"); // 1. 사용자 검증
        verify(cartItemRepository).findByUserIdWithCart(1L); // 2. 장바구니 조회
        verify(orderHelper).getValidProductOptions(any()); // 3. 상품 옵션 조회
        verify(orderHelper).getProductImages(any()); // 4. 상품 이미지 조회
        verify(orderHelper).calculateOrderAmount(any(), any(), anyInt()); // 5. 금액 계산
        verify(orderHelper).validatePointUsage(any(), anyInt()); // 6. 포인트 검증
        verify(orderHelper).createAndSaveOrder(any(), any(), any(), any(), any(), any()); // 7. 주문 생성
        verify(orderHelper).processPostOrder(any(), any(), any(), any(), any()); // 8. 후처리
        verify(cartItemRepository).deleteAll(testCartItems); // 9. 장바구니 삭제
    }

    /**
     * 테스트 2: 빈 장바구니로 주문 시 예외 발생
     */
    @Test
    @DisplayName("빈 장바구니로 주문 시 예외 발생")
    void createOrder_EmptyCart_ThrowsException() {
        // Given: 빈 장바구니 설정
        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(Collections.emptyList()); // 빈 장바구니

        // When & Then: 예외가 발생하는지 확인
        assertThatThrownBy(() -> orderService.createOrder(testRequest, "testUser"))
                .isInstanceOf(OrderException.class) // OrderException이 발생해야 함
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_CART_EMPTY); // 에러 코드 확인

        // 사용자 검증과 장바구니 조회만 호출되었는지 확인
        verify(orderHelper).validateUser("testUser");
        verify(cartItemRepository).findByUserIdWithCart(1L);
        verify(orderHelper, never()).getValidProductOptions(any()); // 이후 메서드들은 호출되지 않아야 함
    }

    /**
     * 테스트 3: 주문 목록 조회
     */
    @Test
    @DisplayName("주문 목록 조회 성공")
    void getOrderListForUser_Success() {
        // Given: 주문 목록 데이터 준비
        List<Order> mockOrders = createMockOrders(); // 2개의 주문
        Page<Order> orderPage = new PageImpl<>(mockOrders, PageRequest.of(0, 10), 2);

        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(orderRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class))).willReturn(orderPage);

        // When: 주문 목록 조회 실행
        OrderPageResponse response = orderService.getOrderListForUser(0, 10, null, "testUser");

        // Then: 결과 검증
        assertThat(response).isNotNull(); // 응답이 null이 아님
        assertThat(response.getContent()).hasSize(2); // 2개의 주문이 있음
        assertThat(response.getTotalElement()).isEqualTo(2); // 총 2개 요소

        // 메서드 호출 검증
        verify(orderHelper).validateUser("testUser");
        verify(orderRepository).findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class));
    }

    /**
     * 테스트 4: 주문 상세 조회
     */
    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_Success() {
        // Given: 주문 상세 데이터 준비
        Long orderId = 1L;
        Order detailOrder = createDetailOrder(); // 상세 정보가 포함된 주문

        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(orderRepository.findByOrderIdAndUser(orderId, testUser)).willReturn(Optional.of(detailOrder));

        // When: 주문 상세 조회 실행
        OrderDetailResponse response = orderService.getOrderDetail(orderId, "testUser");

        // Then: 결과 검증
        assertThat(response).isNotNull(); // 응답이 null이 아님
        assertThat(response.getOrderId()).isEqualTo(1L); // 주문 ID 확인
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ORDERED); // 주문 상태 확인
        assertThat(response.getTotalPrice()).isEqualTo(BigDecimal.valueOf(100000)); // 총 가격 확인

        // 메서드 호출 검증
        verify(orderHelper).validateUser("testUser");
        verify(orderRepository).findByOrderIdAndUser(orderId, testUser);
    }

    /**
     * 테스트 5: 존재하지 않는 주문 조회 시 예외 발생
     */
    @Test
    @DisplayName("존재하지 않는 주문 조회 시 예외 발생")
    void getOrderDetail_NotFound_ThrowsException() {
        // Given: 존재하지 않는 주문 ID
        Long orderId = 999L;

        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(orderRepository.findByOrderIdAndUser(orderId, testUser)).willReturn(Optional.empty()); // 주문이 없음

        // When & Then: 예외가 발생하는지 확인
        assertThatThrownBy(() -> orderService.getOrderDetail(orderId, "testUser"))
                .isInstanceOf(OrderException.class) // OrderException이 발생해야 함
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND); // 에러 코드 확인

        // 메서드 호출 검증
        verify(orderHelper).validateUser("testUser");
        verify(orderRepository).findByOrderIdAndUser(orderId, testUser);
    }

    /**
     * 테스트 6: 선택된 장바구니 아이템이 없을 때 예외 발생
     */
    @Test
    @DisplayName("선택된 장바구니 아이템이 없을 때 예외 발생")
    void createOrder_NoSelectedItems_ThrowsException() {
        // Given: 모든 장바구니 아이템이 선택되지 않음
        List<CartItem> unselectedItems = createUnselectedCartItems();

        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(unselectedItems);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(testRequest, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_CART_EMPTY);

        verify(orderHelper).validateUser("testUser");
        verify(cartItemRepository).findByUserIdWithCart(1L);
    }

    /**
     * 테스트 7: 주문 수량이 최대 허용량을 초과할 때 예외 발생
     */
    @Test
    @DisplayName("주문 수량이 최대 허용량을 초과할 때 예외 발생")
    void createOrder_ExcessiveQuantity_ThrowsException() {
        // Given: 최대 수량을 초과하는 장바구니 아이템
        List<CartItem> excessiveQuantityItems = createExcessiveQuantityCartItems();

        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(excessiveQuantityItems);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(testRequest, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_QUANTITY);
    }

    /**
     * 테스트 8: 사용자 검증 실패 시 예외 발생
     */
    @Test
    @DisplayName("사용자 검증 실패 시 예외 발생")
    void createOrder_InvalidUser_ThrowsException() {
        // Given: 유효하지 않은 사용자
        given(orderHelper.validateUser("testUser"))
                .willThrow(new OrderException(ErrorCode.USER_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(testRequest, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(orderHelper).validateUser("testUser");
    }

    /**
     * 테스트 9: 포인트 부족 시 예외 발생
     */
    @Test
    @DisplayName("포인트 부족 시 예외 발생")
    void createOrder_InsufficientPoints_ThrowsException() {
        // Given: 포인트가 부족한 상황
        List<CartItem> cartItems = createTestCartItems();
        OrderCalculation calculation = createTestOrderCalculation();

        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(cartItems);
        given(orderHelper.getValidProductOptions(any())).willReturn(Map.of(1L, testProductOption));
        given(orderHelper.getProductImages(any())).willReturn(Map.of(1L, testProductImage));
        given(orderHelper.calculateOrderAmount(any(), any(), anyInt())).willReturn(calculation);

        // 포인트 부족으로 예외 발생
        willThrow(new OrderException(ErrorCode.INVALID_POINT))
                .given(orderHelper).validatePointUsage(any(), anyInt());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(testRequest, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POINT);
    }

    /**
     * 테스트 10: 상품 옵션을 찾을 수 없을 때 예외 발생
     */
    @Test
    @DisplayName("상품 옵션을 찾을 수 없을 때 예외 발생")
    void createOrder_ProductOptionNotFound_ThrowsException() {
        // Given: 존재하지 않는 상품 옵션
        List<CartItem> cartItems = createTestCartItems();

        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(cartItems);
        given(orderHelper.getValidProductOptions(any()))
                .willThrow(new OrderException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(testRequest, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_OPTION_NOT_FOUND);
    }

    /**
     * 테스트 11: 재고 부족 시 예외 발생
     */
    @Test
    @DisplayName("재고 부족 시 예외 발생")
    void createOrder_InsufficientStock_ThrowsException() {
        // Given: 재고 부족 상황
        List<CartItem> cartItems = createTestCartItems();

        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(cartItemRepository.findByUserIdWithCart(1L)).willReturn(cartItems);
        given(orderHelper.getValidProductOptions(any())).willReturn(Map.of(1L, testProductOption));
        given(orderHelper.getProductImages(any())).willReturn(Map.of(1L, testProductImage));
        given(orderHelper.calculateOrderAmount(any(), any(), anyInt())).willReturn(testCalculation);
        willDoNothing().given(orderHelper).validatePointUsage(any(), anyInt());
        given(orderHelper.createAndSaveOrder(any(), any(), any(), any(), any(), any())).willReturn(testOrder);

        // 재고 부족으로 예외 발생
        willThrow(new OrderException(ErrorCode.OUT_OF_STOCK))
                .given(orderHelper).processPostOrder(any(), any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(testRequest, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OUT_OF_STOCK);
    }

    // ========================================
    // 주문 목록 조회 추가 테스트들
    // ========================================

    /**
     * 테스트 12: 페이지 번호가 음수일 때 0으로 보정
     */
    @Test
    @DisplayName("페이지 번호가 음수일 때 0으로 보정")
    void getOrderListForUser_NegativePage_CorrectedToZero() {
        // Given: 음수 페이지
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList());
        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(orderRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class)))
                .willReturn(emptyPage);

        // When
        OrderPageResponse response = orderService.getOrderListForUser(-5, 10, null, "testUser");

        // Then: 페이지가 0으로 보정되었는지 확인
        assertThat(response).isNotNull();
        verify(orderRepository).findByUserOrderByCreatedAtDesc(eq(testUser), eq(PageRequest.of(0, 10)));
    }

    /**
     * 테스트 13: 페이지 크기가 범위를 벗어날 때 기본값으로 보정
     */
    @Test
    @DisplayName("페이지 크기가 범위를 벗어날 때 기본값으로 보정")
    void getOrderListForUser_InvalidSize_CorrectedToDefault() {
        // Given: 잘못된 페이지 크기
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList());
        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(orderRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class)))
                .willReturn(emptyPage);

        // When: 크기가 101 (최대 100 초과)
        OrderPageResponse response = orderService.getOrderListForUser(0, 101, null, "testUser");

        // Then: 크기가 10으로 보정되었는지 확인
        assertThat(response).isNotNull();
        verify(orderRepository).findByUserOrderByCreatedAtDesc(eq(testUser), eq(PageRequest.of(0, 10)));
    }

    // ========================================
    // 테스트 데이터 생성 메서드들 (Helper Methods)
    // ========================================

    /**
     * 테스트용 사용자 생성
     */
    private User createTestUser() {
        User user = new User("testUser", "password123", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L); // ID를 1로 설정
        return user;
    }

    /**
     * 테스트용 상품 옵션 생성
     */
    private ProductOption createTestProductOption() {
        // 먼저 상품을 생성
        Product product = Product.builder()
                .name("테스트 운동화")
                .description("테스트용 운동화")
                .price(BigDecimal.valueOf(50000)) // 가격: 5만원
                .discountType(DiscountType.NONE) // 할인 없음
                .discountValue(BigDecimal.ZERO) // 할인 금액 0원
                .build();

        // 상품 옵션 생성 (250사이즈, 흰색, 재고 100개)
        ProductOption option = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 100, product);
        ReflectionTestUtils.setField(option, "optionId", 1L); // ID 설정
        return option;
    }

    /**
     * 테스트용 상품 이미지 생성
     */
    private ProductImage createTestProductImage() {
        Product product = testProductOption.getProduct(); // 위에서 만든 상품 사용
        ProductImage image = new ProductImage("http://test-image.jpg", ImageType.MAIN, product);
        ReflectionTestUtils.setField(image, "imageId", 1L); // ID 설정
        return image;
    }

    /**
     * 테스트용 장바구니 아이템들 생성
     */
    private List<CartItem> createTestCartItems() {
        // 장바구니 생성
        Cart cart = new Cart(testUser);
        ReflectionTestUtils.setField(cart, "cartId", 1L);

        // 장바구니 아이템 생성 (운동화 2개, 선택됨)
        CartItem cartItem = new CartItem(cart, testProductOption.getOptionId(), 2L, 2); // 수량 2개, 선택됨
        ReflectionTestUtils.setField(cartItem, "cartItemId", 1L);

        return List.of(cartItem);
    }

    /**
     * 테스트용 주문 요청 생성
     */
    private OrderCreateRequest createTestOrderRequest() {
        OrderCreateRequest request = new OrderCreateRequest();
        ReflectionTestUtils.setField(request, "deliveryAddress", "서울시 강남구"); // 배송 주소
        ReflectionTestUtils.setField(request, "deliveryDetailAddress", "테스트동 123-45"); // 상세 주소
        ReflectionTestUtils.setField(request, "postalCode", "12345"); // 우편번호
        ReflectionTestUtils.setField(request, "recipientName", "홍길동"); // 받는 사람
        ReflectionTestUtils.setField(request, "recipientPhone", "010-1234-5678"); // 전화번호
        ReflectionTestUtils.setField(request, "usedPoints", 0); // 사용할 포인트: 0원
        ReflectionTestUtils.setField(request, "deliveryMessage", "문 앞에 놓아주세요"); // 배송 메시지
        ReflectionTestUtils.setField(request, "deliveryFee", BigDecimal.valueOf(3000)); // 배송비: 3000원
        ReflectionTestUtils.setField(request, "paymentMethod", "카드"); // 결제 방법
        ReflectionTestUtils.setField(request, "cardNumber", "1234567890123456"); // 카드 번호
        ReflectionTestUtils.setField(request, "cardExpiry", "1225"); // 카드 만료일
        ReflectionTestUtils.setField(request, "cardCvc", "123"); // CVC 번호
        return request;
    }

    /**
     * 테스트용 주문 계산 결과 생성
     */
    private OrderCalculation createTestOrderCalculation() {
        return OrderCalculation.builder()
                .totalAmount(BigDecimal.valueOf(100000)) // 총 금액: 10만원 (5만원 x 2개)
                .finalAmount(BigDecimal.valueOf(100000)) // 최종 금액: 10만원 (포인트 사용 안함)
                .actualUsedPoints(0) // 실제 사용된 포인트: 0원
                .earnedPoints(500) // 적립될 포인트: 500원 (0.5% 적립)
                .build();
    }

    /**
     * 테스트용 주문 생성
     */
    private Order createTestOrder() {
        Order order = Order.builder()
                .user(testUser) // 주문한 사용자
                .status(OrderStatus.ORDERED) // 주문 상태
                .totalPrice(BigDecimal.valueOf(100000)) // 총 가격
                .finalPrice(BigDecimal.valueOf(103000)) // 최종 가격 (배송비 포함)
                .deliveryFee(BigDecimal.valueOf(3000)) // 배송비
                .usedPoints(0) // 사용된 포인트
                .earnedPoints(500) // 적립될 포인트
                .deliveryAddress("서울시 강남구") // 배송 주소
                .deliveryDetailAddress("테스트동 123-45") // 상세 주소
                .postalCode("12345") // 우편번호
                .recipientName("홍길동") // 받는 사람
                .recipientPhone("010-1234-5678") // 전화번호
                .deliveryMessage("문 앞에 놓아주세요") // 배송 메시지
                .paymentMethod("카드") // 결제 방법
                .cardNumber("1234567890123456") // 카드 번호
                .cardExpiry("1225") // 카드 만료일
                .cardCvc("123") // CVC
                .build();
        ReflectionTestUtils.setField(order, "orderId", 1L); // 주문 ID 설정
        return order;
    }

    /**
     * 테스트용 주문 목록 생성 (2개)
     */
    private List<Order> createMockOrders() {
        // 첫 번째 주문
        Order order1 = Order.builder()
                .user(testUser)
                .status(OrderStatus.ORDERED)
                .totalPrice(BigDecimal.valueOf(50000))
                .finalPrice(BigDecimal.valueOf(53000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .recipientName("홍길동")
                .build();
        ReflectionTestUtils.setField(order1, "orderId", 1L);

        // 두 번째 주문
        Order order2 = Order.builder()
                .user(testUser)
                .status(OrderStatus.SHIPPED)
                .totalPrice(BigDecimal.valueOf(80000))
                .finalPrice(BigDecimal.valueOf(83000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .recipientName("홍길동")
                .build();
        ReflectionTestUtils.setField(order2, "orderId", 2L);

        return List.of(order1, order2);
    }

    /**
     * 테스트용 상세 주문 생성 (주문 아이템 포함)
     */
    private Order createDetailOrder() {
        Order order = createTestOrder(); // 기본 주문 생성

        // 주문 아이템 생성
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .productOption(testProductOption)
                .productImage(testProductImage)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(50000))
                .finalPrice(BigDecimal.valueOf(50000))
                .build();
        ReflectionTestUtils.setField(orderItem, "orderItemId", 1L);

        // 주문에 아이템 추가
        order.addOrderItem(orderItem);

        return order;
    }

    private List<CartItem> createUnselectedCartItems() {
        CartItem item = new CartItem();
        ReflectionTestUtils.setField(item, "selected", false); // 선택되지 않음
        ReflectionTestUtils.setField(item, "quantity", 2);
        return List.of(item);
    }

    private List<CartItem> createExcessiveQuantityCartItems() {
        CartItem item = new CartItem();
        ReflectionTestUtils.setField(item, "quantity", 1000); // 최대 수량 초과
        ReflectionTestUtils.setField(item, "selected", true);
        ReflectionTestUtils.setField(item, "optionId", testProductOption.getOptionId());
        return List.of(item);
    }

    private List<Order> createOrdersWithStatus(OrderStatus status) {
        Order order = new Order();
        ReflectionTestUtils.setField(order, "status", status);
        ReflectionTestUtils.setField(order, "orderId", 1L);
        ReflectionTestUtils.setField(order, "user", testUser);
        ReflectionTestUtils.setField(order, "totalPrice", BigDecimal.valueOf(50000));
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());
        return List.of(order);
    }
}