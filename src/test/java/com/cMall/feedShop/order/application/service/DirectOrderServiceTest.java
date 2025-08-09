package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.dto.request.DirectOrderCreateRequest;
import com.cMall.feedShop.order.application.dto.request.OrderItemRequest;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.domain.repository.OrderRepository;
import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.domain.enums.*;
import com.cMall.feedShop.product.domain.exception.ProductException;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.cMall.feedShop.order.domain.enums.OrderStatus.ORDERED;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * DirectOrderService 테스트 클래스
 * - 바로 주문 생성 기능을 테스트합니다
 * - 각 테스트는 하나의 시나리오만 검증합니다
 * - 초등학생도 이해할 수 있도록 상세한 주석을 작성했습니다
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("직접 주문 서비스 테스트")
class DirectOrderServiceTest {

    // 테스트할 실제 서비스 (Mock 객체들이 주입됨)
    @InjectMocks
    private DirectOrderService directOrderService;

    // 가짜 객체들 (실제 DB나 외부 서비스 없이 테스트하기 위함)
    @Mock
    private ProductOptionRepository productOptionRepository;
    @Mock
    private DiscountCalculator discountCalculator;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderCommonService orderCommonService;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private UserDetails userDetails;

    // 테스트에서 공통으로 사용할 데이터들
    private User testUser;
    private Product testProduct;
    private ProductOption testProductOption;
    private ProductImage testProductImage;
    private DirectOrderCreateRequest testRequest;
    private OrderItemRequest testOrderItem;
    private Store testStore;
    private Category testCategory;

    /**
     * 각 테스트 실행 전에 공통 테스트 데이터를 준비합니다
     */
    @BeforeEach
    void setUp() {
        // 1. 테스트용 사용자 생성
        testUser = createTestUser();

        // 2. 테스트용 스토어 생성
        testStore = createTestStore();

        // 3. 테스트용 카테고리 생성
        testCategory = createTestCategory();

        // 4. 테스트용 상품 생성
        testProduct = createTestProduct();

        // 5. 테스트용 상품 옵션 생성
        testProductOption = createTestProductOption();

        // 6. 테스트용 상품 이미지 생성
        testProductImage = createTestProductImage();

        // 7. 테스트용 주문 아이템 생성
        testOrderItem = createTestOrderItemRequest();

        // 8. 테스트용 주문 요청 생성
        testRequest = createTestDirectOrderRequest();
    }

    /**
     * 정상적인 직접 주문 생성 테스트
     * - 모든 조건이 정상일 때 주문이 성공적으로 생성되는지 확인
     */
    @Test
    @DisplayName("직접 주문 생성 성공")
    void createDirectOrder_Success() {
        // Given (테스트 준비): Mock 객체들의 동작을 정의

        // 사용자 검증이 성공한다고 가정
        given(orderCommonService.validateUser(userDetails)).willReturn(testUser);

        // 상품 옵션 조회가 성공한다고 가정
        given(productOptionRepository.findAllByOptionIdIn(any(Set.class)))
                .willReturn(List.of(testProductOption));

        // 상품 이미지 조회가 성공한다고 가정
        given(productImageRepository.findFirstImagesByProductIds(any(Set.class)))
                .willReturn(List.of(testProductImage));

        // 할인 계산이 정상 동작한다고 가정 (할인 없음)
        given(discountCalculator.calculateDiscountPrice(any(), any(), any()))
                .willReturn(BigDecimal.valueOf(50000)); // 5만원

        // 포인트 계산이 정상 동작한다고 가정
        given(orderCommonService.calculateActualUsedPoints(any(), any()))
                .willReturn(0); // 포인트 사용 안함
        given(orderCommonService.calculateFinalAmount(any(), any()))
                .willReturn(BigDecimal.valueOf(50000)); // 최종 금액 5만원
        given(orderCommonService.calculateEarnedPoints(any()))
                .willReturn(250); // 250포인트 적립

        // 주문 저장이 성공한다고 가정
        Order mockOrder = createMockOrder();
        given(orderRepository.save(any(Order.class))).willReturn(mockOrder);

        // When (테스트 실행): 실제 메서드 호출
        OrderCreateResponse response = directOrderService.createDirectOrder(testRequest, userDetails);

        // Then (결과 검증): 예상한 결과가 나왔는지 확인
        assertThat(response).isNotNull(); // 응답이 null이 아님
        assertThat(response.getOrderId()).isEqualTo(1L); // 주문 ID가 1
        assertThat(response.getStatus()).isEqualTo(ORDERED); // 주문 상태가 ORDERED

        // Mock 메서드들이 올바르게 호출되었는지 검증
        verify(orderCommonService).validateUser(userDetails); // 사용자 검증 호출됨
        verify(productOptionRepository).findAllByOptionIdIn(any()); // 상품 옵션 조회 호출됨
        verify(productImageRepository).findFirstImagesByProductIds(any()); // 이미지 조회 호출됨
        verify(orderCommonService).validatePointUsage(any(), any()); // 포인트 검증 호출됨
        verify(orderCommonService).processUserPoints(any(), any(), any()); // 포인트 처리 호출됨
        verify(orderRepository).save(any(Order.class)); // 주문 저장 호출됨
        verify(productOptionRepository).saveAll(any()); // 재고 차감 저장 호출됨
    }

