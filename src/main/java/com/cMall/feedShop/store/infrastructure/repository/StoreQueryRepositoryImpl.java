package com.cMall.feedShop.store.infrastructure.repository;

import com.cMall.feedShop.store.domain.model.QStore;
import com.cMall.feedShop.store.domain.model.Store;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StoreQueryRepositoryImpl implements StoreQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Store> findAllStoresOrderByName() {
        QStore store = QStore.store;

        return queryFactory
                .selectFrom(store)
                .orderBy(store.storeName.asc())
                .fetch();
    }
}
