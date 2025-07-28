package com.cMall.feedShop.order.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class OrderItem {
    @Id
    private Long orderItemId;
    private Long orderId;
    private Long optionId;
    private Long imageId;
    private Integer quantity;
    private BigDecimal discountPrice;
    private BigDecimal price;
    private LocalDateTime orderedAt;
}
