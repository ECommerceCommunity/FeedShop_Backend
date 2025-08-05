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

    // 사용자별 특정 주문 조회 (EntityGraph 대체)
    Optional<Order> findByOrderIdAndUserId(Long orderId, Long userId);

    // 주문 ID와 판매자 ID로 주문 조회 (판매자 권한 검증용)
    Optional<Order> findByOrderIdAndSellerId(Long orderId, Long sellerId);
}