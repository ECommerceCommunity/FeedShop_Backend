package com.cMall.feedShop.order.domain.repository;

import com.cMall.feedShop.order.domain.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
} 