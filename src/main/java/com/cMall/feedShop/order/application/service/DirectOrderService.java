package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.dto.OrderItemData;
import com.cMall.feedShop.order.application.dto.OrderRequestData;
import com.cMall.feedShop.order.application.calculator.OrderCalculation;
import com.cMall.feedShop.order.application.dto.request.DirectOrderCreateRequest;
import com.cMall.feedShop.order.application.dto.request.OrderItemRequest;
import com.cMall.feedShop.order.application.dto.response.OrderCreateResponse;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.cMall.feedShop.order.application.constants.OrderConstants.MAX_ORDER_QUANTITY;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectOrderService {

    private final OrderCommonService orderCommonService;

    /**
     * 직접 주문 생성 (장바구니 없이 상품을 직접 선택하여 주문)
     * @param request 직접 주문 생성 요청 정보
     * @param userDetails 현재 로그인된 사용자 정보
     * @return 주문 생성 응답 정보
     */
    @Transactional
    public OrderCreateResponse createDirectOrder(DirectOrderCreateRequest request, String loginId) {
        // 1. 현재 사용자 조회를 하고 사용자 권한을 검증
        User currentUser = orderCommonService.validateUser(loginId);

        // 2. 주문 아이템 목록을 조회
        List<OrderItemRequest> orderItemRequests = request.getItems();
        validateOrderItems(orderItemRequests);

        // 3. 어댑터로 변환해서 OrderCommonService 사용
        List<OrderItemData> adapters = OrderItemData.fromOrderItemRequests(orderItemRequests);
        Map<Long, ProductOption> optionMap = orderCommonService.getValidProductOptions(adapters);
        Map<Long, ProductImage> imageMap = orderCommonService.getProductImages(adapters);

        // 4. 주문 금액 계산
        OrderCalculation calculation = orderCommonService.calculateOrderAmount(adapters, optionMap, request.getUsedPoints());

        // 5. 포인트 사용 가능 여부 확인
        orderCommonService.validatePointUsage(currentUser, calculation.getActualUsedPoints());

        // 6. 주문 및 주문 아이템 생성
        Order order = orderCommonService.createAndSaveOrder(currentUser, OrderRequestData.from(request), calculation, adapters, optionMap, imageMap);

        // 7. 재고 차감
        orderCommonService.processPostOrder(currentUser, adapters, optionMap, calculation);

        // 8. 주문 생성 응답 반환
        return OrderCreateResponse.from(order);
    }

    // 주문 아이템이 비어있는지 검증 (직접 주문용)
    private void validateOrderItems(List<OrderItemRequest> items) {
        if (items.isEmpty()) {
            throw new OrderException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // 각 아이템별 검증
        for (OrderItemRequest item : items) {
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
}
