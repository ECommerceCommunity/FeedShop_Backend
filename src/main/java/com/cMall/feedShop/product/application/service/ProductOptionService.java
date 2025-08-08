package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductOptionCreateRequest;
import com.cMall.feedShop.product.application.dto.response.ProductOptionCreateResponse;
import com.cMall.feedShop.product.application.dto.response.info.ProductOptionInfo;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductOptionService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ProductOptionRepository productOptionRepository;

    /**
     * 상품 ID로 상품 옵션 정보를 조회하는 서비스 메서드
     *
     * @param productId 상품 ID
     * @param userDetails 사용자 정보 (판매자)
     * @return 상품 옵션 정보 리스트
     */
    @Transactional(readOnly = true)
    public List<ProductOptionInfo> getProductOptions(Long productId, UserDetails userDetails) {
        // 1. 현재 사용자를 가져오고 권한을 검증한다.
        User currentUser = getCurrentUser(userDetails);

        // 2. 판매자 권한을 검증한다.
        validateSellerRole(currentUser);

        // 3. 상품 소유권을 검증하고 상품 정보를 가져온다.
        Product product = getProductOwnership(productId, currentUser.getId());

        // 4. 상품 옵션 정보를 조회하여 반환한다.
        return ProductOptionInfo.fromList(product.getProductOptions());
    }

    /**
     * 상품 옵션을 추가하는 서비스 메서드
     *
     * @param productId 상품 ID
     * @param request   상품 옵션 생성 요청
     * @param userDetails 인증된 사용자 정보
     * @return 생성된 상품 옵션의 ID를 포함한 응답 객체
     */
    @Transactional
    public ProductOptionCreateResponse addProductOption(Long productId, ProductOptionCreateRequest request, UserDetails userDetails) {
        // 1. 현재 사용자를 가져오고 권한을 검증한다.
        User currentUser = getCurrentUser(userDetails);

        // 2. 상품 소유권을 검증하고 상품 정보를 가져온다.
        Product product = getProductOwnership(productId, currentUser.getId());

        // 3. DB 에서 같은 옵션이 이미 있는지 확인한다.
        validateDuplicateOption(product, request);

        // 4. 상품 옵션을 생성한다.
        ProductOption newOption = createNewOption(request, product);

        // 5. 생성된 옵션을 저장한다.
        ProductOption savedOption = productOptionRepository.save(newOption);

        // 6. 저장된 옵션의 ID를 포함한 응답 객체를 생성하여 반환한다.
        return ProductOptionCreateResponse.of(savedOption.getOptionId());
    }

    private User getCurrentUser(UserDetails userDetails) {
        // 사용자 정보에서 로그인 ID를 가져온다.
        String loginId = userDetails.getUsername();

        // 로그인 ID로 사용자 정보를 조회한다.
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ProductException(ErrorCode.USER_NOT_FOUND));

        // 사용자가 SELLER 권한을 가지고 있는지 확인
        if (user.getRole() != UserRole.SELLER) {
            throw new ProductException(ErrorCode.FORBIDDEN);
        }

        return user;
    }

    private Product getProductOwnership(Long productId, Long currentUserId) {
        // 상품을 조회한다
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));

        // 현재 사용자의 가게를 조회한다
        Store userStore = getUserStore(currentUserId);

        // 상품이 해당 가게에 속하는지 확인한다
        if (!product.getStore().getStoreId().equals(userStore.getStoreId())) {
            throw new ProductException(ErrorCode.PRODUCT_NOT_BELONG_TO_STORE);
        }

        return product;
    }

    // 스토어 조회
    private Store getUserStore(Long userId) {
        // 내 가게를 찾는다.
        return storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new ProductException(ErrorCode.STORE_NOT_FOUND));
    }

    private void validateSellerRole(User user) {
        if (user.getRole() != UserRole.SELLER) {
            throw new ProductException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateDuplicateOption(Product product, ProductOptionCreateRequest request) {

        // DB 에서 상품의 옵션들 중에서 요청한 옵션과 동일한 옵션이 있는지 확인한다.
        boolean isDuplicate = productOptionRepository.existsByProduct_ProductIdAndGenderAndSizeAndColor(
                product.getProductId(),
                request.getGender(),
                request.getSize(),
                request.getColor()
        );

        // 만약 동일한 옵션이 있다면 예외를 발생시킨다.
        if (isDuplicate) {
            throw new ProductException(ErrorCode.DUPLICATE_PRODUCT_OPTION);
        }
    }

    // 옵션 객체를 생성한다.
    private ProductOption createNewOption(ProductOptionCreateRequest request, Product product) {
        return new ProductOption(
                request.getGender(),
                request.getSize(),
                request.getColor(),
                request.getStock(),
                product
        );
    }
}
