package com.cMall.feedShop.order.application.dto;

import com.cMall.feedShop.cart.domain.model.CartItem;
import com.cMall.feedShop.order.application.dto.request.OrderItemRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderItemAdapter는 주문 아이템의 상품 옵션 ID와 주문 수량을 포함하는 DTO 클래스입니다.
 * 이 클래스는 장바구니 아이템이나 주문 아이템 요청을 기반으로 생성될 수 있습니다.
 */
@Getter
@AllArgsConstructor
public class OrderItemData {
    // 상품 옵션 ID
    // ex) "RED, 250, MALE"의 고유 번호
    private Long optionId;

    // 상품 이미지 ID
    private Long imageId;

    // 주문 수량
    // ex) 2개
    private Integer quantity;

    // 장바구니 아이템의 상품 옵션 ID와 주문 수량을 기반으로 OrderItemAdapter 객체를 생성한다.
    public static OrderItemData from(CartItem cartItem) {
        return new OrderItemData(cartItem.getOptionId(), cartItem.getImageId(), cartItem.getQuantity());
    }

    // 직접 주문한 아이템의 상품 옵션 ID와 주문 수량을 기반으로 OrderItemAdapter 객체를 생성한다.
    public static OrderItemData from(OrderItemRequest request) {
        return new OrderItemData(request.getOptionId(), request.getImageId(), request.getQuantity());
    }

    // 장바구니 아이템 목록을 OrderItemAdapter 객체 목록으로 변환한다.
    public static List<OrderItemData> fromCartItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(OrderItemData::from)
                .collect(Collectors.toList());
    }

    // 주문 아이템 요청 목록을 OrderItemAdapter 객체 목록으로 변환한다.
    public static List<OrderItemData> fromOrderItemRequests(List<OrderItemRequest> orderItemRequests) {
        return orderItemRequests.stream()
                .map(OrderItemData::from)
                .collect(Collectors.toList());
    }
}
