package com.cMall.feedShop.cart.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RecentViewResponse {
    private Long viewId;
    private Long productId;
    private LocalDateTime viewedAt;
}
