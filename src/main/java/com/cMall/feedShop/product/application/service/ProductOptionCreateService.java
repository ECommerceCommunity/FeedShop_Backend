package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.request.ProductOptionCreateRequest;
import com.cMall.feedShop.product.application.dto.response.ProductOptionCreateResponse;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductOptionCreateService {

    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionHelper productOptionHelper;

    public ProductOptionCreateResponse addProductOption(Long productId, ProductOptionCreateRequest request, String loginId) {
        // 1. 현재 사용자를 가져오고 권한을 검증한다.
        User currentUser = productOptionHelper.getCurrentUser(loginId);

        // 2. 상품 소유권을 검증하고 상품 정보를 가져온다.
        Product product = productOptionHelper.getProductOwnership(productId, currentUser.getId());

        // 3. DB 에서 같은 옵션이 이미 있는지 확인한다.
        productOptionHelper.validateDuplicateOption(product, request);

        // 4. 상품 옵션을 생성한다.
        ProductOption newOption = productOptionHelper.createNewOption(request, product);

        // 5. 생성된 옵션을 저장한다.
        ProductOption savedOption = productOptionRepository.save(newOption);

        // 6. 저장된 옵션의 ID를 포함한 응답 객체를 생성하여 반환한다.
        return ProductOptionCreateResponse.of(savedOption.getOptionId());
    }
}