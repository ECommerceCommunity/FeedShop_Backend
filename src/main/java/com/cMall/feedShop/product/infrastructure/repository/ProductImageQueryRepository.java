package com.cMall.feedShop.product.infrastructure.repository;

import com.cMall.feedShop.product.domain.model.ProductImage;

import java.util.List;
import java.util.Set;

public interface ProductImageQueryRepository {

    // 여러 상품의 첫 번째 이미지들을 한 번에 조회한다.
    List<ProductImage> findFirstImagesByProductIds(Set<Long> productIds);
}