    /**
     * 주문 아이템이 비어있을 때 예외 발생 테스트
     */
    @Test
    @DisplayName("주문 아이템이 비어있으면 예외 발생")
    void createDirectOrder_EmptyItems_ThrowsException() {
        // Given: 빈 주문 아이템 리스트를 가진 요청 생성
        DirectOrderCreateRequest emptyRequest = createEmptyDirectOrderRequest();
        given(orderCommonService.validateUser(userDetails)).willReturn(testUser);

        // When & Then: 예외가 발생하는지 확인
        assertThatThrownBy(() -> directOrderService.createDirectOrder(emptyRequest, userDetails))
                .isInstanceOf(OrderException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_ITEM_NOT_FOUND);
    }

    /**
     * 존재하지 않는 상품 옵션 ID로 주문 시 예외 발생 테스트
     */
    @Test
    @DisplayName("존재하지 않는 상품 옵션으로 주문 시 예외 발생")
    void createDirectOrder_ProductOptionNotFound_ThrowsException() {
        // Given: 상품 옵션이 존재하지 않는다고 가정
        given(orderCommonService.validateUser(userDetails)).willReturn(testUser);
        given(productOptionRepository.findAllByOptionIdIn(any(Set.class)))
                .willReturn(Collections.emptyList()); // 빈 리스트 반환

        // When & Then: 예외가 발생하는지 확인
        assertThatThrownBy(() -> directOrderService.createDirectOrder(testRequest, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_OPTION_NOT_FOUND);
    }

    /**
     * 재고가 부족할 때 예외 발생 테스트
     */
    @Test
    @DisplayName("재고 부족 시 예외 발생")
    void createDirectOrder_OutOfStock_ThrowsException() {
        // Given: 재고가 부족한 상품 옵션 생성
        ProductOption outOfStockOption = createOutOfStockProductOption();

        given(orderCommonService.validateUser(userDetails)).willReturn(testUser);
        given(productOptionRepository.findAllByOptionIdIn(any(Set.class)))
                .willReturn(List.of(outOfStockOption));

        // When & Then: 재고 부족 예외가 발생하는지 확인
        assertThatThrownBy(() -> directOrderService.createDirectOrder(testRequest, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OUT_OF_STOCK);
    }

    /**
     * 상품 이미지가 없을 때 예외 발생 테스트
     */
    @Test
    @DisplayName("상품 이미지가 없으면 예외 발생")
    void createDirectOrder_ProductImageNotFound_ThrowsException() {
        // Given: 상품 이미지가 존재하지 않는다고 가정
        given(orderCommonService.validateUser(userDetails)).willReturn(testUser);
        given(productOptionRepository.findAllByOptionIdIn(any(Set.class)))
                .willReturn(List.of(testProductOption));
        given(productImageRepository.findFirstImagesByProductIds(any(Set.class)))
                .willReturn(Collections.emptyList()); // 빈 리스트 반환

        // When & Then: 예외가 발생하는지 확인
        assertThatThrownBy(() -> directOrderService.createDirectOrder(testRequest, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
    }

    /**
     * 포인트 사용 시 정상 처리 테스트
     */
    @Test
    @DisplayName("포인트 사용하여 주문 성공")
    void createDirectOrder_WithPoints_Success() {
        // Given: 포인트를 사용하는 주문 요청 생성
        DirectOrderCreateRequest requestWithPoints = createDirectOrderRequestWithPoints();

        given(orderCommonService.validateUser(userDetails)).willReturn(testUser);
        given(productOptionRepository.findAllByOptionIdIn(any(Set.class)))
                .willReturn(List.of(testProductOption));
        given(productImageRepository.findFirstImagesByProductIds(any(Set.class)))
                .willReturn(List.of(testProductImage));
        given(discountCalculator.calculateDiscountPrice(any(), any(), any()))
                .willReturn(BigDecimal.valueOf(50000));

        // 포인트 계산 로직
        given(orderCommonService.calculateActualUsedPoints(any(), eq(1000)))
                .willReturn(1000); // 1000포인트 사용
        given(orderCommonService.calculateFinalAmount(any(), eq(1000)))
                .willReturn(BigDecimal.valueOf(49000)); // 포인트 차감 후 49000원
        given(orderCommonService.calculateEarnedPoints(any()))
                .willReturn(245); // 245포인트 적립

        Order mockOrder = createMockOrder();
        given(orderRepository.save(any(Order.class))).willReturn(mockOrder);

        // When: 포인트 사용 주문 실행
        OrderCreateResponse response = directOrderService.createDirectOrder(requestWithPoints, userDetails);

        // Then: 성공적으로 처리되었는지 확인
        assertThat(response).isNotNull();
        verify(orderCommonService).validatePointUsage(testUser, 1000); // 포인트 검증 호출됨
        verify(orderCommonService).processUserPoints(eq(testUser), eq(1000), eq(245)); // 포인트 처리 호출됨
    }

    // =========================
    // 테스트 데이터 생성 메서드들
    // =========================

    /**
     * 테스트용 사용자 생성
     */
    private User createTestUser() {
        User user = new User("testUser", "password123", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    /**
     * 테스트용 스토어 생성
     */
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

    /**
     * 테스트용 카테고리 생성
     */
    private Category createTestCategory() {
        Category category = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(category, "categoryId", 1L);
        return category;
    }

    /**
     * 테스트용 상품 생성
     */
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

    /**
     * 테스트용 상품 옵션 생성 (재고 충분)
     */
    private ProductOption createTestProductOption() {
        ProductOption option = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 100, testProduct);
        ReflectionTestUtils.setField(option, "optionId", 1L);
        return option;
    }

    /**
     * 재고가 부족한 상품 옵션 생성
     */
    private ProductOption createOutOfStockProductOption() {
        ProductOption option = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.WHITE, 1, testProduct);
        ReflectionTestUtils.setField(option, "optionId", 1L);
        return option;
    }

    /**
     * 테스트용 상품 이미지 생성
     */
    private ProductImage createTestProductImage() {
        ProductImage image = new ProductImage("http://test-image.jpg", ImageType.MAIN, testProduct);
        ReflectionTestUtils.setField(image, "imageId", 1L);
        return image;
    }

    /**
     * 테스트용 주문 아이템 요청 생성
     */
    private OrderItemRequest createTestOrderItemRequest() {
        OrderItemRequest item = new OrderItemRequest();
        ReflectionTestUtils.setField(item, "optionId", 1L);
        ReflectionTestUtils.setField(item, "quantity", 2); // 수량 2개
        return item;
    }

    /**
     * 테스트용 직접 주문 요청 생성
     */
    private DirectOrderCreateRequest createTestDirectOrderRequest() {
        DirectOrderCreateRequest request = new DirectOrderCreateRequest();
        ReflectionTestUtils.setField(request, "items", List.of(testOrderItem));
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

    /**
     * 빈 주문 아이템을 가진 요청 생성
     */
    private DirectOrderCreateRequest createEmptyDirectOrderRequest() {
        DirectOrderCreateRequest request = new DirectOrderCreateRequest();
        ReflectionTestUtils.setField(request, "items", new ArrayList<>());
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
     * Mock 주문 객체 생성
     */
    private Order createMockOrder() {
        Order order = Order.builder()
                .user(testUser)
                .status(ORDERED)
                .totalPrice(BigDecimal.valueOf(50000))
                .finalPrice(BigDecimal.valueOf(53000)) // 배송비 포함
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
}