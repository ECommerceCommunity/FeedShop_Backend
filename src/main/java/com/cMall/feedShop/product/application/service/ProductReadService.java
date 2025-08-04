package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.common.ProductImageInfo;
import com.cMall.feedShop.product.application.dto.common.ProductOptionInfo;
import com.cMall.feedShop.product.application.dto.response.*;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.application.util.DiscountCalculator;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductReadService {
    private final ProductRepository productRepository;
    private final DiscountCalculator discountCalculator;

    // 상품 목록 조회 (페이징)
    public ProductPageResponse getProductList(int page, int size) {
        if (page < 0) {
            page = 0;
        }

        // 기본값 20, 최대 100
        if (size < 1 || size > 100) {
            size = 20;
        }

        Pageable pageable = PageRequest.of(page, size);

        // 삭제되지 않은 상품들을 Store, 이미지와 함께 조회. (모든 상품을 페이지별로)
        Page<Product> productPage = productRepository.findAllByOrderByCreatedAtDesc(pageable);

        // 각각의 상품(Product 엔티티)을 ProductListResponse(응답값)로 변환한다.
        Page<ProductListResponse> responsePage = productPage.map(this::convertToProductListResponse);

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
        return convertToProductDetailResponse(product);
    }

    // 각각의 상품들을 ProductListResponse로 변환한다.
    private ProductListResponse convertToProductListResponse(Product product) {

        // 할인가를 계산한다.
        BigDecimal discountPrice = product.getDiscountPrice(discountCalculator);

        // 대표 이미지 URL 가져오기
        String mainImageUrl = product.getMainImageUrl();

        // ProductListResponse 에서 응답값(상품 정보)을 생성해준다.
        return ProductListResponse.of(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                discountPrice,
                product.getStore().getStoreId(),
                product.getStore().getStoreName(),
                product.getWishNumber(),
                mainImageUrl
        );
    }

    // Product를 ProductDetailResponse로 변환한다.
    private ProductDetailResponse convertToProductDetailResponse(Product product) {
        // 할인가를 계산한다.
        BigDecimal discountPrice = product.getDiscountPrice(discountCalculator);

        // 이미지를 List<ProductImageDto>로 변환
        List<ProductImageInfo> images = ProductImageInfo.fromList(product.getProductImages());

        // 이미지를 List<ProductOptionDto>로 변환
        List<ProductOptionInfo> options = ProductOptionInfo.fromList(product.getProductOptions());

        return ProductDetailResponse.of(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getDiscountType(),
                product.getDiscountValue(),
                discountPrice,
                product.getWishNumber(),
                product.getDescription(),
                product.getStore().getStoreId(),
                product.getStore().getStoreName(),
                product.getCategory().getType(),
                product.getCategory().getName(),
                images,
                options,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
