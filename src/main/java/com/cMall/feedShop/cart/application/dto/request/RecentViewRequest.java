package com.cMall.feedShop.cart.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecentViewRequest {
    private Long userId;
    private Long productId;
}
