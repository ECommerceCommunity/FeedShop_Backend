package com.cMall.feedShop.product.domain.repository;

import com.cMall.feedShop.product.domain.model.ProductImage;
import com.cMall.feedShop.product.infrastructure.repository.ProductImageQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long>, ProductImageQueryRepository {
    Optional<ProductImage> findByImageId(Long imageId);
}
