package com.cMall.feedShop.order.application.dto.response;

import com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
public class PurchasedItemListResponse {
    private List<PurchasedItemInfo> items;
    private int totalCount;

    public static PurchasedItemListResponse from(List<PurchasedItemInfo> items) {
        return PurchasedItemListResponse.builder()
                .items(items)
                .totalCount(items.size())
                .build();
    }
}
