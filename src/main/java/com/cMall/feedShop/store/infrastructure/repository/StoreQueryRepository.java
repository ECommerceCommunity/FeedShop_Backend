package com.cMall.feedShop.store.infrastructure.repository;

import com.cMall.feedShop.store.domain.model.Store;

import java.util.List;

public interface StoreQueryRepository {
    // 모든 가게 목록을 이름순으로 조회
    List<Store> findAllStoresOrderByName();
}
