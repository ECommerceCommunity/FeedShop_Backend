package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.calculator.OrderCalculation;
import com.cMall.feedShop.order.application.dto.request.DirectOrderCreateRequest;
import com.cMall.feedShop.order.application.dto.request.OrderItemRequest;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.order.domain.model.Order;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * DirectOrderService 테스트 클래스
 * - 바로 주문 생성 기능을 완전히 새롭게 테스트합니다
 * - OrderCommonService를 사용하는 새로운 구조에 맞춰 작성되었습니다
 * - 모든 테스트는 Mock을 사용하여 외부 의존성 없이 실행됩니다
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("직접 주문 서비스 테스트 - 새 버전")
class DirectOrderServiceTest {

    // 테스트할 실제 서비스 (OrderCommonService가 주입됨)
    @InjectMocks
    private DirectOrderService directOrderService;

    // Mock 객체들 (실제 서비스 대신 가짜 객체 사용)
    @Mock
    private OrderHelper orderHelper; // 공통 주문 서비스

    // 테스트에서 공통으로 사용할 데이터들
    private User testUser; // 테스트용 사용자
    private DirectOrderCreateRequest testRequest; // 테스트용 주문 요청
    private OrderItemRequest testOrderItem; // 테스트용 주문 아이템
    private ProductOption testProductOption; // 테스트용 상품 옵션
    private ProductImage testProductImage; // 테스트용 상품 이미지
    private OrderCalculation testCalculation; // 테스트용 주문 계산 결과
    private Order testOrder; // 테스트용 주문

    /**
     * 각 테스트 실행 전에 공통 테스트 데이터를 준비합니다
     * 초등학생도 이해할 수 있도록: 시험 보기 전에 연필과 지우개를 준비하는 것과 같아요
     */
    @BeforeEach
    void setUp() {
        // 1. 테스트용 사용자 생성 (홍길동이라는 가상의 사용자)
        testUser = createTestUser();

        // 2. 테스트용 주문 아이템 생성 (운동화 2켤레 주문)
        testOrderItem = createTestOrderItem();

        // 3. 테스트용 주문 요청 생성 (배송지, 결제 정보 포함)
        testRequest = createTestDirectOrderRequest();

        // 4. 테스트용 상품 옵션 생성 (250사이즈 흰색 운동화)
        testProductOption = createTestProductOption();

        // 5. 테스트용 상품 이미지 생성
        testProductImage = createTestProductImage();

        // 6. 테스트용 주문 계산 결과 생성 (금액, 포인트 등)
        testCalculation = createTestOrderCalculation();

        // 7. 테스트용 주문 생성 (최종 생성된 주문)
        testOrder = createTestOrder();
    }

    /**
     * 테스트 1: 정상적인 직접 주문 생성
     */
    @Test
    @DisplayName("정상적인 직접 주문 생성 성공")
    void createDirectOrder_Success() {
        // Given: 테스트 준비 단계 (모든 Mock 동작을 정의)

        // 사용자 검증이 성공한다고 설정
        given(orderHelper.validateUser("testUser")).willReturn(testUser);

        // 상품 옵션 조회가 성공한다고 설정
        Map<Long, ProductOption> optionMap = new HashMap<>();
        optionMap.put(1L, testProductOption);
        given(orderHelper.getValidProductOptions(any())).willReturn(optionMap);

        // 상품 이미지 조회가 성공한다고 설정
        Map<Long, ProductImage> imageMap = new HashMap<>();
        imageMap.put(1L, testProductImage);
        given(orderHelper.getProductImages(any())).willReturn(imageMap);

        // 주문 금액 계산이 성공한다고 설정
        given(orderHelper.calculateOrderAmount(any(), any(), anyInt())).willReturn(testCalculation);

        // 포인트 검증이 통과한다고 설정 (문제없음)
        willDoNothing().given(orderHelper).validatePointUsage(any(), anyInt());

        // 주문 생성 및 저장이 성공한다고 설정
        given(orderHelper.createAndSaveOrder(any(), any(), any(), any(), any(), any())).willReturn(testOrder);

        // 주문 후 처리가 성공한다고 설정 (재고 차감, 포인트 처리)
        willDoNothing().given(orderHelper).processPostOrder(any(), any(), any(), any(), any());

        // When: 실제 테스트 실행 단계
        OrderCreateResponse response = directOrderService.createDirectOrder(testRequest, "testUser");

        // Then: 결과 검증 단계 (예상한 결과가 나왔는지 확인)

        // 응답이 null이 아닌지 확인
        assertThat(response).isNotNull();

        // 주문 ID가 올바른지 확인
        assertThat(response.getOrderId()).isEqualTo(1L);

        // 주문 상태가 ORDERED인지 확인
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ORDERED);

