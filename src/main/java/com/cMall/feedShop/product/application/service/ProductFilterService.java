package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.request.ProductFilterRequest;
import com.cMall.feedShop.product.application.dto.response.ProductListResponse;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.domain.enums.ProductSortType;
import com.cMall.feedShop.product.application.utils.PagingUtils;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ProductPageResponse filterProductList(ProductFilterRequest request, int page, int size, ProductSortType productSortType) {
        // 페이지 정보 생성
        Pageable pageable = PagingUtils.createPageable(page, size, productRepository.countWithFilters(
                request.getCategoryId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getStoreId()
        ));

        // 필터링된 상품 목록을 조회한다.
        Page<Product> productPage = productRepository.findProductsWithFilters(
                request.getCategoryId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getStoreId(),
                productSortType,
                pageable
        );

        // Product 엔티티를 ProductListResponse로 변환한다.
        Page<ProductListResponse> responsePage = productPage.map(productMapper::toListResponse);

        // 응답값 객체 생성하여 반환.
        return ProductPageResponse.of(responsePage);
    }
}
