package com.cMall.feedShop.order.infrastructure.repository;

import com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo;

import java.util.List;

public interface OrderItemQueryRepository {
    List<PurchasedItemInfo> findPurchasedItemsByUserId(Long userId);
}