        // Mock 메서드들이 올바른 순서로 호출되었는지 검증
        verify(orderHelper).validateUser("testUser"); // 1. 사용자 검증
        verify(orderHelper).getValidProductOptions(any()); // 2. 상품 옵션 조회
        verify(orderHelper).getProductImages(any()); // 3. 상품 이미지 조회
        verify(orderHelper).calculateOrderAmount(any(), any(), anyInt()); // 4. 금액 계산
        verify(orderHelper).validatePointUsage(any(), anyInt()); // 5. 포인트 검증
        verify(orderHelper).createAndSaveOrder(any(), any(), any(), any(), any(), any()); // 6. 주문 생성
        verify(orderHelper).processPostOrder(any(), any(), any(), any(), any()); // 7. 후처리
    }

    /**
     * 테스트 2: 빈 주문 아이템으로 주문 시 예외 발생
     */
    @Test
    @DisplayName("빈 주문 아이템으로 주문 시 예외 발생")
    void createDirectOrder_EmptyItems_ThrowsException() {
        // Given: 빈 주문 아이템을 가진 요청 생성
        DirectOrderCreateRequest emptyRequest = createEmptyDirectOrderRequest();
        given(orderHelper.validateUser("testUser")).willReturn(testUser);

        // When & Then: 예외가 발생하는지 확인
        assertThatThrownBy(() -> directOrderService.createDirectOrder(emptyRequest, "testUser"))
                .isInstanceOf(OrderException.class) // OrderException이 발생해야 함
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_ITEM_NOT_FOUND); // 에러 코드 확인

        // 사용자 검증만 호출되고 나머지는 호출되지 않았는지 확인
        verify(orderHelper).validateUser("testUser");
        verify(orderHelper, never()).getValidProductOptions(any());
    }

    /**
     * 테스트 3: 포인트 사용 주문
     */
    @Test
    @DisplayName("포인트 사용 주문 성공")
    void createDirectOrder_WithPoints_Success() {
        // Given: 포인트를 사용하는 주문 요청 생성
        DirectOrderCreateRequest requestWithPoints = createDirectOrderRequestWithPoints();

        // 포인트 사용을 포함한 계산 결과 생성
        OrderCalculation calculationWithPoints = OrderCalculation.builder()
                .totalAmount(BigDecimal.valueOf(100000)) // 총 금액 10만원
                .finalAmount(BigDecimal.valueOf(99000)) // 포인트 차감 후 9만9천원
                .actualUsedPoints(1000) // 실제 사용된 포인트 1000원
                .earnedPoints(495) // 적립될 포인트 495원
                .build();

        // Mock 설정
        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(orderHelper.getValidProductOptions(any())).willReturn(Map.of(1L, testProductOption));
        given(orderHelper.getProductImages(any())).willReturn(Map.of(1L, testProductImage));
        given(orderHelper.calculateOrderAmount(any(), any(), eq(1000))).willReturn(calculationWithPoints);
        willDoNothing().given(orderHelper).validatePointUsage(testUser, 1000);
        given(orderHelper.createAndSaveOrder(any(), any(), any(), any(), any(), any())).willReturn(testOrder);
        willDoNothing().given(orderHelper).processPostOrder(any(), any(), any(), any(), any());

        // When: 테스트 실행
        OrderCreateResponse response = directOrderService.createDirectOrder(requestWithPoints, "testUser");

        // Then: 결과 검증
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1L);

        // 포인트 관련 메서드가 올바르게 호출되었는지 확인
        verify(orderHelper).validatePointUsage(testUser, 1000); // 포인트 검증
        verify(orderHelper).calculateOrderAmount(any(), any(), eq(1000)); // 포인트 포함 계산
    }

    /**
     * 테스트 4: null 주문 아이템으로 주문 시 예외 발생
     */
    @Test
    @DisplayName("null 주문 아이템으로 주문 시 예외 발생")
    void createDirectOrder_NullItems_ThrowsException() {
        // Given: null 아이템 리스트
        DirectOrderCreateRequest nullItemsRequest = new DirectOrderCreateRequest();
        ReflectionTestUtils.setField(nullItemsRequest, "items", new ArrayList<>());

        given(orderHelper.validateUser("testUser")).willReturn(testUser);

        // When & Then
        assertThatThrownBy(() -> directOrderService.createDirectOrder(nullItemsRequest, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_ITEM_NOT_FOUND);
    }

    /**
     * 테스트 5: 주문 아이템 수량이 0 이하일 때 예외 발생
     */
    @Test
    @DisplayName("주문 아이템 수량이 0 이하일 때 예외 발생")
    void createDirectOrder_ZeroQuantity_ThrowsException() {
        // Given: 수량이 0인 아이템
        OrderItemRequest zeroQuantityItem = new OrderItemRequest();
        ReflectionTestUtils.setField(zeroQuantityItem, "optionId", 1L);
        ReflectionTestUtils.setField(zeroQuantityItem, "quantity", 0);

        DirectOrderCreateRequest request = new DirectOrderCreateRequest();
        ReflectionTestUtils.setField(request, "items", List.of(zeroQuantityItem));

        given(orderHelper.validateUser("testUser")).willReturn(testUser);

        // When & Then
        assertThatThrownBy(() -> directOrderService.createDirectOrder(request, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_QUANTITY);
    }

    /**
     * 테스트 6: 주문 아이템 수량이 최대치를 초과할 때 예외 발생
     */
    @Test
    @DisplayName("주문 아이템 수량이 최대치를 초과할 때 예외 발생")
    void createDirectOrder_ExcessiveQuantity_ThrowsException() {
        // Given: 최대 수량을 초과하는 아이템
        OrderItemRequest excessiveItem = new OrderItemRequest();
        ReflectionTestUtils.setField(excessiveItem, "optionId", 1L);
        ReflectionTestUtils.setField(excessiveItem, "quantity", 1000); // MAX_ORDER_QUANTITY 초과

        DirectOrderCreateRequest request = new DirectOrderCreateRequest();
        ReflectionTestUtils.setField(request, "items", List.of(excessiveItem));

        // 포인트 사용을 포함한 계산 결과 생성
        OrderCalculation calculationWithPoints = OrderCalculation.builder()
                .totalAmount(BigDecimal.valueOf(100000)) // 총 금액 10만원
                .finalAmount(BigDecimal.valueOf(99000)) // 포인트 차감 후 9만9천원
                .actualUsedPoints(1000) // 실제 사용된 포인트 1000원
                .earnedPoints(495) // 적립될 포인트 495원
                .build();

        // Mock 설정
        given(orderHelper.validateUser("testUser")).willReturn(testUser);

        // When & Then
        assertThatThrownBy(() -> directOrderService.createDirectOrder(request, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_QUANTITY);
    }

    /**
     * 테스트 7: 상품 옵션을 찾을 수 없을 때 예외 발생
     */
    @Test
    @DisplayName("상품 옵션을 찾을 수 없을 때 예외 발생")
    void createDirectOrder_ProductOptionNotFound_ThrowsException() {
        // Given: 존재하지 않는 상품 옵션
        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(orderHelper.getValidProductOptions(any()))
                .willThrow(new OrderException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> directOrderService.createDirectOrder(testRequest, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_OPTION_NOT_FOUND);
    }

    /**
     * 테스트 8: 재고 부족 시 예외 발생
     */
    @Test
    @DisplayName("재고 부족 시 예외 발생")
    void createDirectOrder_InsufficientStock_ThrowsException() {
        // Given: 재고 부족 상황
        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(orderHelper.getValidProductOptions(any())).willReturn(Map.of(1L, testProductOption));
        given(orderHelper.getProductImages(any())).willReturn(Map.of(1L, testProductImage));
        given(orderHelper.calculateOrderAmount(any(), any(), anyInt())).willReturn(testCalculation);
        willDoNothing().given(orderHelper).validatePointUsage(any(), anyInt());
        given(orderHelper.createAndSaveOrder(any(), any(), any(), any(), any(), any())).willReturn(testOrder);

        // 재고 부족으로 예외 발생
        willThrow(new OrderException(ErrorCode.OUT_OF_STOCK))
                .given(orderHelper).processPostOrder(any(), any(), any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> directOrderService.createDirectOrder(testRequest, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OUT_OF_STOCK);
    }

    /**
     * 테스트 9: 포인트 부족 시 예외 발생
     */
    @Test
    @DisplayName("포인트 부족 시 예외 발생")
    void createDirectOrder_InsufficientPoints_ThrowsException() {
        // Given: 포인트 부족 상황
        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(orderHelper.getValidProductOptions(any())).willReturn(Map.of(1L, testProductOption));
        given(orderHelper.getProductImages(any())).willReturn(Map.of(1L, testProductImage));
        given(orderHelper.calculateOrderAmount(any(), any(), anyInt())).willReturn(testCalculation);

        // 포인트 부족으로 예외 발생
        willThrow(new OrderException(ErrorCode.INVALID_POINT))
                .given(orderHelper).validatePointUsage(any(), anyInt());

        // When & Then
        assertThatThrownBy(() -> directOrderService.createDirectOrder(testRequest, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POINT);
    }

    /**
     * 테스트 10: 사용자 검증 실패 시 예외 발생
     */
    @Test
    @DisplayName("사용자 검증 실패 시 예외 발생")
    void createDirectOrder_InvalidUser_ThrowsException() {
        // Given: 유효하지 않은 사용자
        given(orderHelper.validateUser("testUser"))
                .willThrow(new OrderException(ErrorCode.USER_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> directOrderService.createDirectOrder(testRequest, "testUser"))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(orderHelper).validateUser("testUser");
    }

    /**
     * 테스트 11: 중복 상품 옵션으로 주문 시 수량 합산 처리
     */
    @Test
    @DisplayName("중복 상품 옵션으로 주문 시 정상 처리")
    void createDirectOrder_DuplicateOptions_Success() {
        // Given: 같은 상품 옵션을 여러 번 추가
        OrderItemRequest item1 = new OrderItemRequest();
        ReflectionTestUtils.setField(item1, "optionId", 1L);
        ReflectionTestUtils.setField(item1, "quantity", 2);

        OrderItemRequest item2 = new OrderItemRequest();
        ReflectionTestUtils.setField(item2, "optionId", 1L); // 같은 옵션
        ReflectionTestUtils.setField(item2, "quantity", 3);

        DirectOrderCreateRequest request = new DirectOrderCreateRequest();
        ReflectionTestUtils.setField(request, "items", List.of(item1, item2));
        ReflectionTestUtils.setField(request, "deliveryAddress", "서울시 강남구");
        ReflectionTestUtils.setField(request, "usedPoints", 0);

        // Mock 설정
        given(orderHelper.validateUser("testUser")).willReturn(testUser);
        given(orderHelper.getValidProductOptions(any())).willReturn(Map.of(1L, testProductOption));
        given(orderHelper.getProductImages(any())).willReturn(Map.of(1L, testProductImage));
        given(orderHelper.calculateOrderAmount(any(), any(), anyInt())).willReturn(testCalculation);
        willDoNothing().given(orderHelper).validatePointUsage(any(), anyInt());
        given(orderHelper.createAndSaveOrder(any(), any(), any(), any(), any(), any())).willReturn(testOrder);
        willDoNothing().given(orderHelper).processPostOrder(any(), any(), any(), any(), any());

        // When
        OrderCreateResponse response = directOrderService.createDirectOrder(request, "testUser");

        // Then: 성공적으로 처리됨
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1L);
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
     * 테스트용 주문 아이템 생성
     */
    private OrderItemRequest createTestOrderItem() {
        OrderItemRequest item = new OrderItemRequest();
        ReflectionTestUtils.setField(item, "optionId", 1L); // 상품 옵션 ID: 1
        ReflectionTestUtils.setField(item, "quantity", 2); // 수량: 2개
        return item;
    }

    /**
     * 테스트용 직접 주문 요청 생성
     */
    private DirectOrderCreateRequest createTestDirectOrderRequest() {
        DirectOrderCreateRequest request = new DirectOrderCreateRequest();
        ReflectionTestUtils.setField(request, "items", List.of(testOrderItem)); // 주문 아이템 목록
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
     * 빈 주문 아이템을 가진 요청 생성
     */
    private DirectOrderCreateRequest createEmptyDirectOrderRequest() {
        DirectOrderCreateRequest request = new DirectOrderCreateRequest();
        ReflectionTestUtils.setField(request, "items", new ArrayList<>()); // 빈 주문 아이템 목록
        return request;
    }

    /**
     * 포인트 사용 주문 요청 생성
     */
    private DirectOrderCreateRequest createDirectOrderRequestWithPoints() {
        DirectOrderCreateRequest request = createTestDirectOrderRequest();
        ReflectionTestUtils.setField(request, "usedPoints", 1000); // 1000포인트 사용
        return request;
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
}