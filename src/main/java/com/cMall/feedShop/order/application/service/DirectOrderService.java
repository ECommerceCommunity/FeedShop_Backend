package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.calculator.OrderCalculation;
import com.cMall.feedShop.order.application.dto.request.DirectOrderCreateRequest;
import com.cMall.feedShop.order.application.dto.request.OrderItemRequest;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
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
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
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
public class DirectOrderService {

    private final ProductOptionRepository productOptionRepository;
    private final DiscountCalculator discountCalculator;
    private final OrderRepository orderRepository;
    private final OrderCommonService orderCommonService;
    private final ProductImageRepository productImageRepository;

    /**
     * 직접 주문 생성 (장바구니 없이 상품을 직접 선택하여 주문)
     * @param request 직접 주문 생성 요청 정보
     * @param userDetails 현재 로그인된 사용자 정보
     * @return 주문 생성 응답 정보
     */
    @Transactional
    public OrderCreateResponse createDirectOrder(DirectOrderCreateRequest request, UserDetails userDetails) {
        // 1. 현재 사용자 조회를 하고 사용자 권한을 검증
        User currentUser = orderCommonService.validateUser(userDetails);

        // 2. 주문 아이템 목록을 조회
        List<OrderItemRequest> orderItemRequests = request.getItems();
        validateDirectOrderItems(orderItemRequests);

        // 3. 주문할 상품 정보를 조회하고 검증한다
        Map<Long, ProductOption> optionMap = validateDirectOrderProductOptions(orderItemRequests);
        Map<Long, ProductImage> imageMap = getDirectProductImages(optionMap);

        // 4. 주문 금액 계산
        OrderCalculation calculation = calculateDirectOrderAmount(orderItemRequests, optionMap, request.getUsedPoints());

        // 5. 포인트 사용 가능 여부 확인
        orderCommonService.validatePointUsage(currentUser, calculation.getActualUsedPoints());

        // 6. 주문 및 주문 아이템 생성
        Order order = createAndSaveDirectOrder(currentUser, request, calculation, orderItemRequests, optionMap, imageMap);

        // 7. 재고 차감
        processPostDirectOrder(currentUser, orderItemRequests, optionMap, calculation);

        // 8. 주문 생성 응답 반환
        return OrderCreateResponse.from(order);
    }

    // 상품 옵션 조회 및 재고 확인 (직접 주문용)
    private Map<Long, ProductOption> validateDirectOrderProductOptions(List<OrderItemRequest> items) {
        // 요청에서 옵션 ID를 추출하여 중복 제거
        Set<Long> optionIds = items.stream()
                .map(OrderItemRequest::getOptionId)
                .collect(Collectors.toSet());

        // DB 에서 옵션 ID로 상품 옵션을 조회한다.
        List<ProductOption> options = productOptionRepository.findAllByOptionIdIn(optionIds);
        if (options.size() != optionIds.size()) {
            throw new ProductException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }

        Map<Long, ProductOption> optionMap = options.stream()
                .collect(Collectors.toMap(ProductOption::getOptionId, Function.identity()));

        // 재고 검증
        validateDirectOrderStock(items, optionMap);

        return optionMap;
    }

    private void validateDirectOrderItems(List<OrderItemRequest> items) {
        if (items.isEmpty()) {
            throw new OrderException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }
    }

    private Map<Long, ProductImage> getDirectProductImages(Map<Long, ProductOption> optionMap) {
        // 상품 옵션들에서 상품 ID만 추출하기
        Set<Long> productIds = optionMap.values().stream()
                .map(option -> option.getProduct().getProductId())
                .collect(Collectors.toSet());

        // 상품 ID 들로 메인 이미지들을 한 번에 조회
        List<ProductImage> mainImages = productImageRepository.findFirstImagesByProductIds(productIds);

        // 메인 이미지가 없는 상품이 있다면 예외 발생
        if (mainImages.size() != productIds.size()) {
            throw new ProductException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        }

        // 조회한 이미지들을 상품 ID 를 키로 하는 맵으로 변환
        return mainImages.stream()
                .collect(Collectors.toMap(
                        image -> image.getProduct().getProductId(),
                        Function.identity()));
    }

