package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.request.ProductOptionUpdateRequest;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductOptionUpdateService {

    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionHelper productOptionHelper;

    public void updateProductOption(Long optionId, ProductOptionUpdateRequest request, String loginId) {
        // 1. 현재 사용자 정보를 가져온다.
        User currentUser = productOptionHelper.getCurrentUser(loginId);

        // 2. 판매자 권한을 검증한다.
        productOptionHelper.validateSellerRole(currentUser);

        // 3. 상품 옵션 정보를 가져온다.
        ProductOption productOption = productOptionHelper.getProductOption(optionId);

        // 4. 해당 상품 옵션이 내 가게 상품인지 검증한다.
        productOptionHelper.validateSellerPermission(currentUser, productOption);

        // 5. 옵션 정보를 업데이트한다.
        productOptionHelper.updateOptionInfo(productOption, request);

        // 6. 변경된 상품 옵션을 저장한다.
        productOptionRepository.save(productOption);
    }
}