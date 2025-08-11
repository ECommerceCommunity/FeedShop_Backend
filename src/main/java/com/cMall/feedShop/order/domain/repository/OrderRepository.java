package com.cMall.feedShop.order.domain.repository;

import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.order.infrastructure.repository.OrderQueryRepository;
import com.cMall.feedShop.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderQueryRepository {
    // 내가 주문한 주문 을 모두 찾는다. 목록 조회 (주문일 기준 최신순)
    default Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable) {
        return findByUserOrderByCreatedAtDesc(user.getId(), pageable);
    }

    // 내가 주문한 주문 + 특정 주문 상태인 것만 필터링해서 모두 찾는다. 목록 조회 (주문일 기준 최신순으로 정렬한다)
    default Page<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status, Pageable pageable) {
        return findByUserAndStatusOrderByCreatedAtDesc(user.getId(), status, pageable);
    }

    // 판매자 ID와 주문 ID로 주문을 찾는다.
    default Optional<Order> findByOrderIdAndSeller(Long orderId, User seller) {
        return findByOrderIdAndSellerId(orderId, seller.getId());
    }

    // 유저 ID로 주문 ID로 주문을 찾는다.
    default Optional<Order> findByOrderIdAndUser(Long orderId, User user) {
        return findByOrderIdAndUserId(orderId, user.getId());
    }
}