    // 재고를 확인한다. (직접 주문용)
    private void validateDirectOrderStock(List<OrderItemRequest> items, Map<Long, ProductOption> optionMap) {
        // 각 주문 아이템마다 재고를 확인한다.
        for (OrderItemRequest item : items) {
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
    private OrderCalculation calculateDirectOrderAmount(List<OrderItemRequest> items, Map<Long, ProductOption> optionMap, Integer usedPoints) {
        // 총 상품 금액 계산
        BigDecimal totalAmount = calculateDirectTotalAmount(items, optionMap);

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

    private BigDecimal calculateDirectTotalAmount(List<OrderItemRequest> items, Map<Long, ProductOption> optionMap) {
        return items.stream()
                .map(item -> {
                    // 장바구니 아이템에서 옵션 ID로 상품을 조회한다.
                    ProductOption option = optionMap.get(item.getOptionId());
                    Product product = option.getProduct();

                    // 상품의 할인된 가격을 계산해서 알아낸다.
                    BigDecimal discountPrice = discountCalculator.calculateDiscountPrice(
                            product.getPrice(),
                            product.getDiscountType(),
                            product.getDiscountValue()
                    );

                    // 할인된 가격에 장바구니 아이템의 수량을 곱한다.
                    return discountPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                // 수량이 곱해진 할인된 가격을 더한다.
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order createAndSaveDirectOrder(User user, DirectOrderCreateRequest request, OrderCalculation calculation,
                                           List<OrderItemRequest> items, Map<Long, ProductOption> optionMap, Map<Long, ProductImage> imageMap) {
        // 주문 Entity 생성
        Order order = createDirectOrderEntity(user, request, calculation);

        // 주문 아이템 생성
        createDirectOrderItems(order, items, optionMap, imageMap);

        // 주문 DB 저장
        return orderRepository.save(order);
    }

    // 주문 Entity 생성
    private Order createDirectOrderEntity(User user, DirectOrderCreateRequest request, OrderCalculation calculation) {
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

    // 주문의 주문 아이템 Entity로 만든다.
    private void createDirectOrderItems(Order order, List<OrderItemRequest> items, Map<Long, ProductOption> optionMap, Map<Long, ProductImage> imageMap) {
        for (OrderItemRequest item : items) {
            ProductOption option = optionMap.get(item.getOptionId());
            Product product = option.getProduct();

            ProductImage image = imageMap.get(product.getProductId());

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
                    .quantity(item.getQuantity())
                    .totalPrice(totalPrice)
                    .finalPrice(finalPrice)
                    .build();

            order.addOrderItem(orderItem);
        }
    }

    private void processPostDirectOrder(User user, List<OrderItemRequest> items, Map<Long, ProductOption> optionMap, OrderCalculation calculation) {
        // 재고 차감
        decreaseDirectOrderStock(items, optionMap);

        // 포인트 처리
        orderCommonService.processUserPoints(user, calculation.getActualUsedPoints(), calculation.getEarnedPoints());
    }

    // 재고 차감
    private void decreaseDirectOrderStock(List<OrderItemRequest> items, Map<Long, ProductOption> optionMap) {
        // 모든 요청 주문 아이템들의 option을 조회한다.
        // option의 stock 에서 요청 주문 아이템의 quantity를 뺀다.
        List<ProductOption> optionsToUpdate = items.stream()
                .map(item -> {
                    ProductOption option = optionMap.get(item.getOptionId());
                    option.decreaseStock(item.getQuantity());
                    return option;
                }).toList();

        productOptionRepository.saveAll(optionsToUpdate);
    }
}
