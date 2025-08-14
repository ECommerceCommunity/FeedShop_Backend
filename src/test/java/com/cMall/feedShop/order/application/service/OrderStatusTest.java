package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.dto.request.OrderStatusUpdateRequest;
import com.cMall.feedShop.order.application.dto.response.OrderStatusUpdateResponse;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 🔍 초보자 설명:
 * 이 테스트는 주문 상태 변경 기능이 올바르게 동작하는지 확인합니다.
 * - 판매자가 주문 상태를 변경하는 경우
 * - 사용자가 자신의 주문 상태를 변경하는 경우
 * - 잘못된 상태 변경을 시도하는 경우
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("주문 상태 변경 테스트")
class OrderStatusTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private OrderService orderService;

    // 테스트에서 사용할 기본 데이터들
    private User sellerUser;  // 판매자 사용자
    private User buyerUser;   // 구매자 사용자
    private Order testOrder;  // 테스트용 주문
    private OrderStatusUpdateRequest statusUpdateRequest;  // 상태 변경 요청

    @BeforeEach
    void setUp() {
        // 판매자 사용자 생성 (상품을 파는 사람)
        sellerUser = new User("seller123", "password", "seller@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(sellerUser, "id", 1L);

        // 구매자 사용자 생성 (상품을 사는 사람)
        buyerUser = new User("buyer123", "password", "buyer@test.com", UserRole.USER);
        ReflectionTestUtils.setField(buyerUser, "id", 2L);

        // 기본 테스트 주문 생성 (주문됨 상태)
        testOrder = Order.builder()
                .user(buyerUser)               // 주문한 사용자
                .status(OrderStatus.ORDERED)   // 처음엔 주문됨 상태
                .totalPrice(BigDecimal.valueOf(50000))
                .finalPrice(BigDecimal.valueOf(53000))
                .deliveryFee(BigDecimal.valueOf(3000))
                .recipientName("김구매자")
                .recipientPhone("010-1234-5678")
                .deliveryAddress("서울시 강남구")
                .paymentMethod("카드")
                .build();
        ReflectionTestUtils.setField(testOrder, "orderId", 100L);

        // UserDetails를 String으로 변경했으므로 더 이상 mock 설정이 필요하지 않음
    }

    /**
     * 🏪 판매자 주문 상태 변경 테스트
     * 판매자가 자신이 판매한 상품의 주문 상태를 변경하는 테스트
     */
    @Nested
    @DisplayName("판매자 주문 상태 변경")
    class SellerOrderStatusUpdate {

        @Test
        @DisplayName("성공: 주문됨 → 배송중으로 변경")
        void updateOrderStatus_Success_OrderedToShipped() {
            // Given: 주문을 배송중으로 변경하려는 상황
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.SHIPPED);

            // Mock 설정: 판매자 조회 성공
            given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(sellerUser));
            // Mock 설정: 주문 조회 성공 (판매자의 상품 주문)
            given(orderRepository.findByOrderIdAndSeller(orderId, sellerUser))
                    .willReturn(Optional.of(testOrder));

            // When: 주문 상태 변경 실행
            OrderStatusUpdateResponse response = orderService.updateOrderStatus(orderId, statusUpdateRequest, "seller123");

            // Then: 결과 검증
            assertThat(response).isNotNull();                                    // 응답이 있는지 확인
            assertThat(response.getOrderId()).isEqualTo(100L);                  // 주문 ID가 맞는지 확인
            assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPED);    // 상태가 변경되었는지 확인
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);   // 실제 주문 객체도 변경되었는지 확인

            // Mock 메서드가 올바르게 호출되었는지 검증
            verify(userRepository).findByLoginId("seller123");
            verify(orderRepository).findByOrderIdAndSeller(orderId, sellerUser);
        }

        @Test
        @DisplayName("성공: 주문됨 → 취소로 변경")
        void updateOrderStatus_Success_OrderedToCancelled() {
            // Given: 주문을 취소로 변경하려는 상황
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.CANCELLED);

            given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(sellerUser));
            given(orderRepository.findByOrderIdAndSeller(orderId, sellerUser))
                    .willReturn(Optional.of(testOrder));

            // When: 주문 상태 변경 실행
            OrderStatusUpdateResponse response = orderService.updateOrderStatus(orderId, statusUpdateRequest, "seller123");

            // Then: 결과 검증
            assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("성공: 배송중 → 배송완료로 변경")
        void updateOrderStatus_Success_ShippedToDelivered() {
            // Given: 배송중인 주문을 배송완료로 변경
            testOrder.updateStatus(OrderStatus.SHIPPED);  // 먼저 배송중 상태로 만들기
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.DELIVERED);

            given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(sellerUser));
            given(orderRepository.findByOrderIdAndSeller(orderId, sellerUser))
                    .willReturn(Optional.of(testOrder));

            // When
            OrderStatusUpdateResponse response = orderService.updateOrderStatus(orderId, statusUpdateRequest, "seller123");

            // Then
            assertThat(response.getStatus()).isEqualTo(OrderStatus.DELIVERED);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("실패: 판매자 권한이 없는 경우")
        void updateOrderStatus_Fail_NotSeller() {
            // Given: 일반 사용자가 판매자 권한 필요한 작업을 시도
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.SHIPPED);

            // 일반 사용자로 로그인
            given(userRepository.findByLoginId("user123")).willReturn(Optional.of(buyerUser));

            // When & Then: 권한 오류가 발생해야 함
            assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, statusUpdateRequest, "user123"))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(ErrorCode.ORDER_FORBIDDEN.getMessage());

            verify(userRepository).findByLoginId("user123");
        }

        @Test
        @DisplayName("실패: 주문을 찾을 수 없는 경우")
        void updateOrderStatus_Fail_OrderNotFound() {
            // Given: 존재하지 않는 주문 ID
            Long orderId = 999L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.SHIPPED);

            given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(sellerUser));
            // 주문을 찾지 못하는 상황
            given(orderRepository.findByOrderIdAndSeller(orderId, sellerUser))
                    .willReturn(Optional.empty());

            // When & Then: 주문 없음 오류가 발생해야 함
            assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, statusUpdateRequest, "seller123"))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("실패: 잘못된 상태 변경 (주문됨 → 배송완료)")
        void updateOrderStatus_Fail_InvalidStatusChange() {
            // Given: 주문됨 상태에서 바로 배송완료로 변경 시도 (잘못된 순서)
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.DELIVERED);

            given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(sellerUser));
            given(orderRepository.findByOrderIdAndSeller(orderId, sellerUser))
                    .willReturn(Optional.of(testOrder));

            // When & Then: 잘못된 상태 변경 오류가 발생해야 함
            assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, statusUpdateRequest, "seller123"))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(ErrorCode.INVALID_ORDER_STATUS.getMessage());
        }

        @Test
        @DisplayName("실패: 이미 완료된 주문 상태 변경 시도")
        void updateOrderStatus_Fail_AlreadyCompletedOrder() {
            // Given: 이미 배송완료된 주문을 다시 변경 시도
            testOrder.updateStatus(OrderStatus.DELIVERED);  // 배송완료 상태로 설정
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.SHIPPED);

            given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(sellerUser));
            given(orderRepository.findByOrderIdAndSeller(orderId, sellerUser))
                    .willReturn(Optional.of(testOrder));

            // When & Then: 완료된 주문은 변경할 수 없음
            assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, statusUpdateRequest, "seller123"))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(ErrorCode.INVALID_ORDER_STATUS.getMessage());
        }
    }

    /**
     * 🛒 사용자 주문 상태 변경 테스트
     * 일반 사용자가 자신의 주문 상태를 변경하는 테스트 (취소, 반품)
     */
    @Nested
    @DisplayName("사용자 주문 상태 변경")
    class UserOrderStatusUpdate {

        @Test
        @DisplayName("성공: 주문 취소 (주문됨 → 취소)")
        void updateUserOrderStatus_Success_OrderCancel() {
            // Given: 주문된 상품을 취소하려는 상황
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.CANCELLED);

            given(userRepository.findByLoginId("user123")).willReturn(Optional.of(buyerUser));
            given(orderRepository.findByOrderIdAndUser(orderId, buyerUser))
                    .willReturn(Optional.of(testOrder));

            // When: 주문 취소 실행
            OrderStatusUpdateResponse response = orderService.updateUserOrderStatus(orderId, statusUpdateRequest, "user123");

            // Then: 취소 성공 검증
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(100L);
            assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

            verify(userRepository).findByLoginId("user123");
            verify(orderRepository).findByOrderIdAndUser(orderId, buyerUser);
        }

        @Test
        @DisplayName("성공: 상품 반품 (배송완료 → 반품)")
        void updateUserOrderStatus_Success_OrderReturn() {
            // Given: 배송완료된 상품을 반품하려는 상황
            testOrder.updateStatus(OrderStatus.DELIVERED);  // 배송완료 상태로 설정
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.RETURNED);

            given(userRepository.findByLoginId("user123")).willReturn(Optional.of(buyerUser));
            given(orderRepository.findByOrderIdAndUser(orderId, buyerUser))
                    .willReturn(Optional.of(testOrder));

            // When: 반품 신청 실행
            OrderStatusUpdateResponse response = orderService.updateUserOrderStatus(orderId, statusUpdateRequest, "user123");

            // Then: 반품 신청 성공 검증
            assertThat(response.getStatus()).isEqualTo(OrderStatus.RETURNED);
            assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.RETURNED);
        }

        @Test
        @DisplayName("실패: 다른 사용자의 주문 변경 시도")
        void updateUserOrderStatus_Fail_NotMyOrder() {
            // Given: 다른 사용자의 주문을 변경하려는 시도
            User anotherUser = new User("another", "password", "another@test.com", UserRole.USER);
            ReflectionTestUtils.setField(anotherUser, "id", 3L);

            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.CANCELLED);

            given(userRepository.findByLoginId("user123")).willReturn(Optional.of(anotherUser));
            // 다른 사용자로는 주문을 찾을 수 없음
            given(orderRepository.findByOrderIdAndUser(orderId, anotherUser))
                    .willReturn(Optional.empty());

            // When & Then: 주문 없음 오류 발생
            assertThatThrownBy(() -> orderService.updateUserOrderStatus(orderId, statusUpdateRequest, "user123"))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("실패: 잘못된 상태에서 취소 시도 (배송중 → 취소)")
        void updateUserOrderStatus_Fail_InvalidCancelStatus() {
            // Given: 이미 배송중인 상품을 취소하려는 시도
            testOrder.updateStatus(OrderStatus.SHIPPED);  // 배송중 상태로 설정
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.CANCELLED);

            given(userRepository.findByLoginId("user123")).willReturn(Optional.of(buyerUser));
            given(orderRepository.findByOrderIdAndUser(orderId, buyerUser))
                    .willReturn(Optional.of(testOrder));

            // When & Then: 배송중인 상품은 취소할 수 없음
            assertThatThrownBy(() -> orderService.updateUserOrderStatus(orderId, statusUpdateRequest, "user123"))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(ErrorCode.INVALID_ORDER_STATUS.getMessage());
        }

        @Test
        @DisplayName("실패: 잘못된 상태에서 반품 시도 (주문됨 → 반품)")
        void updateUserOrderStatus_Fail_InvalidReturnStatus() {
            // Given: 아직 배송되지 않은 상품을 반품하려는 시도
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.RETURNED);

            given(userRepository.findByLoginId("user123")).willReturn(Optional.of(buyerUser));
            given(orderRepository.findByOrderIdAndUser(orderId, buyerUser))
                    .willReturn(Optional.of(testOrder));

            // When & Then: 배송완료되지 않은 상품은 반품할 수 없음
            assertThatThrownBy(() -> orderService.updateUserOrderStatus(orderId, statusUpdateRequest, "user123"))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(ErrorCode.INVALID_ORDER_STATUS.getMessage());
        }

        @Test
        @DisplayName("실패: 사용자가 판매자 전용 상태로 변경 시도")
        void updateUserOrderStatus_Fail_UserTriesToSetSellerStatus() {
            // Given: 사용자가 배송중 상태로 변경 시도 (판매자만 가능)
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.SHIPPED);

            given(userRepository.findByLoginId("user123")).willReturn(Optional.of(buyerUser));
            given(orderRepository.findByOrderIdAndUser(orderId, buyerUser))
                    .willReturn(Optional.of(testOrder));

            // When & Then: 사용자는 배송 상태를 변경할 수 없음
            assertThatThrownBy(() -> orderService.updateUserOrderStatus(orderId, statusUpdateRequest, "user123"))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(ErrorCode.INVALID_ORDER_STATUS.getMessage());
        }

        @Test
        @DisplayName("실패: 이미 취소된 주문에 대한 추가 변경")
        void updateUserOrderStatus_Fail_AlreadyCancelledOrder() {
            // Given: 이미 취소된 주문을 또 변경하려는 시도
            testOrder.updateStatus(OrderStatus.CANCELLED);  // 취소 상태로 설정
            Long orderId = 100L;
            statusUpdateRequest = createStatusUpdateRequest(OrderStatus.RETURNED);

            given(userRepository.findByLoginId("user123")).willReturn(Optional.of(buyerUser));
            given(orderRepository.findByOrderIdAndUser(orderId, buyerUser))
                    .willReturn(Optional.of(testOrder));

            // When & Then: 취소된 주문은 더 이상 변경할 수 없음
            assertThatThrownBy(() -> orderService.updateUserOrderStatus(orderId, statusUpdateRequest, "user123"))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(ErrorCode.INVALID_ORDER_STATUS.getMessage());
        }
    }

    /**
     * 🛠️ 테스트 헬퍼 메서드
     * 테스트에서 반복적으로 사용되는 객체 생성을 돕는 메서드
     */
    private OrderStatusUpdateRequest createStatusUpdateRequest(OrderStatus status) {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        // ReflectionTestUtils를 사용해 private 필드에 값 설정
        ReflectionTestUtils.setField(request, "status", status);
        return request;
    }
}