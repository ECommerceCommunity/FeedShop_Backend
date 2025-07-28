package com.cMall.feedShop.product.domain.repository;

import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.store.domain.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 상품 목록 조회 (store, productImages 포함)
    @EntityGraph(attributePaths = {"store", "category", "productImages"})
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 상품 상세 조회 (모든 연관 포함)
    @EntityGraph(attributePaths = {"store", "category", "productImages"})
    Optional<Product> findByProductId(Long productId);

    // 같은 스토어 내의 상품명 중복 확인
    boolean existsByStoreAndName(Store store, String productName);

    // 상품 수정 시 자기 자신 제외하고 중복 확인
    boolean existsByStoreAndNameAndProductIdNot(Store store, String productName, Long productId);
}
