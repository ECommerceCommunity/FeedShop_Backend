package com.cMall.feedShop.product.application.dto.common;

import com.cMall.feedShop.product.domain.enums.ImageType;
import com.cMall.feedShop.product.domain.model.ProductImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductImageInfo {
    private Long imageId;
    private String url;
    private ImageType type;

    public static ProductImageInfo from(ProductImage image) {
        return ProductImageInfo.builder()
                .imageId(image.getImageId())
                .url(image.getUrl())
                .type(image.getType())
                .build();
    }

    public static List<ProductImageInfo> fromList(List<ProductImage> images)
    {
        return images.stream()
                .map(ProductImageInfo::from)
                .toList();
    }
}
