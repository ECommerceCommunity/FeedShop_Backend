package com.cMall.feedShop.product.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import com.cMall.feedShop.product.domain.enums.ImageType;
import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.store.domain.model.Store;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "wish_number", nullable = false, columnDefinition = "int default 0")
    private Integer wishNumber;

    @Enumerated(EnumType.STRING)
    @Column(name="discount_type", length = 50, nullable = false, columnDefinition = "varchar(50) default 'NONE'")
    private DiscountType discountType;

    @Column(name="discount_value")
    private BigDecimal discountValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductOption> productOptions = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade =  CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    @Builder
    public Product(String name, BigDecimal price, Store store, Category category,
                   DiscountType discountType, BigDecimal discountValue, String description) {
        this.name = name;
        this.price = price;
        this.store = store;
        this.category = category;
        this.discountType = discountType != null ? discountType : DiscountType.NONE;
        this.discountValue = discountValue;
        this.description = description;
        this.wishNumber = 0;
    }

    // 대표 이미지 조회 (목록에서 필요)
    public String getMainImageUrl() {
        return productImages.stream()
                .filter(image -> ImageType.MAIN.equals(image.getType()))
                .min(Comparator.comparing(ProductImage::getImageId))
                .map(ProductImage::getUrl)
                .orElse(null);
    }

    // 할인가 계산
    public BigDecimal getDiscountPrice(DiscountCalculator discountCalculator) {
        return discountCalculator.calculateDiscountPrice(
                this.price,
                this.discountType,
                this.discountValue
        );
    }

    // 상품 정보 업데이트
    public void updateInfo(String name, BigDecimal price, String description) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (price != null) {
            this.price = price;
        }
        if (description != null) {
            this.description = description;
        }
    }

    // 할인 정보 업데이트
    public void updateDiscount(DiscountType discountType, BigDecimal discountValue) {
        if (discountType != null) {
            this.discountType = discountType;
        }
        if (discountValue != null) {
            this.discountValue = discountValue;
        }
    }

    // 카테고리 업데이트
    public void updateCategory(Category category) {
        if (category != null) {
            this.category = category;
        }
    }

    // 판매 가능한 상태인지 확인
    public boolean isAvailableForSale() {
        return productOptions.stream()
                .anyMatch(ProductOption::isInStock);
    }
}
