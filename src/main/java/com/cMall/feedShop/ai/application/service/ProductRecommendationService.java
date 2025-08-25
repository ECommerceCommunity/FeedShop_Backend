package com.cMall.feedShop.ai.application.service;

import com.cMall.feedShop.ai.application.dto.response.ProductRecommendationAIResponse;
import com.cMall.feedShop.ai.domain.enums.ProductRecommendationConfig;
import com.cMall.feedShop.ai.domain.fallback.ProductRecommendationFallback;
import com.cMall.feedShop.ai.domain.repository.ProductRecommendationRepository;
import com.cMall.feedShop.ai.domain.template.ProductRecommendationTemplate;
import com.cMall.feedShop.common.ai.BaseAIService;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.user.domain.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductRecommendationService {
    private final BaseAIService aiService;
    private final ProductRecommendationRepository productRecommendationRepository;
    private final ObjectMapper objectMapper;
    private final ProductRecommendationTemplate template;
    private final ProductRecommendationFallback fallback;

    // AI를 사용하여 사용자 맞춤 상품 추천
    public List<Product> recommendProducts(User user, String prompt, int limit) {
        final int safeLimit = ProductRecommendationConfig.validateProductCount(limit);
        log.info("AI 추천 요청 - 사용자: {}, 프롬프트: '{}'", user != null ? user.getEmail() : "anonymous", prompt);

        try {
            // AI 프롬프트 생성
            String aiPrompt = template.buildPrompt(user, prompt, safeLimit);

            // 생성된 AI 프롬프트로 물어보기
            String aiResponse = aiService.generateText(aiPrompt);

            // AI 응답을 파싱
            ProductRecommendationAIResponse responseMap = aiService.parseAIResponse(aiResponse, ProductRecommendationAIResponse.class);

            // 상품 ID 추출
            List<Long> recommendedProductIds = extractProductIds(responseMap, safeLimit);

            // 상품 ID로 상품 조회
            List<Product> products = getProductsByIds(recommendedProductIds, safeLimit);

            // 추천 기록 저장
            saveRecommendation(user, prompt, recommendedProductIds, aiResponse);

            return products;
        } catch (Exception e) {
            log.warn("AI 추천 중 오류 발생, 폴백 사용: {}", e.getMessage());
            return fallback.getFallbackProducts(safeLimit);
        }
    }

    private List<Long> extractProductIds(ProductRecommendationAIResponse responseMap, int limit) {
        try {
            if (!responseMap.isSuccess()) {
                log.warn("AI 응답 실패: {}, fallback 사용", responseMap.getMessage());
                return fallback.getFallbackProductIds(limit);
            }

            List<Long> productIds = responseMap.getProductIds();
            if (productIds.isEmpty()) {
                log.warn("AI 응답에서 productIds를 찾을 수 없음, fallback 사용");
                return fallback.getFallbackProductIds(limit);
            }

            return productIds.stream()
                    .filter(id -> id != null && id > 0)
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("상품 ID 추출 실패: {}", e.getMessage());
            return fallback.getFallbackProductIds(limit);
        }
    }

    private List<Product> getProductsByIds(List<Long> productIds, int limit) {
        if (productIds == null || productIds.isEmpty()) {
            return fallback.getFallbackProducts(limit);
        }

        List<Product> products = productRecommendationRepository.findProductsByIds(productIds);

        // id -> product 맵
        Map<Long, Product> byId = products.stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));

        // AI가 준 순서를 보존하며 수집
        List<Product> ordered = productIds.stream()
                .map(byId::get)
                .filter(p -> p != null)
                .collect(Collectors.toList());

        // 부족하면 폴백으로 채우되 중복 제거
        if (ordered.size() < limit) {
            int need = limit - ordered.size();
            var seen = new java.util.LinkedHashSet<Long>(
                    ordered.stream().map(Product::getProductId).toList()
            );
            List<Product> fallback = this.fallback.getFallbackProducts(Math.max(need * 2, 1));
            for (Product p : fallback) {
                if (ordered.size() >= limit) break;
                Long id = p.getProductId();
                if (seen.add(id)) {        // 처음 본 id만 추가
                    ordered.add(p);
                }
            }
        }

        // 최종적으로 정확히 limit 개로 자르기
        return ordered.stream().limit(limit).collect(Collectors.toList());
    }

    private void saveRecommendation(User user, String prompt, List<Long> productIds, String response) {
        try {
            String productIdsJson = objectMapper.writeValueAsString(productIds);
            com.cMall.feedShop.ai.domain.model.ProductRecommendation recommendation = com.cMall.feedShop.ai.domain.model.ProductRecommendation.builder()
                    .user(user)
                    .prompt(prompt)
                    .recommendedProductIds(productIdsJson)
                    .response(response)
                    .build();
            productRecommendationRepository.save(recommendation);
        } catch (Exception e) {
            log.error("추천 이력 저장 실패", e);
        }
    }
}
