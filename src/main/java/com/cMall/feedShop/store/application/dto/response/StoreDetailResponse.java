package com.cMall.feedShop.store.application.dto.response;

import com.cMall.feedShop.store.domain.model.Store;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StoreDetailResponse {
    private Long storeId;
    private Long sellerId;
    private String storeName;
    private String description;
    private String logo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoreDetailResponse from(Store store) {
        return StoreDetailResponse.builder()
                .storeId(store.getStoreId())
                .sellerId(store.getSellerId())
                .storeName(store.getStoreName())
                .description(store.getDescription())
                .logo(store.getLogo())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
