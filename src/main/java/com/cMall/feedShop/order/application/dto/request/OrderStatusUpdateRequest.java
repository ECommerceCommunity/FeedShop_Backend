package com.cMall.feedShop.order.application.dto.request;

import com.cMall.feedShop.order.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderStatusUpdateRequest {
    @NotNull(message = "주문 상태는 필수입니다.")
    private OrderStatus status;
}
