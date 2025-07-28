package com.cMall.feedShop.store.domain.repository;

import com.cMall.feedShop.store.domain.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findBySellerId(Long sellerId);
}
