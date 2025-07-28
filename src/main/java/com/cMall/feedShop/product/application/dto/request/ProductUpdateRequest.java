package com.cMall.feedShop.product.application.dto.request;

import com.cMall.feedShop.product.domain.enums.DiscountType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class ProductUpdateRequest {
    @Size(max = 200, message = "상품 이름은 최대 200자까지 입력 가능합니다.")
    private String name;

    @DecimalMin(value = "1000", message = "상품 가격은 1000원 이상이어야 합니다.")
    private BigDecimal price;

    private Long categoryId;

    private DiscountType discountType;

    private BigDecimal discountValue;

    private String description;

    @Valid
    private List<ProductImageRequest> images;

    @Valid
    private List<ProductOptionRequest> options;
}
