package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.calculator.DiscountCalculator;
import com.cMall.feedShop.product.application.dto.response.ProductListResponse;
import com.cMall.feedShop.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final DiscountCalculator discountCalculator;

    public ProductListResponse toListResponse(Product product) {
        // 1. 할인된 가격을 계산한다.
        BigDecimal discountPrice = product.getDiscountPrice(discountCalculator);

        // 2. 대표 이미지 URL을 가져온다.
        String mainImageUrl = product.getMainImageUrl();

        // 3. 응답 객체 생성
        return ProductListResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .discountPrice(discountPrice)
                .mainImageUrl(mainImageUrl)
                .categoryId(product.getCategory().getCategoryId())
                .storeId(product.getStore().getStoreId())
                .storeName(product.getStore().getStoreName())
                .build();
    }
}
