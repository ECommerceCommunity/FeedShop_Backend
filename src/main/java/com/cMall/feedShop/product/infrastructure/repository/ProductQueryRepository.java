package com.cMall.feedShop.product.infrastructure.repository;

import com.cMall.feedShop.product.domain.enums.ProductSortType;
import com.cMall.feedShop.product.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductQueryRepository {
    long countByKeyword(String keyword);

    long countWithFilters(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Long storeId);

    long countAll();

    Page<Product> findProductsWithFilters(
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long storeId,
            ProductSortType productSortType,
            Pageable pageable
    );

    Page<Product> searchProductsByName(String keyword, Pageable pageable);
}
