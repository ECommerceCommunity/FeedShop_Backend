package com.cMall.feedShop.product.domain.repository;

import com.cMall.feedShop.product.domain.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    Optional<ProductImage> findByImageId(Long imageId);
}
