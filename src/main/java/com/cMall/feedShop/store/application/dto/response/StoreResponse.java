package com.cMall.feedShop.store.application.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class StoreResponse {
    private Long storeId;
    private String storeName;
    private String description;
    private String logo;
    private Long managerId;
    private LocalDateTime createdAt;
}
