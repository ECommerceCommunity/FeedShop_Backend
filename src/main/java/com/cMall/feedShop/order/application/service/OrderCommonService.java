package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.dto.OrderItemData;
import com.cMall.feedShop.order.application.dto.OrderRequestData;
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
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.application.service.PointService;
import com.cMall.feedShop.user.domain.model.UserPoint;
import com.cMall.feedShop.user.domain.repository.UserPointRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cMall.feedShop.order.application.constants.OrderConstants.*;

/**
 * 주문 관련 공통 서비스
 * - 사용자 검증, 포인트 사용 및 적립 처리 등을 담당
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderCommonService {

    private final UserRepository userRepository;
    private final UserPointRepository userPointRepository;
    private final PointService pointService;
    private final ProductOptionRepository productOptionRepository;
    private final DiscountCalculator discountCalculator;
    private final ProductImageRepository productImageRepository;
    private final OrderRepository orderRepository;

    /**
     * 사용자 검증
     * - 로그인 ID로 사용자 조회
     * - 사용자 역할이 USER인지 확인
     * - USER가 아닌 경우 예외 처리
     *
     * @param loginId Spring Security의 UserDetails 객체
     * @return 검증된 User 객체
     */
    public User validateUser(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new OrderException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.USER) {
            throw new OrderException(ErrorCode.ORDER_FORBIDDEN);
        }

        return user;
    }

    /**
     * 사용자 포인트 조회
     * - 주문을 요청한 사용자의 포인트 정보를 조회
     *
     * @param user 주문을 요청한 사용자 정보
     */
    public void validatePointUsage(User user, Integer usedPoints) {
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

    /**
     * 사용자 포인트 조회
     * - 사용자가 존재하지 않을 경우 새로 생성
     *
     * @param user 주문을 요청한 사용자 정보
     * @return 사용자 포인트 정보
     */
    private UserPoint getUserPoint(User user) {
        return userPointRepository.findByUser(user)
                .orElse(UserPoint.builder()
                        .user(user)
                        .currentPoints(0)
                        .build());
    }

    /**
     * 주문 아이템 어댑터를 통해 유효한 상품 옵션들을 조회하고 검증한다.
     * - 어댑터에서 옵션 ID를 가져와 DB 에서 조회
     * - 재고를 확인하고, 존재하지 않는 옵션은 예외 처리
     *
     * @param adapters 주문 아이템 어댑터 리스트
     * @return 유효한 상품 옵션 ID와 ProductOption 객체의 매핑
     */
    public Map<Long, ProductOption> getValidProductOptions(List<OrderItemData> adapters) {
        // 1. 모든 어댑터에서 상품 옵션 ID를 가져온다.
        Set<Long> optionIds = adapters.stream()
                .map(OrderItemData::getOptionId)
                .collect(Collectors.toSet());

        // 2. DB(ProductOption) 에서 해당 옵션 ID 들을 조회한다.
        List<ProductOption> options = productOptionRepository.findAllByOptionIdIn(optionIds);

        // 3. 조회한 옵션들이 정확히 존재하는지 확인한다.
        if (options.size() != optionIds.size()) {
            throw new OrderException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }

        // 4. 옵션 ID와 ProductOption 객체를 매핑한다.
        Map<Long, ProductOption> optionMap = options.stream()
                .collect(Collectors.toMap(ProductOption::getOptionId, Function.identity()));

        // 4. 재고를 확인한다.
        validateStock(adapters, optionMap);

        return optionMap;
    }

    /**
     * 주문 아이템 어댑터를 통해 상품 이미지들을 조회한다.
     * - 어댑터에서 이미지 ID를 가져와 DB 에서 조회
     * - 존재하지 않는 이미지는 예외 처리
     *
     * @param adapters 주문 아이템 어댑터 리스트
     * @return 유효한 상품 이미지 ID와 ProductImage 객체의 매핑
     */
    public Map<Long, ProductImage> getProductImages(List<OrderItemData> adapters) {
        Set<Long> imageIds = adapters.stream()
                .map(OrderItemData::getImageId)
                .collect(Collectors.toSet());

        return productImageRepository.findAllById(imageIds).stream()
                .collect(Collectors.toMap(ProductImage::getImageId, Function.identity()));
    }

    /**
     * 주문 아이템 어댑터 리스트와 상품 옵션 맵을 통해 재고를 검증한다.
     * - 각 주문 아이템의 수량이 해당 상품 옵션의 재고보다 적은지 확인
     * - 재고가 부족한 경우 예외 처리
     *
     * @param adapters 주문 아이템 어댑터 리스트
     * @param optionMap 유효한 상품 옵션 ID와 ProductOption 객체의 매핑
     */
    private void validateStock(List<OrderItemData> adapters, Map<Long, ProductOption> optionMap) {
        // 각 주문 아이템마다 재고를 확인한다.
        for (OrderItemData adapter : adapters) {
            // 주문 아이템에 해당하는 상품 옵션을 가져온다.
            ProductOption option = optionMap.get(adapter.getOptionId());
            // 재고가 없거나 주문 수량보다 적은 경우
            if (!option.isInStock() || option.getStock() < adapter.getQuantity()) {
                throw new ProductException(ErrorCode.OUT_OF_STOCK,
                        String.format("상품 '%s'의 재고가 부족합니다. 현재 재고: %d, 요청 수량: %d",
                                option.getProduct().getName(), option.getStock(), adapter.getQuantity()));
            }
        }
    }

    /**
     * 주문 금액을 계산한다.
     * - 총 상품 금액, 실제 사용 가능한 포인트, 최종 금액, 적립 포인트를 계산하여 OrderCalculation 객체로 반환
     *
     * @param adapters 주문 아이템 어댑터 리스트
     * @param optionMap 유효한 상품 옵션 ID와 ProductOption 객체의 매핑
     * @param usedPoints 사용자가 요청한 포인트
     * @return OrderCalculation 객체
     */
    public OrderCalculation calculateOrderAmount(List<OrderItemData> adapters, Map<Long, ProductOption> optionMap, Integer usedPoints) {
        // 총 상품 금액 계산
        BigDecimal totalAmount = calculateTotalAmount(adapters, optionMap);

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

    // 총 금액을 계산한다.
    private BigDecimal calculateTotalAmount(List<OrderItemData> adapters, Map<Long, ProductOption> optionMap) {
        return adapters.stream()
                .map(adapter -> {
                    // 장바구니 아이템에서 옵션 ID로 상품을 조회한다.
                    ProductOption option = optionMap.get(adapter.getOptionId());
                    Product product = option.getProduct();

                    // 상품의 할인된 가격을 계산해서 알아낸다.
                    BigDecimal discountPrice = discountCalculator.calculateDiscountPrice(
                            product.getPrice(),
                            product.getDiscountType(),
                            product.getDiscountValue()
                    );

                    // 할인된 가격에 장바구니 아이템의 수량을 곱한다.
                    return discountPrice.multiply(BigDecimal.valueOf(adapter.getQuantity()));
                })
                // 수량이 곱해진 할인된 가격을 더한다.
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 실제 사용 가능한 포인트를 계산한다.
    public Integer calculateActualUsedPoints(BigDecimal totalAmount, Integer requestedPoints) {
        if (requestedPoints == null || requestedPoints <= 0) {
            return 0;
        }

        BigDecimal maxPointUsage = totalAmount.multiply(POINT_USAGE_RATE)
                .setScale(0, RoundingMode.DOWN);

        BigDecimal requestedPointAmount = BigDecimal.valueOf(requestedPoints);

        return requestedPointAmount.compareTo(maxPointUsage) <= 0
                ? requestedPoints
                : maxPointUsage.intValue();
    }

    // 최종 금액을 계산한다.
    private BigDecimal calculateFinalAmount(BigDecimal totalAmount, Integer usedPoints) {
        BigDecimal pointDeduction = BigDecimal.valueOf(usedPoints);
        BigDecimal finalAmount = totalAmount.subtract(pointDeduction);

        return finalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmount;
    }

    // 적립 포인트 계산
    public Integer calculateEarnedPoints(BigDecimal finalAmount) {
        if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal units = finalAmount.divide(POINT_REWARD_THRESHOLD, 0, RoundingMode.DOWN);

        // 10,000원 단위로 50 포인트를 적립한다.
        return units.multiply(POINT_REWARD_AMOUNT).intValue();
    }

    /**
     * 주문을 생성하고 저장한다.
     * - 주문 Entity를 생성하고, 주문 아이템을 추가한 후 DB에 저장
     *
     * @param user 주문을 요청한 사용자 정보
     * @param request 주문 생성 요청 정보
     * @param calculation 주문 금액 계산 결과
     * @param adapters 주문 아이템 어댑터 리스트
     * @param optionMap 유효한 상품 옵션 ID와 ProductOption 객체의 매핑
     * @param imageMap 유효한 상품 이미지 ID와 ProductImage 객체의 매핑
     * @return 저장된 Order 객체
     */
    public Order createAndSaveOrder(User user, OrderRequestData request, OrderCalculation calculation,
                                    List<OrderItemData> adapters, Map<Long, ProductOption> optionMap, Map<Long, ProductImage> imageMap) {
        // 주문 Entity 생성
        Order order = createOrderEntity(user, request, calculation);

        // 주문 아이템 생성
        createOrderItems(order, adapters, optionMap, imageMap);

        // 주문 DB 저장
        return orderRepository.save(order);
    }

    /**
     * 주문 Entity를 생성한다.
     * - 주문 상태, 총 금액, 최종 금액, 배송비, 사용 포인트, 적립 포인트 등을 설정
     *
     * @param user 주문을 요청한 사용자 정보
     * @param request 주문 생성 요청 정보
     * @param calculation 주문 금액 계산 결과
     * @return 생성된 Order 객체
     */
    private Order createOrderEntity(User user, OrderRequestData request, OrderCalculation calculation) {
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

    /**
     * 주문 아이템을 생성하고 주문에 추가한다.
     * - 각 어댑터에서 상품 옵션과 이미지 정보를 가져와 주문 아이템을 생성
     * - 주문에 아이템을 추가
     *
     * @param order 주문 객체
     * @param adapters 주문 아이템 어댑터 리스트
     * @param optionMap 유효한 상품 옵션 ID와 ProductOption 객체의 매핑
     * @param imageMap 유효한 상품 이미지 ID와 ProductImage 객체의 매핑
     */
    private void createOrderItems(Order order, List<OrderItemData> adapters, Map<Long, ProductOption> optionMap, Map<Long, ProductImage> imageMap) {
        for (OrderItemData adapter : adapters) {
            ProductOption option = optionMap.get(adapter.getOptionId());
            ProductImage image = imageMap.get(adapter.getImageId());
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
                    .quantity(adapter.getQuantity())
                    .totalPrice(totalPrice)
                    .finalPrice(finalPrice)
                    .build();

            order.addOrderItem(orderItem);
        }
    }

    /**
     * 주문 후 처리 로직
     * - 재고 차감, 사용자 포인트 사용 및 적립 처리
     *
     * @param user 주문을 요청한 사용자 정보
     * @param adapters 주문 아이템 어댑터 리스트
     * @param optionMap 유효한 상품 옵션 ID와 ProductOption 객체의 매핑
     * @param calculation 주문 금액 계산 결과
     * @param orderId 주문 ID (포인트 거래 내역 연결용)
     */
    public void processPostOrder(User user, List<OrderItemData> adapters, Map<Long, ProductOption> optionMap, OrderCalculation calculation, Long orderId) {
        // 재고 차감
        decreaseStock(adapters, optionMap);

        // 포인트 처리
        processUserPoints(user, calculation.getActualUsedPoints(), calculation.getEarnedPoints(), orderId);
    }

    /**
     * 사용자 포인트 사용 및 적립 처리
     * - 주문 완료 후 사용자 포인트를 사용하고 적립하는 로직
     *
     * @param user 주문을 요청한 사용자 정보
     * @param usedPoints 사용한 포인트
     * @param earnedPoints 적립한 포인트
     * @param orderId 주문 ID (포인트 거래 내역 연결용)
     */
    private void processUserPoints(User user, Integer usedPoints, Integer earnedPoints, Long orderId) {
        // 포인트 사용
        if (usedPoints != null && usedPoints > 0) {
            pointService.usePoints(user, usedPoints, "주문 결제", orderId);
        }

        // 포인트 적립
        if (earnedPoints != null && earnedPoints > 0) {
            pointService.earnPoints(user, earnedPoints, "주문 적립", orderId);
        }
    }

    // 재고 차감
    private void decreaseStock(List<OrderItemData> adapters, Map<Long, ProductOption> optionMap) {
        List<ProductOption> optionsToUpdate = adapters.stream()
                .map(adapter -> {
                    ProductOption option = optionMap.get(adapter.getOptionId());
                    option.decreaseStock(adapter.getQuantity());
                    return option;
                }).toList();

        productOptionRepository.saveAll(optionsToUpdate);
    }
}
