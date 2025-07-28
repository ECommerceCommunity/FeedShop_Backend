package com.cMall.feedShop.cart.domain.repository;

import com.cMall.feedShop.cart.domain.model.RecentView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecentViewRepository extends JpaRepository<RecentView, Long> {
}
