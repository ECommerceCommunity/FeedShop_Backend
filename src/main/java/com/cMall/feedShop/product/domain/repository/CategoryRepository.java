package com.cMall.feedShop.product.domain.repository;

import com.cMall.feedShop.product.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
