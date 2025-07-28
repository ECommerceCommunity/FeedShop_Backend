package com.cMall.feedShop.order.domain.repository;

import com.cMall.feedShop.order.domain.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
