package com.cMall.feedShop.order.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.domain.model.ProductOption;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor
public class OrderItem extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_image_id", nullable = false)
    private ProductImage productImage;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "final_price", nullable = false)
    private BigDecimal finalPrice;

    @Builder
    public OrderItem(Order order, ProductOption productOption, ProductImage productImage, Integer quantity, BigDecimal totalPrice, BigDecimal finalPrice) {
        this.order = order;
        this.productOption = productOption;
        this.productImage = productImage;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.finalPrice = finalPrice;
    }
}
