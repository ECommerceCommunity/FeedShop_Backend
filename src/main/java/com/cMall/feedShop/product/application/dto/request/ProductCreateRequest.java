package com.cMall.feedShop.product.application.dto.request;

import com.cMall.feedShop.product.domain.enums.DiscountType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class ProductCreateRequest {
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 200, message = "상품명은 200자를 초과할 수 없습니다.")
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @DecimalMin(value = "1000", message = "가격은 1000보다 커야 합니다.")
    private BigDecimal price;

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    private DiscountType discountType;

    private BigDecimal discountValue;

    private String description;

    @Valid
    @NotEmpty(message = "상품 이미지는 최소 1개 이상 등록해야 합니다.")
    private List<ProductImageRequest> images;

    @Valid
    @NotEmpty(message = "상품 옵션은 최소 1개 이상 등록해야 합니다.")
    private List<ProductOptionRequest> options;
}