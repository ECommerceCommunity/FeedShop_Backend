package com.cMall.feedShop.ai.domain.fallback;

import com.cMall.feedShop.ai.domain.repository.ProductRecommendationRepository;
import com.cMall.feedShop.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductRecommendationFallback {
    private final ProductRecommendationRepository productRecommendationRepository;

    // AI 상품 추천 실패 시, 최근 등록된 상품 ID 목록을 폴백으로 제공
    public List<Long> getFallbackProductIds(int limit) {
        try {
            return getFallbackProducts(limit).stream()
                    .map(Product::getProductId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Fallback 상품 ID 조회 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // AI 상품 추천 실패 시, 최근 등록된 상품 목록을 폴백으로 제공
    public List<Product> getFallbackProducts(int limit) {
        try {
            int safeLimit = Math.max(1, limit);
            log.info("Fallback 상품 조회 - 개수: {}", safeLimit);

            return productRecommendationRepository
                    .findAllProductsOrderByCreatedAtDesc(PageRequest.of(0, safeLimit))
                    .getContent();

        } catch (Exception e) {
            log.error("Fallback 상품 조회 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

}
