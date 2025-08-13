package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.application.dto.response.ProductDetailResponse;
import com.cMall.feedShop.product.application.dto.response.ProductListResponse;
import com.cMall.feedShop.product.application.dto.response.info.ProductImageInfo;
import com.cMall.feedShop.product.application.dto.response.info.ProductOptionInfo;
import com.cMall.feedShop.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final DiscountCalculator discountCalculator;

    /**
     * Product 엔티티를 ProductListResponse로 변환
     */
    public ProductListResponse toListResponse(Product product) {
        // 1. 할인된 가격을 계산한다.
        BigDecimal discountPrice = product.getDiscountPrice(discountCalculator);

        // 2. 대표 이미지 URL을 가져온다.
        String mainImageUrl = product.getMainImageUrl();

        // 3. 응답 객체 생성
        return ProductListResponse.of(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                discountPrice,
                product.getCategory().getCategoryId(),
                product.getStore().getStoreId(),
                product.getStore().getStoreName(),
                product.getWishNumber(),
                mainImageUrl
        );
    }

    /**
     * Product 엔티티를 ProductDetailResponse로 변환
     */
    public ProductDetailResponse toDetailResponse(Product product) {
        // 할인가를 계산한다.
        BigDecimal discountPrice = product.getDiscountPrice(discountCalculator);

        // 이미지를 List<ProductImageDto>로 변환
        List<ProductImageInfo> images = ProductImageInfo.fromList(product.getProductImages());

        // 이미지를 List<ProductOptionDto>로 변환
        List<ProductOptionInfo> options = ProductOptionInfo.fromList(product.getProductOptions());

        return ProductDetailResponse.of(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getDiscountType(),
                product.getDiscountValue(),
                discountPrice,
                product.getWishNumber(),
                product.getDescription(),
                product.getStore().getStoreId(),
                product.getStore().getStoreName(),
                product.getCategory().getType(),
                product.getCategory().getName(),
                images,
                options,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
