package com.cMall.feedShop.ai.domain.repository;

import com.cMall.feedShop.ai.domain.model.ProductRecommendation;
import com.cMall.feedShop.product.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRecommendationRepository extends JpaRepository<ProductRecommendation, Long> {

    // 상품 ID 목록으로 상품 조회
    @Query("SELECT p FROM Product p WHERE p.productId IN :productIds")
    List<Product> findProductsByIds(@Param("productIds") List<Long> productIds);

    // Fallback: 사용자별 모든 상품 조회 (최신순)
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    Page<Product> findAllProductsOrderByCreatedAtDesc(Pageable pageable);

}
