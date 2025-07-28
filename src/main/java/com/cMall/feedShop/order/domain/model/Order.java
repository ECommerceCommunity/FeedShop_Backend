package com.cMall.feedShop.order.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@NoArgsConstructor
public class Order {
    @Id
    private Long orderId;
    private Long userId;
    private String status;
    private BigDecimal deliveryFee;
    private BigDecimal totalPrice;
    private BigDecimal totalDiscountPrice;
    private String currency;
    private Integer usedPoints;
    private Integer earnedPoints;
    private String deliveryAddress;
    private String deliveryDetailAddress;
    private String postalCode;
    private String recipientName;
    private String recipientPhone;
    private String deliveryMessage;
    private String paymentMethod;
    private String cardNumber;
    private String cardExpiry;
    private String cardCvc;
    private LocalDateTime orderedAt;
    private LocalDateTime deletedAt;
}
