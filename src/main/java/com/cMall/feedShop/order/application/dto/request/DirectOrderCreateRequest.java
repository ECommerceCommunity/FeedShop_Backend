package com.cMall.feedShop.order.application.dto.request;

import com.cMall.feedShop.order.application.validator.ValidCardPayment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@ValidCardPayment
public class DirectOrderCreateRequest extends OrderCreateRequest {
    @NotEmpty(message = "주문할 상품 목록은 1개 이상이어야 합니다.")
    @Valid
    @Schema(description = "주문할 상품 목록", required = true)
    private List<OrderItemRequest> items;
}
