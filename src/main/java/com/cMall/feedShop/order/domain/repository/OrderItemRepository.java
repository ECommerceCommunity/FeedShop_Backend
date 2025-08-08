package com.cMall.feedShop.order.domain.repository;

import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.product.domain.model.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 특정 상품 옵션이 주문에 포함되어 있는지 확인
    boolean existsByProductOption(ProductOption productOption);
}