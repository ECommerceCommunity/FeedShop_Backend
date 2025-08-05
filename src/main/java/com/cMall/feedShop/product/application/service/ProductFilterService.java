package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.request.ProductFilterRequest;
import com.cMall.feedShop.product.application.dto.response.ProductListResponse;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductFilterService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * 상품 목록 필터링 조회 서비스
     * @param request
     * @param page
     * @param size
     * @return
     */
    public ProductPageResponse filterProductList(ProductFilterRequest request, int page, int size) {
        // 1. 페이지 번호를 검증한다 (음수면 0으로 고정한다.)
        if (page < 0) {
            page = 0;
        }

        // 2. 페이지 크기를 검증한다 (1~100 사이로 제한한다. 기본값은 20)
        if (size < 1 || size > 100) {
            size = 20;
        }

        // 3. 페이지 요청 객체를 생성한다.
        Pageable pageable = PageRequest.of(page, size);

        // 4. 필터링된 상품 목록을 조회한다.
        Page<Product> productPage = productRepository.findProductsWithFilters(
                request.getCategoryId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getStoreId(),
                pageable
        );

        // 5. Product 엔티티를 ProductListResponse로 변환한다.
        Page<ProductListResponse> responsePage = productPage.map(productMapper::toListResponse);

        // 6. 응답값 객체 생성하여 반환.
        return ProductPageResponse.of(responsePage);
    }
}
