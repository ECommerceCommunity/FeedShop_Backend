package com.cMall.feedShop.product.application.dto.request;

import com.cMall.feedShop.product.domain.enums.ImageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductImageRequest {
    @NotBlank(message = "이미지 URL은 필수입니다.")
    private String url;

    @NotNull(message = "이미지 타입은 필수입니다.")
    private ImageType type;
}
