package com.cMall.feedShop.product.application.dto.response;

import com.cMall.feedShop.product.application.dto.common.ProductImageInfo;
import com.cMall.feedShop.product.application.dto.common.ProductOptionInfo;
import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProductDetailResponse {
    private Long productId;
    private String name;
    private BigDecimal price;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal discountPrice;
    private Integer wishNumber;
    private String description;
    private Long storeId;
    private String storeName;
    private CategoryType categoryType;
    private String categoryName;
    private List<ProductImageInfo> images;
    private List<ProductOptionInfo> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductDetailResponse of(
            Long productId,
            String name,
            BigDecimal price,
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal discountPrice,
            Integer wishNumber,
            String description,
            Long storeId,
            String storeName,
            CategoryType categoryType,
            String categoryName,
            List<ProductImageInfo> images,
            List<ProductOptionInfo> options,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return ProductDetailResponse.builder()
                .productId(productId)
                .name(name)
                .price(price)
                .discountType(discountType)
                .discountValue(discountValue)
                .discountPrice(discountPrice)
                .wishNumber(wishNumber != null ? wishNumber : 0)
                .description(description)
                .storeId(storeId)
                .storeName(storeName)
                .categoryType(categoryType)
                .categoryName(categoryName)
                .images(images != null ? images : List.of())
                .options(options != null ? options : List.of())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
