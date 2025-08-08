package com.cMall.feedShop.order.application.dto.response.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasedItemInfo {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private LocalDateTime orderedAt;

    public static PurchasedItemInfo of(Long orderItemId, Long productId, String productName, String productImageUrl, LocalDateTime orderedAt) {
        return PurchasedItemInfo.builder()
                .orderItemId(orderItemId)
                .productId(productId)
                .productName(productName)
                .productImageUrl(productImageUrl)
                .orderedAt(orderedAt)
                .build();
    }
}
