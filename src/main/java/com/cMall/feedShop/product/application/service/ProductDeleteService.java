package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductDeleteService {

    private final ProductRepository productRepository;
    private final ProductHelper productHelper;

    public void deleteProduct(Long productId, String loginId) {
        // 1. 현재 사용자 정보 가져오기 및 권한 검증
        User currentUser = productHelper.getCurrentUser(loginId);

        // 2. 판매자 권한 검증
        productHelper.validateSellerRole(currentUser);

        // 3. 상품 조회 (소유권 검증 포함)
        Product product = productHelper.getProductOwnership(productId, currentUser.getId());

        // 4. 주문에 포함된 상품인지 확인
        productHelper.validateProductNotInOrder(product);

        // 5. 소프트 딜리트
        product.delete();
    }
}