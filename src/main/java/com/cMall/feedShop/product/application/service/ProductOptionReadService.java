package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.response.info.ProductOptionInfo;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOptionReadService {

    private final ProductOptionHelper productOptionHelper;

    public List<ProductOptionInfo> getProductOptions(Long productId, String loginId) {
        // 1. 현재 사용자를 가져오고 권한을 검증한다.
        User currentUser = productOptionHelper.getCurrentUser(loginId);

        // 2. 판매자 권한을 검증한다.
        productOptionHelper.validateSellerRole(currentUser);

        // 3. 상품 소유권을 검증하고 상품 정보를 가져온다.
        Product product = productOptionHelper.getProductOwnership(productId, currentUser.getId());

        // 4. 상품 옵션 정보를 조회하여 반환한다.
        return ProductOptionInfo.fromList(product.getProductOptions());
    }
}