package com.cMall.feedShop.product.application.dto.request;

import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductOptionRequest {
    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;

    @NotNull(message = "사이즈는 필수입니다.")
    private Size size;

    @NotNull(message = "색상은 필수입니다.")
    private Color color;

    @NotNull(message = "재고는 필수입니다.")
    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    private Integer stock;
}
