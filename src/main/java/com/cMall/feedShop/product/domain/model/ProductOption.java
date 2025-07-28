package com.cMall.feedShop.product.domain.model;

import com.cMall.feedShop.product.domain.converter.SizeConverter;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
