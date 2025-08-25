package com.cMall.feedShop.order.infrastructure.repository;

import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderQueryRepository {
    // 내 가게 주문 목록 조회
    Page<Order> findOrdersBySellerId(Long sellerId, Pageable pageable);

    // 내 가게 주문 목록 조회 (상태별) - OrderStatus 타입으로 수정
    Page<Order> findOrdersBySellerIdAndStatus(Long sellerId, OrderStatus status, Pageable pageable);

    // 사용자별 주문 목록 조회 (EntityGraph 대체)
    Page<Order> findByUserOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 사용자별 + 상태별 주문 목록 조회 (EntityGraph 대체)
    Page<Order> findByUserAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status, Pageable pageable);

    // 주문 ID와 유저 ID로 주문 조회
    Optional<Order> findByOrderIdAndUserId(Long orderId, Long userId);

    // 주문 ID와 판매자 ID로 주문 조회
    Optional<Order> findByOrderIdAndSellerId(Long orderId, Long sellerId);
    
    // 특정 사용자의 특정 상태 주문 개수 조회
    Long countByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);
    
    // 특정 사용자의 총 주문 금액 조회 (DELIVERED 상태만)
    Long findTotalOrderAmountByUserId(Long userId);
}