package com.cMall.feedShop.product.domain.model;

import com.cMall.feedShop.product.domain.enums.ImageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_images")
@Getter
@NoArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id")
    private Long imageId;

    @Column(name = "url", nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name="type", length = 50, nullable = false)
    private ImageType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public ProductImage(String url, ImageType type, Product product) {
        this.url = url;
        this.type = type;
        this.product = product;
    }
}
