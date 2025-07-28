package com.cMall.feedShop.order.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderRequest {
    private Long optionId;
    private Integer quantity;
}
