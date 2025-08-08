package com.cMall.feedShop.order.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.domain.enums.OrderStatus;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.ORDERED;

    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "final_price", nullable = false)
    private BigDecimal finalPrice;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "used_points", nullable = false)
    private Integer usedPoints = 0;

    @Column(name = "earned_points", nullable = false)
    private Integer earnedPoints = 0;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "delivery_detail_address", nullable = false)
    private String deliveryDetailAddress;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false)
    private String recipientPhone;

    @Column(name = "delivery_message")
    private String deliveryMessage;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "currency", nullable = false)
    private String currency = "KRW"; // 기본 통화는 한국 원화(KRW)

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "card_expiry")
    private String cardExpiry;

    @Column(name = "card_cvc")
    private String cardCvc;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Order(User user, OrderStatus status, BigDecimal finalPrice, BigDecimal deliveryFee, BigDecimal totalPrice,
                 Integer usedPoints, Integer earnedPoints, String deliveryAddress, String deliveryDetailAddress,
                 String postalCode, String recipientName, String recipientPhone, String deliveryMessage,
                 String paymentMethod, String cardNumber, String cardExpiry, String cardCvc) {
        this.user = user;
        this.status = status;
        this.finalPrice = finalPrice;
        this.deliveryFee = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        this.totalPrice = totalPrice;
        this.usedPoints = usedPoints != null ? usedPoints : 0;
        this.earnedPoints = earnedPoints != null ? earnedPoints : 0;
        this.deliveryAddress = deliveryAddress;
        this.deliveryDetailAddress = deliveryDetailAddress;
        this.postalCode = postalCode;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.deliveryMessage = deliveryMessage;
        this.paymentMethod = paymentMethod;
        this.cardNumber = cardNumber;
        this.cardExpiry = cardExpiry;
        this.cardCvc = cardCvc;
        this.currency = "KRW";
    }

    // 주문 아이템 추가
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
    }

    // 주문 상태 업데이트
    public void updateStatus(OrderStatus status) {
        this.status = status;
    }
    
    // 주문 취소
    public void cancel() {
        if (this.status == OrderStatus.ORDERED) {
            this.status = OrderStatus.CANCELLED;
        } else {
            throw new OrderException(ErrorCode.ORDER_CANCEL_FORBIDDEN);
        }
    }
}
