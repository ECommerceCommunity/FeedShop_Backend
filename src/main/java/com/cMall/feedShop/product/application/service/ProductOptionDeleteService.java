package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductOptionDeleteService {

    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionHelper productOptionHelper;

    public void deleteProductOption(Long optionId, String loginId) {
        // 1. 현재 사용자 정보를 가져온다.
        User currentUser = productOptionHelper.getCurrentUser(loginId);

        // 2. 판매자 권한을 검증한다.
        productOptionHelper.validateSellerRole(currentUser);

        // 3. 상품 옵션 정보를 가져온다.
        ProductOption productOption = productOptionHelper.getProductOption(optionId);

        // 4. 해당 상품 옵션이 내 가게 상품인지 검증한다.
        productOptionHelper.validateSellerPermission(currentUser, productOption);

        // 5. 주문 내역이 있는지 확인한다.
        productOptionHelper.validateNotOrderedOption(productOption);

        // 6. 상품 옵션을 삭제한다.
        productOptionRepository.delete(productOption);
    }
}