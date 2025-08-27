package com.cMall.feedShop.ai.presentation;

import com.cMall.feedShop.ai.application.dto.request.ProductRecommendationRequest;
import com.cMall.feedShop.ai.application.dto.response.ProductRecommendationResponse;
import com.cMall.feedShop.ai.application.service.ProductRecommendationService;
import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.product.application.dto.response.ProductListResponse;
import com.cMall.feedShop.product.application.service.ProductMapper;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "상품 AI 추천", description = "추천 상품 조회 관련 API")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class ProductRecommendationController {

    private final ProductRecommendationService productRecommendationService;
    private final ProductMapper productMapper;
    private final UserRepository userRepository;

    @Operation(
            summary = "상품 AI 추천",
            description = "프로필 신체 정보를 반영하여 사용자 맞춤형 상품을 AI가 추천합니다. (로그인 사용자에 한함)"
    )
    @PostMapping("/products/recommend")
    @ApiResponseFormat(message = "AI 추천 완료")
    public ApiResponse<ProductRecommendationResponse> recommend(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProductRecommendationRequest request
    ) {
        User currentUser = AuthUtils.extractUser(userDetails, userRepository);

        // AI 추천 서비스 호출 (결과값은 상품 리스트)
        List<Product> recommendedProducts = productRecommendationService
                .recommendProducts(currentUser, request.getPrompt(), request.getLimit());

        // 상품 리스트를 응답 DTO로 변환
        List<ProductListResponse> productResponses = recommendedProducts.stream()
                .map(productMapper::toListResponse)
                .collect(Collectors.toList());

        // 최종 응답 생성
        ProductRecommendationResponse response = ProductRecommendationResponse.builder()
                .products(productResponses)
                .prompt(request.getPrompt())
                .count(productResponses.size())
                .build();

        return ApiResponse.success(response);
    }
}
