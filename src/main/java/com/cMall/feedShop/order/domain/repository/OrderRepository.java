package com.cMall.feedShop.order.domain.repository;

import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 주문 목록 조회 (주문일 기준 최신순)
    @EntityGraph(attributePaths = {"orderItems", "orderItems.productOption", "orderItems.productOption.product", "orderItems.productImage"})
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 상태 필터링 주문 목록 조회 (주문일 기준 최신순)
    @EntityGraph(attributePaths = {"orderItems", "orderItems.productOption", "orderItems.productOption.product", "orderItems.productImage"})
    Page<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status, Pageable pageable);
}
