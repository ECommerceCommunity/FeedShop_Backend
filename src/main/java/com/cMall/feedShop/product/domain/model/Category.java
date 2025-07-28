package com.cMall.feedShop.product.domain.model;

import com.cMall.feedShop.product.domain.enums.CategoryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name="type", length = 50, nullable = false)
    private CategoryType type;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    public Category(CategoryType type, String name) {
        this.type = type;
        this.name = name;
    }
}
