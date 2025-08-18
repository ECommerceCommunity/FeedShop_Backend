package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductSearchRequest;
import com.cMall.feedShop.product.application.dto.response.*;
import com.cMall.feedShop.product.application.utils.PagingUtils;
import com.cMall.feedShop.product.domain.enums.ProductSortType;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    // 상품 목록 조회 (페이징)
    public ProductPageResponse getProductList(ProductSearchRequest request, int page, int size, ProductSortType productSortType) {
        // 페이지 정보 생성
        long totalElements = productRepository.countWithAllConditions(request);

        // Page Overflow 체크하여 Pageable 생성
        Pageable pageable = PagingUtils.createPageable(page, size, totalElements);

        // 정렬 기준에 따라 상품 목록 조회
        Page<Product> productPage = productRepository.findWithAllConditions(request, productSortType, pageable);

        // 각각의 상품(Product 엔티티)을 ProductListResponse(응답값)로 변환한다.
        Page<ProductListResponse> responsePage = productPage.map(productMapper::toListResponse);

        // ProductPageResponse 에서 상품 리스트 묶어서 페이지 정보 추가. 최종 응답값 리턴
        return ProductPageResponse.of(responsePage);
    }

    // 판매자 상품 목록 조회 (페이징)
    public ProductPageResponse getSellerProductList(int page, int size, String loginId) {
        // 판매자 ID가 유효한지 확인
        User currentUser = getCurrentUser(loginId);

        // 판매자 권한 검증
        validateSellerRole(currentUser);

        // 스토어 조회
        Store store = getUserStore(currentUser.getId());

        // 해당 스토어의 총 상품 수 조회
        long totalElements = productRepository.countByStoreId(store.getStoreId());

        // 페이지 정보 생성
        Pageable pageable = PagingUtils.createPageable(page, size, totalElements);

        // 판매자 ID에 해당하는 상품 목록 조회
        Page<Product> productPage = productRepository.findByStoreIdOrderByCreatedAtDesc(store.getStoreId(), pageable);

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

    // JWT 에서 현재 사용자 추출
    private User getCurrentUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ProductException(ErrorCode.USER_NOT_FOUND));
    }

    // 스토어 조회
    private Store getUserStore(Long userId) {
        // 내 가게를 찾는다.
        return storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new ProductException(ErrorCode.STORE_NOT_FOUND));
    }

    // 판매자 권한을 검증한다.
    private void validateSellerRole(User user) {
        if (user.getRole() != UserRole.SELLER) {
            throw new ProductException(ErrorCode.FORBIDDEN);
        }
    }
}
