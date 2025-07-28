package com.cMall.feedShop.store.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreRequest {
    private String storeName;
    private String description;
    private String logo;
    private Long managerId;
}
