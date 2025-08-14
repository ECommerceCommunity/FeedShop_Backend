package com.cMall.feedShop.product.application.dto.response;

import com.cMall.feedShop.product.domain.enums.ImageType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductImageUploadResponse {
    private String imageUrl; // GCS에 저장된 이미지 URL
    private ImageType type; // 이미지 타입 (MAIN, DETAIL)
    private String originalName; // 원본 파일 이름
    private Long fileSize; // 파일 크기 (바이트 단위)

    public static ProductImageUploadResponse of(String imageUrl, ImageType type, String originalName, Long fileSize) {
        return ProductImageUploadResponse.builder()
                .imageUrl(imageUrl)
                .type(type)
                .originalName(originalName)
                .fileSize(fileSize)
                .build();
    }
}
