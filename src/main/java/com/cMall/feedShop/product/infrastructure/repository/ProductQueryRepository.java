package com.cMall.feedShop.product.infrastructure.repository;

import com.cMall.feedShop.product.application.dto.request.ProductSearchRequest;
import com.cMall.feedShop.product.domain.enums.ProductSortType;
import com.cMall.feedShop.product.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryRepository {

    long countWithAllConditions(ProductSearchRequest request);

    Page<Product> findWithAllConditions(ProductSearchRequest request, ProductSortType sortType, Pageable pageable);

    long countByStoreId(Long storeId);

    Page<Product> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);
}
