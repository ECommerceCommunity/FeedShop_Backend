package com.cMall.feedShop.product.domain.model;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.domain.model.OrderItem;
import com.cMall.feedShop.product.domain.converter.SizeConverter;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.exception.ProductException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_options")
@Getter
@NoArgsConstructor
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long optionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 50)
    private Gender gender;

    @Convert(converter = SizeConverter.class)
    @Column(name = "size")
    private Size size;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", length = 50)
    private Color color;

    @Column(name = "stock")
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "productOption", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    public ProductOption(Gender gender, Size size, Color color, Integer stock, Product product) {
        this.gender = gender;
        this.size = size;
        this.color = color;
        this.stock = stock;
        this.product = product;
    }

    // 재고 확인
    public boolean isInStock() {
        return stock != null && stock > 0;
    }

    // 재고 차감
    public void decreaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ProductException(ErrorCode.OUT_OF_STOCK, "차감할 수량은 1 이상이어야 합니다.");
        }

        if (this.stock == null || this.stock < quantity) {
            throw new ProductException(ErrorCode.OUT_OF_STOCK);
        }

        this.stock -= quantity;
    }

    // 재고 증가 (주문 취소시)
    public void increaseStock(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ProductException(ErrorCode.OUT_OF_STOCK, "증가할 수량은 1 이상이어야 합니다.");
        }

        if (this.stock == null) {
            this.stock = quantity;
        }

        this.stock += quantity;
    }
}
