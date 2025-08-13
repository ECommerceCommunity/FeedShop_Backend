package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.response.*;
import com.cMall.feedShop.product.application.utils.PagingUtils;
import com.cMall.feedShop.product.domain.enums.ProductSortType;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductReadService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    // 상품 목록 조회 (페이징)
    public ProductPageResponse getProductList(int page, int size, ProductSortType productSortType) {
        // 페이지 정보 생성
        long totalElements = productRepository.countAll();
        Pageable pageable = PagingUtils.createPageable(page, size, totalElements);

        // 정렬 기준에 따라 상품 목록 조회
        Page<Product> productPage = getProductsBySortType(productSortType, pageable);

        // 각각의 상품(Product 엔티티)을 ProductListResponse(응답값)로 변환한다.
        Page<ProductListResponse> responsePage = productPage.map(productMapper::toListResponse);

        // ProductPageResponse 에서 상품 리스트 묶어서 페이지 정보 추가. 최종 응답값 리턴
        return ProductPageResponse.of(responsePage);
    }

    // 상품 상세 조회
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));

        // productOptions 지연 로딩 강제 초기화
        Hibernate.initialize(product.getProductOptions());

        // 상품(Product 엔티티)을 ProductDetailResponse(응답값)로 변환한다.
        return productMapper.toDetailResponse(product);
    }

    // 정렬 기준에 따라 상품 목록을 조회한다.
    private Page<Product> getProductsBySortType(ProductSortType productSortType, Pageable pageable) {
        return productSortType == ProductSortType.POPULAR
                ? productRepository.findAllByOrderByWishNumberDesc(pageable)
                : productRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
