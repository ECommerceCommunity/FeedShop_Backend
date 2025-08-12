package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.response.ProductListResponse;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.application.utils.PagingUtils;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * 상품 목록 검색 서비스
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색된 상품 목록 응답
     */
    public ProductPageResponse searchProductList(String keyword, int page, int size) {
        // 키워드 trim
        if (keyword != null) {
            keyword = StringUtils.trimToEmpty(keyword);
        }

        // 페이지 정보 생성
        Pageable pageable = PagingUtils.createPageable(page, size);

        // 상품 검색 (키워드가 있으면 검색, 없으면 전체 목록)
        Page<Product> productPage = productRepository.searchProductsByName(keyword, pageable);

        if (PagingUtils.isPageOverflow(productPage, page)) {
            long totalElements = productPage.getTotalElements();
            Pageable correctedPageable = PagingUtils.createPageable(page, size, totalElements);
            productPage = productRepository.searchProductsByName(keyword, correctedPageable);
        }

        // 각각의 상품(Product 엔티티)을 ProductListResponse(응답값)로 변환한다.
        Page<ProductListResponse> responsePage = productPage.map(productMapper::toListResponse);

        // ProductPageResponse 에서 상품 리스트 묶어서 페이지 정보 추가. 최종 응답값 리턴
        return ProductPageResponse.of(responsePage);
    }
}
