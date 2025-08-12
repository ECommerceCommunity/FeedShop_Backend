package com.cMall.feedShop.order.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderItemRequest {

    @NotNull(message = "상품 옵션 ID는 필수입니다.")
    @Min(value = 1, message = "상품 옵션 ID는 1 이상이어야 합니다.")
    @Schema(description = "상품 옵션 ID", example = "123", required = true)
    private Long optionId;

    @NotNull(message = "상품 이미지 ID는 필수입니다.")
    @Min(value = 1, message = "상품 이미지 ID는 1 이상이어야 합니다.")
    @Schema(description = "상품 이미지 ID", example = "123", required = true)
    private Long imageId;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    @Schema(description = "주문 수량(최대 50개)", example = "2", required = true)
    private Integer quantity;
}
