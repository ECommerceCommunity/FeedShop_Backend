package com.cMall.feedShop.order.domain.repository;

import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 내가 주문한 주문 을 모두 찾는다. 목록 조회 (주문일 기준 최신순)
    // order 와 관계된 orderItem(N) - (1)productOption(1) - (N)product
    // order 와 관계된 orderItem(N) - (1)productImage
    // 도 같이 조회한다.
    @EntityGraph(attributePaths = {"orderItems", "orderItems.productOption", "orderItems.productOption.product", "orderItems.productImage"})
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 내가 주문한 주문 + 특정 주문 상태인 것만 필터링해서 모두 찾는다. 목록 조회 (주문일 기준 최신순으로 정렬한다)
    // order 와 관계된 orderItem(N) - (1)productOption(1) - (N)product
    // order 와 관계된 orderItem(N) - (1)productImage
    // 도 같이 조회한다.
    @EntityGraph(attributePaths = {"orderItems", "orderItems.productOption", "orderItems.productOption.product", "orderItems.productImage"})
    Page<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status, Pageable pageable);

    // 특정 주문 ID에 해당하는 주문 + 내가 주문한 주문 을 찾는다. 단일 조회.
    // order 와 관계된 orderItem(N) - (1)productOption(1) - (N)product
    // order 와 관계된 orderItem(N) - (1)productImage
    // 도 같이 조회한다.
    @EntityGraph(attributePaths = {"orderItems", "orderItems.productOption", "orderItems.productOption.product", "orderItems.productImage"})
    Optional<Order> findByOrderIdAndUser(Long orderId, User user);
}
