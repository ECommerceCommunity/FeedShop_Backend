package com.cMall.feedShop.store.application.dto.response;

import com.cMall.feedShop.store.domain.model.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreListResponse {
    private Long storeId;
    private String storeName;

    public static StoreListResponse from(Store store) {
        return StoreListResponse.builder()
                .storeId(store.getStoreId())
                .storeName(store.getStoreName())
                .build();
    }
}
