package com.cMall.feedShop.order.domain.repository;

import com.cMall.feedShop.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
